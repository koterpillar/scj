/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nebohodimo.scj;

/**
 *
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class SciMod
		extends Sci
{
	private static final long minThreshold = 100;
	private static final long maxThreshold = 1000;

	@Override
	protected long relevantHits(String word1, String word2, Occurences o)
	{
		long hits = super.relevantHits(word1, word2, o);
		return hits < minThreshold ? 0 : hits < maxThreshold ? hits / 2 : hits;
	}
}
