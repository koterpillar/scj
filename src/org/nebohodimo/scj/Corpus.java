/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nebohodimo.scj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * A collection of texts as a language resource
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class Corpus extends InternetSearcherImpl {

	private static final int MAX_DOCS = 1000;
	private String directory_;
	private String cacheDirectory_;
	private String pattern_;

	public String getDirectory() {
		return directory_;
	}

	public String getCacheDirectory() {
		return cacheDirectory_;
	}

	public String getPattern() {
		return pattern_;
	}

	/**
	 * Construct a new corpus language resource
	 * 
	 * @param directory
	 *            Directory with the text files to search
	 * @param pattern
	 *            File pattern to restrict searches with
	 * @param cacheDirectory
	 *            Directory to use for cache files
	 */
	public Corpus(String directory, String pattern, String cacheDirectory) {
		directory_ = directory;
		pattern_ = pattern;
		cacheDirectory_ = cacheDirectory;
	}

	/**
	 * Construct the index for searching
	 * 
	 * @throws IOException
	 */
	public void computeIndex() throws IOException {
		IndexWriter writer = new IndexWriter(cacheDirectory_,
				new StandardAnalyzer(), MaxFieldLength.UNLIMITED);
		searchForFiles(new File(directory_), pattern_, writer);
		writer.optimize();
		writer.close();
	}

	private void searchForFiles(File directory, final String pattern,
			IndexWriter writer) throws IOException {
		for (File f : directory.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getAbsolutePath().endsWith(pattern);
			}
		})) {
			// index that file
			FileInputStream fs = new FileInputStream(f);
			BufferedReader fr = new BufferedReader(new InputStreamReader(fs));
			// read xml header
			String header = fr.readLine();
			if (header.indexOf("<?xml") < 0) {
				continue; // skip this file

			}
			if (header.indexOf("1251") >= 0) {
				fr.close();
				fr = new BufferedReader(new InputStreamReader(fs, Charset
						.forName("cp1251")));
			}
			Pattern paragraphRegex = Pattern.compile("\\<p\\>([^<]+)\\</p\\>");
			StringBuilder rawText = new StringBuilder();
			while (fr.ready()) {
				rawText.append(fr.readLine());
			}
			Matcher matcher = paragraphRegex.matcher(rawText);
			int i = 0;
			while (matcher.find()) {
				String paragraph = matcher.group();
				Document doc = new Document();
				doc.add(new Field("text", paragraph, Field.Store.COMPRESS,
						Field.Index.ANALYZED));
				doc.add(new Field("path", f.getPath() + i, Field.Store.YES,
						Field.Index.NO));
				i++;
			}
		}
		for (File subDir : directory.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.isDirectory();
			}
		})) {
			searchForFiles(subDir, pattern, writer);
		}
	}

	public Occurences findOccurences(Collection<String> words)
			throws IOException {
		BooleanQuery query = new BooleanQuery();
		for (String word : words) {
			query.add(new TermQuery(new Term("text", word)), Occur.MUST);
		}
		Searcher searcher = new IndexSearcher(cacheDirectory_);
		TopDocs hits = searcher.search(query, MAX_DOCS);
		Occurences result = new Occurences(hits.totalHits);
		// for (int i = 0; i < hits.scoreDocs.length; i++) {
		// ScoreDoc d = hits.scoreDocs[i];
		// Reader text = d.getField("text").readerValue();
		// result.add(getGraphAn().parse(text));
		// }
		return result;
	}
}
