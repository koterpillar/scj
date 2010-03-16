/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

/**
 *
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class Mii implements WordLink {
	
	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max)
	{
		return Math.log(max *
				together.getHitCount() /
				first.getHitCount() /
				second.getHitCount());
	}

	public double getMin(long max)
	{
		return Double.NEGATIVE_INFINITY;
	}

	public double getMax(long max)
	{
		return Math.log(max);
	}

	public double combine(double link1, double link2, long max)
	{
		return link1 * link2 / getMax(max);
	}

}