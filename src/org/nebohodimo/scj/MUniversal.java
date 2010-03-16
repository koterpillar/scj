/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nebohodimo.scj;

/**
 *
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class MUniversal
		implements WordLink
{

	private static final double base = 16;
	private double power_;

	public MUniversal(double power)
	{
		power_ = power;
	}

	public double getPower()
	{
		return power_;
	}

	public double get(String word1, String word2, Occurences together,
			Occurences first, Occurences second, long max)
	{
		double n1 = first.getHitCount();
		double n2 = second.getHitCount();
		double n12 = together.getHitCount();
		double m12 = fInverse((f(n1) + f(n2)) / 2);
		return base + Math.log(n12 / m12) / Math.log(2);
	}

	public double getMin(long max)
	{
		return Double.NEGATIVE_INFINITY;
	}

	public double getMax(long max)
	{
		return base;
	}

	public double combine(double link1, double link2, long max)
	{
		return link1 + link2 - base;
	}

	private double f(double z)
	{
		if (power_ == 0)
		{
			return Math.log(z);
		} else
		{
			return Math.pow(z, power_);
		}
	}

	private double fInverse(double z)
	{
		if (power_ == 0)
		{
			return Math.exp(z);
		} else
		{
			return Math.pow(z, 1 / power_);
		}
	}
	/*F (z)     √M12           Name of M12
	log z       N1 N2       Mean geometric
	1/z 2N1 N2 /(N1 + N2 ) Mean harmonic
	√      √
	√
	z (( N 1 + N 2 )/2)2 Mean square root
	z     (N1 + N2 )/2    Mean arithmetic
	z2        2     2
	((N1 + N2 )/2)  Mean quadratic
	 */
}
