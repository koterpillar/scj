/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nebohodimo.scj;

/**
 *
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class Sci
		implements WordLink
{

	private static final double logMax = 16;

	protected long relevantHits(String word1, String word2, Occurences o)
	{
		return o.getHitCount();
	}

	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max)
	{
		if(together.getHitCount() == 0)
			return -logMax;
		else
			return logMax + Math.log(
					relevantHits(word1, word2, together) /
					Math.sqrt(first.getHitCount() * second.getHitCount())) /
				Math.log(2);
	}

	public double getMin(long max)
	{
		return -logMax;
	}

	public double getMax(long max)
	{
		return logMax;
	}

	public double combine(double link1, double link2, long max)
	{
		return link1 * link2 / logMax;
	}
}
