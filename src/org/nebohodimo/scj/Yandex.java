package org.nebohodimo.scj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Yandex web search engine as a language resource
 * 
 * Yandex web search engine as a language resource, using Yandex.XML
 * {@link http://xml.yandex.ru/}
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class Yandex extends InternetSearcherImpl {

	private static final String QUERY_ADDRESS = "http://xmlsearch.yandex.ru/xmlsearch";
	private static final String XML_QUERY = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>\n"
			+ "<request>\n"
			+ "<query>%s</query>\n"
			+ "<maxpassages>5</maxpassages>"
			+ "<groupings>\n"
			+ "<groupby attr=\"d\" mode=\"deep\" "
			+ "groups-on-page=\"10\" docs-in-group=\"1\" />\n"
			+ "</groupings>\n" + "</request>\n";

	private static final XPathExpression WORDSTAT_PATH;
	private static final XPathExpression PHRASE_COUNT_PATH;
	private static final XPathExpression ERROR_PATH;
	private static final XPathExpression ERROR_CODE_PATH;
	private static final XPathExpression PASSAGES_PATH;

	private Date limitExceededTime = null;
	private int queries_ = 0;
	private Map<String, Document> cache = new Hashtable<String, Document>();
	private Map<String, Long> wordCache = new Hashtable<String, Long>();
	private QueryStyle queryStyle_;

	/**
	 * Type of queries to use
	 * 
	 * @author alex
	 * 
	 */
	public enum QueryStyle {
		/**
		 * Plain queries - default Yandex behavior
		 */
		Plain,
		/**
		 * Strict queries - the exact word forms must appear in the text
		 */
		Strict,
		/**
		 * Query the exact word sequence, in any word forms
		 */
		Magic
	}

	static {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		try {
			WORDSTAT_PATH = xpath.compile("//wordstat/text()");
			PHRASE_COUNT_PATH = xpath
					.compile("//response/found[@priority=\"phrase\"]/text()");
			ERROR_PATH = xpath.compile("//error/text()");
			ERROR_CODE_PATH = xpath.compile("//error/@code");
			PASSAGES_PATH = xpath.compile("//passage");

		} catch (XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Construct a new Yandex search engine
	 * 
	 * @param queryStyle
	 *            Type of queries to use
	 */
	public Yandex(QueryStyle queryStyle) {
		queryStyle_ = queryStyle;
	}

	@Override
	protected String getQueryString(Collection<String> words) {
		switch (queryStyle_) {
		case Plain:
			return super.getQueryString(words);
		case Strict:
			return "\"" + super.getQueryString(words) + "\"";
		case Magic:
			StringBuilder result = new StringBuilder();
			for (String word : words) {
				if (result.length() > 0) {
					result.append(" /(1 1) ");
				}
				result.append("+\"");
				result.append(word);
				result.append('"');
			}
			return result.toString();
		default:
			throw new IllegalStateException("Invalid query style.");
		}
	}

	/**
	 * Check whether the limit of 1000 queries per day is exceeded
	 * 
	 * @return Whether the limit of 1000 queries per day is exceeded
	 */
	public boolean isLimitExceeded() {
		if (limitExceededTime != null) {
			// Check if that was yesterday
			Calendar cal = new GregorianCalendar();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date startOfDay = cal.getTime();
			if (limitExceededTime.after(startOfDay)) {
				return true;
			} else {
				limitExceededTime = null;
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Number of queries made
	 * 
	 * @return Number of queries made
	 */
	public int getQueries() {
		return queries_;
	}

	private Document queryXml(String query) throws IOException {
		queries_++;

		// Send query and receive response
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL(QUERY_ADDRESS).openConnection();
		} catch (MalformedURLException ex) {
			throw new IllegalStateException(ex);
		}
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		OutputStream requestStream = conn.getOutputStream();
		String request = String.format(XML_QUERY, query);
		requestStream.write(request.getBytes("CP1251"));
		requestStream.close();
		InputStream xmlStream = conn.getInputStream();

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder;

		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException(ex);
		}
		try {
			return builder.parse(xmlStream);
		} catch (SAXException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private Document queryCache(String query) {
		if (cache.containsKey(query)) {
			return cache.get(query);
		} else {
			return null;
		}
	}

	private Document queryWithCache(String query) throws IOException {
		Document result = queryCache(query);
		if (result == null) {
			result = queryXml(query);
		}
		return result;
	}

	private void cacheWordCount(Document doc) {
		String freqs;
		try {
			freqs = (String) WORDSTAT_PATH.evaluate(doc, XPathConstants.STRING);
		} catch (XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}

		if (freqs != null && !freqs.equals("")) {
			String[] respWords = freqs.split(",");
			for (String word : respWords) {
				String trimmedWord = word.replace('!', ' ').trim();
				// word = "word: count"
				int sep = trimmedWord.indexOf(": ");
				if (sep > 0) {
					String w = trimmedWord.substring(0, sep);
					long c = Long.valueOf(trimmedWord.substring(sep + 2));
					wordCache.put(w, c);
				}
			}
		}
	}

	private long getPhraseHitCount(Document doc) throws IOException {
		String hitCount, error, errorCode;
		try {
			hitCount = (String) PHRASE_COUNT_PATH.evaluate(doc,
					XPathConstants.STRING);
			error = (String) ERROR_PATH.evaluate(doc, XPathConstants.STRING);
			errorCode = (String) ERROR_CODE_PATH.evaluate(doc,
					XPathConstants.STRING);
		} catch (XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}
		if (hitCount != null && !hitCount.equals("")) {
			return Long.valueOf(hitCount);
		} else {
			if (error != null) {
				if (errorCode != null && !errorCode.equals("")) {
					YandexErrorCode code = YandexErrorCode.valueOf(Integer
							.valueOf(errorCode).intValue());
					if (code == YandexErrorCode.NOTHING_FOUND) {
						return 0;
					} else {
						throw new YandexException(code, error, doc.toString());
					}
				} else {
					throw new YandexException(error, doc.toString());
				}
			} else {
				throw new YandexException(YandexErrorCode.UNKNOWN,
						"Neither error or results are present");
			}
		}
	}

	private static String getPassageText(Node passage) {
		StringBuilder result = new StringBuilder();
		NodeList childNodes = passage.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			result.append(childNodes.item(i).getTextContent());
			result.append(' ');
		}
		return result.toString().trim().replace('\n', ' ').replace('\r', ' ');
	}

	public Occurences findOccurences(Collection<String> words)
			throws IOException {
		Document searchResult = queryWithCache(getQueryString(words));
		cacheWordCount(searchResult);
		NodeList list;
		try {
			list = (NodeList) PASSAGES_PATH.evaluate(searchResult,
					XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}
		Occurences context = new Occurences(getPhraseHitCount(searchResult));
		for (int i = 0; i < list.getLength(); i++) {
			String passageText = getPassageText(list.item(i));
			List<Grapheme> graphemes = graphAn_.parse(new StringReader(
					passageText));
			context.add(graphemes);
		}
		return context;
	}
}
