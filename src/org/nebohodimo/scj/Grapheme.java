package org.nebohodimo.scj;

import java.io.Serializable;
import java.util.Set;

/**
 * A grapheme - text part with some properties
 * 
 * A grapheme - text part with some properties (e.g. a word, a punctuation mark,
 * a space, a number).
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public interface Grapheme extends Serializable {
	/**
	 * The exact string in the text this Grapheme corresponds to
	 * 
	 * @return The exact string in the text this Grapheme corresponds to
	 */
	String getGrapheme();

	/**
	 * Properties this Grapheme has.
	 * 
	 * Properties this Grapheme has, e.g. word case, normal form, etc.
	 * 
	 * @return Properties this Grapheme has
	 */
	Set<String> getPropertyNames();

	/**
	 * Value of a property
	 * @param property Property to get
	 * @return Value of the property
	 */
	Object getProperty(String property);
}
