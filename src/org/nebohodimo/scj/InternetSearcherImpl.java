package org.nebohodimo.scj;

import java.util.Collection;

/**
 * Base class for Internet search engines as language resources
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public abstract class InternetSearcherImpl implements LanguageResource {

	protected GraphAn graphAn_;

	/**
	 * Construct a new searcher with default graphematic analyzer
	 */
	public InternetSearcherImpl() {
		graphAn_ = new DefaultGraphAn();
	}

	/**
	 * Construct a new searcher with a specified analyzer
	 * @param graphAn Graphematic analyzer to use
	 */
	public InternetSearcherImpl(GraphAn graphAn) {
		graphAn_ = graphAn;
	}

	public GraphAn getGraphAn() {
		return graphAn_;
	}

	public void setGraphAn_(GraphAn graphAn) {
		graphAn_ = graphAn;
	}

	/**
	 * Build a query string for asking the searcher
	 * @param words Word sequence to join
	 * @return Words joined by spaces 
	 */
	protected String getQueryString(Collection<String> words) {
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(word);
		}
		return result.toString();
	}
}
