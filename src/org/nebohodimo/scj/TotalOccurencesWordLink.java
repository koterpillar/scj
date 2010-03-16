package org.nebohodimo.scj;

public class TotalOccurencesWordLink implements WordLink {

	@Override
	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max) {
		return Math.log(together.getHitCount());
	}

}
