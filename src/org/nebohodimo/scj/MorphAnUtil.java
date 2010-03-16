package org.nebohodimo.scj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MorphAnUtil {
	private MorphAn morphAn_;

	public MorphAnUtil(MorphAn morphAn)
	{
		morphAn_ = morphAn;
	}

	public boolean normalFormEquals(Grapheme g1, String word2) {
		if (g1 == null)
			return word2 == null;
		if (word2 == null)
			return false;
		int cc1 = ((Integer) g1.getProperty("characterCategory")).intValue();
		String word1 = g1.getGrapheme();
		if (cc1 != Character.LOWERCASE_LETTER
				&& cc1 != Character.UPPERCASE_LETTER)
			return word1.equals(word2);

		if (word1.length() <= 3 || word2.length() <= 3)
			return word1.equals(word2);
		List<String> nf1 = null;
		List<String> nf2 = null;
		try {
			nf1 = morphAn_.normalForm(word1);
			nf2 = morphAn_.normalForm(word2);
		} catch (IOException e) {
			if (nf1 == null)
				nf1 = new ArrayList<String>();
			if (nf2 == null)
				nf2 = new ArrayList<String>();
		}
		for (String form : nf1)
			if (nf2.contains(form))
				return true;
		return false;
	}
	
	public List<List<Grapheme>> getMiddles(String word1, String word2,
			List<Grapheme> snippet) {
		List<List<Grapheme>> result = new ArrayList<List<Grapheme>>();
		Iterator<Grapheme> i = snippet.iterator();
		while (i.hasNext()) {
			Grapheme g = null;
			while (i.hasNext()
					&& !normalFormEquals((g = i.next()), word1))
				continue;
			List<Grapheme> middleList = new ArrayList<Grapheme>();
			while (i.hasNext()
					&& !normalFormEquals((g = i.next()), word2)
					&& !normalFormEquals(g, word1)) {
				middleList.add(g);
			}
			if (g != null && normalFormEquals(g, word2))
				result.add(middleList);
		}
		return result;
	}
}
