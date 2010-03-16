/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

import java.io.IOException;
import java.util.Collection;

/**
 * A resource which can be searched for a word sequence
 * 
 * A resource which can be searched for a word sequence. Examples include a
 * corpus, or Internet via a search engine.
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public interface LanguageResource {
	/**
	 * Find all occurences of a specified word sequence
	 * 
	 * @param words
	 *            Sequence to search
	 * @return Occurences object with found data
	 * @throws IOException
	 */
	Occurences findOccurences(Collection<String> words) throws IOException;
}
