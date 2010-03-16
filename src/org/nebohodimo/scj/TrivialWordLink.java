package org.nebohodimo.scj;

import java.io.IOException;
import java.util.List;

public class TrivialWordLink implements WordLink {
	private SnippetRefined refined;

	public TrivialWordLink() throws IOException
	{
		refined = new SnippetRefined(this);
	}
	
	@Override
	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max) {
		int snippetCount = 0;
		int pairs = 0;
		for(List<Grapheme> lg: together)
		{
			snippetCount++;
			pairs += refined.countPairs(word1, word2, lg);
		}
		/*for(int i = 0; i < pairs; i++)
			System.out.print('#');
		for(int i = pairs; i < snippetCount; i++)
			System.out.print('-');
		for(int i = snippetCount; i < 20; i++)
			System.out.print(' ');*/
		double result = snippetCount == 0 ? 0 : ((double)pairs / (double)snippetCount);
		//if(together.getHitCount() < 1000)
		//	result = 0;
		//System.out.println(result);
		return result * 32 - 16;
	}
}
