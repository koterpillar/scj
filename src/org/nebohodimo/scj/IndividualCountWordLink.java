package org.nebohodimo.scj;

public class IndividualCountWordLink implements WordLink {
	private boolean first_;
	
	public IndividualCountWordLink(boolean first) { first_ = first; }
	
	public boolean getFirst() { return first_; }
	
	@Override
	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max) {
		
		return Math.log((first_ ? first : second).getHitCount());
	}

}
