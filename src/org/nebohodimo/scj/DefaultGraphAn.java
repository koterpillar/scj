/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nebohodimo.scj;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base implementation of graphematic analyzer
 * 
 * Base implementation of graphematic analyzer. Splits the input by character
 * classes.
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class DefaultGraphAn implements GraphAn {

	private Collection<Set<Integer>> compatibleCharacters;

	public DefaultGraphAn() {
		compatibleCharacters = new ArrayList<Set<Integer>>();
		addCompatibleCharacterTypes(Character.LOWERCASE_LETTER,
				Character.UPPERCASE_LETTER);
		addCompatibleCharacterTypes(Character.LOWERCASE_LETTER,
				Character.TITLECASE_LETTER);
	}

	private void addCompatibleCharacterTypes(int t1, int t2) {
		for (Set<Integer> compset : compatibleCharacters) {
			if (compset.contains(t1)) {
				compset.add(t2);
				return;
			}
			if (compset.contains(t2)) {
				compset.add(t1);
				return;
			}
		}
		Set<Integer> newSet = new HashSet<Integer>();
		newSet.add(t1);
		newSet.add(t2);
		compatibleCharacters.add(newSet);
	}

	private boolean areCharactersCompatible(int c1, int c2) {
		int t1 = Character.getType(c1);
		int t2 = Character.getType(c2);
		if (t1 == t2) {
			return true;
		} else {
			for (Set<Integer> compset : compatibleCharacters) {
				if (compset.contains(t1)) {
					return compset.contains(t2);
				}
			}
			return false;
		}
	}

	private int commonType(Collection<Integer> characters) {
		if (characters.isEmpty()) {
			throw new IllegalArgumentException("Set is empty.");
		}
		Integer min = null;
		for (Integer c : characters) {
			int i = Character.getType(c);
			if (min == null || min > i) {
				min = i;
			}
		}
		return min;
	}

	public List<Grapheme> parse(Reader reader) throws IOException {
		ArrayList<Grapheme> result = new ArrayList<Grapheme>();
		StringBuilder builder;
		int c = reader.read();
		while (c != -1) {
			builder = new StringBuilder();
			Set<Integer> chars = new HashSet<Integer>();
			int c2 = c;
			while (c2 != -1 && areCharactersCompatible(c2, c)) {
				builder.append((char) c2);
				chars.add(c2);
				c2 = reader.read();
			}
			String graphemeString = builder.toString();
			DefaultGrapheme grapheme = new DefaultGrapheme(graphemeString);
			grapheme.setProperty("characterCategory", commonType(chars));
			result.add(grapheme);
			c = c2;
		}
		return result;
	}
}
