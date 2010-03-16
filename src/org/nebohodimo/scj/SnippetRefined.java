/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.nebohodimo.scj.aot.PipeAot;

/**
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class SnippetRefined implements WordLink {

	private WordLink base_;
	private MorphAn morphAn;
	private MorphAnUtil morphAnUtil;

	public SnippetRefined(WordLink base) throws IOException {
		base_ = base;
		morphAn = new PipeAot("Russian");
		morphAnUtil = new MorphAnUtil(morphAn);
	}

	@Override
	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max) {
		long snippets = 0;
		long middles = 0;
		for (List<Grapheme> snippet : together) {
			snippets++;
			middles += morphAnUtil.getMiddles(word1, word2, snippet).size();
		}
		if (snippets == 0)
			return -16;
		if(middles <= 2)
			return -16;
		return base_.get(word1, word2, together, first, second, max);
	}

	public int countPairs(String word1, String word2, List<Grapheme> snippet) {
		int toAdd = 0;
		for (List<Grapheme> middle : morphAnUtil.getMiddles(word1, word2, snippet)) {
			int weighedDist = middleWeight(middle);
			if (weighedDist > 5)
				toAdd++;
		}
		return toAdd;
	}

	public static List<Grapheme> uniteSimilarInMiddle(List<Grapheme> middle) {
		List<Grapheme> result = new ArrayList<Grapheme>();
		for (Grapheme g : middle) {
			int charCategory = ((Integer) g.getProperty("characterCategory"))
					.intValue();
			switch (charCategory) {
			case Character.LOWERCASE_LETTER:
			case Character.UPPERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.OTHER_LETTER:
				DefaultGrapheme g2 = new DefaultGrapheme("word");
				g2.setProperty("characterCategory", charCategory);
				g = g2;
				break;
			default:
				break;
			}
			result.add(g);
		}
		return result;
	}

	private int middleWeight(List<Grapheme> middle) {
		middle = uniteSimilarInMiddle(middle);
		int weighedDist = 0;
		for (Grapheme g1 : middle)
			weighedDist += getWeight(g1);
		return weighedDist;
	}

	public static int getWeight(Grapheme g) {
		int weight = 0;
		int charCategory = ((Integer) g.getProperty("characterCategory"))
				.intValue();
		String gs = g.getGrapheme();
		switch (charCategory) {
		case Character.START_PUNCTUATION:
		case Character.END_PUNCTUATION:
			// things like brackets. Looks severe.
			weight += 10;
			break;
		case Character.SPACE_SEPARATOR:
			// space is a space.
			break;
		case Character.LOWERCASE_LETTER:
		case Character.UPPERCASE_LETTER:
		case Character.OTHER_LETTER:
		case Character.TITLECASE_LETTER:
		case Character.DECIMAL_DIGIT_NUMBER:
			// a word... bad.
			weight += 50;
			break;
		case Character.OTHER_NUMBER:
			if ("⁰¹²³⁴⁵⁶⁷⁸⁹".contains(gs))
				break;
			else {
				weight += 50;
				break;
			}
		case Character.DASH_PUNCTUATION:
			// hyphens
			weight += 2;
			break;
		case Character.OTHER_PUNCTUATION:
			if (gs.equals(","))
				weight += 10;
			else if (gs.equals(".") || gs.equals("?") || gs.equals("...")
					|| gs.equals("!") || gs.endsWith("…"))
				weight += 25;
			else if (gs.equals("\"") || gs.equals("'"))
				weight += 3;
			else if (gs.equals(":"))
				weight += 10;
			else {
				// trash
				weight += 50;
			}
			break;
		case Character.INITIAL_QUOTE_PUNCTUATION:
		case Character.FINAL_QUOTE_PUNCTUATION:
			weight += 3;
			break;
		case Character.FORMAT:
			// nothing harmful there
			break;
		case Character.CONNECTOR_PUNCTUATION:
			// underscore
			weight += 5;
			break;
		case Character.MATH_SYMBOL:
		case Character.OTHER_SYMBOL:
		case Character.CURRENCY_SYMBOL:
			// maybe garbage
			weight += 50;
			break;
		case Character.MODIFIER_LETTER:
		case Character.MODIFIER_SYMBOL:
			weight += 40;
			break;
		case Character.UNASSIGNED:
			weight += 40;
			break;
		default: // 0, 27
			System.out.print("[" + charCategory + "]=" + gs);
		}
		return weight;
	}
}
