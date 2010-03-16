package org.nebohodimo.scj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Google web search engine as a language resource
 * 
 * Google web search engine as a language resource, using Google AJAX API
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class Google extends InternetSearcherImpl {

	private static String endpointURL = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	private boolean strict_;

	/**
	 * Construct a new instance of Google search engine interface.
	 * 
	 * @param strict
	 *            Whether to surround the query by quotes, to only search for
	 *            exact matches
	 */
	public Google(boolean strict) {
		strict_ = strict;
	}

	@Override
	protected String getQueryString(Collection<String> words) {
		String query = super.getQueryString(words);
		if (strict_)
			query = "\"" + query + "\"";
		return query;
	}

	public Occurences findOccurences(Collection<String> words)
			throws IOException {
		String query = getQueryString(words);
		String finalUrl = endpointURL + URLEncoder.encode(query, "UTF8");
		URL url = new URL(finalUrl);
		URLConnection connection = url.openConnection();
		connection.addRequestProperty("Referer",
				"http://www.mysite.com/index.html");

		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject googleResult;
		try {
			// googleResult = new JSONObject(new
			// JSONTokener(builder.toString()));
			googleResult = JSONObject.fromObject(builder.toString());
			googleResult = googleResult.getJSONObject("responseData");
			JSONArray resultPages = googleResult.getJSONArray("results");
			if (resultPages.size() == 0) {
				return new Occurences(0);
			} else {
				Occurences occurences = new Occurences(googleResult
						.getJSONObject("cursor").getInt("estimatedResultCount"));
				for (int i = 0; i < resultPages.size(); i++) {
					JSONObject resultPage = resultPages.getJSONObject(i);
					String snippet = resultPage.getString("content");
					snippet = snippet.replaceAll("\\<b\\>", "").replaceAll(
							"\\</b\\>", "");
					occurences.add(getGraphAn()
							.parse(new StringReader(snippet)));
				}
				return occurences;
			}
		} catch (JSONException ex) {
			String message = String.format("While querying %s got %s", query,
					builder.toString());
			throw new IOException(message, ex);
		}
	}
}
