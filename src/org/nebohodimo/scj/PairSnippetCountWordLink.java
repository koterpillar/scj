package org.nebohodimo.scj;

import java.io.IOException;
import java.util.List;

import org.nebohodimo.scj.aot.PipeAot;

public class PairSnippetCountWordLink implements WordLink {
	
	private MorphAnUtil morphAn;

	public PairSnippetCountWordLink() throws IOException
	{
		morphAn = new MorphAnUtil(new PipeAot("russian"));
	}

	@Override
	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max) {
		if(together.size() == 0)
			return -1;
		int snippetMiddlesCount = 0;
		for (List<Grapheme> snippet : together) {
			List<List<Grapheme>> snippetMiddles = morphAn.getMiddles(
					word1, word2, snippet);
			snippetMiddlesCount += snippetMiddles.size();
		}
		return snippetMiddlesCount;
	}
}
