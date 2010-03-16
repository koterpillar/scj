/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

/**
 *
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public interface WordLink {
	double get(String word1, String word2,
			Occurences together,
			Occurences first, Occurences second, 
			long max);
}
