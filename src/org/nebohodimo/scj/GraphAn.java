/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Graphematic analyzer
 * 
 * Graphematic analyzer - turns read strings into tokens
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public interface GraphAn {
	/**
	 * Read the stream and return token sequence
	 * @param reader Reader to use
	 * @return All tokens read
	 * @throws IOException
	 */
	List<Grapheme> parse(Reader reader) throws IOException;
}
