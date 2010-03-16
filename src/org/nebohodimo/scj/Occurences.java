/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Information gathered about a word sequence occurences
 * 
 * Information gathered about a word sequence occurences Includes total hit
 * count and a list of fragments containing it. Both might be inaccurate and/or
 * incomplete.
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class Occurences implements Collection<List<Grapheme>> {
	private long hitCount_;
	private Collection<List<Grapheme>> context_;

	public Occurences(long hitCount) {
		hitCount_ = hitCount;
		context_ = new ArrayList<List<Grapheme>>();
	}

	/**
	 * Get the total hit count
	 * 
	 * @return Number of times the word sequence occured in a language resource
	 */
	public long getHitCount() {
		return hitCount_;
	}

	public int size() {
		return context_.size();
	}

	public boolean isEmpty() {
		return context_.isEmpty();
	}

	public boolean contains(Object arg0) {
		return context_.contains(arg0);
	}

	public Iterator<List<Grapheme>> iterator() {
		return context_.iterator();
	}

	public Object[] toArray() {
		return context_.toArray();
	}

	public <T> T[] toArray(T[] arg0) {
		return context_.toArray(arg0);
	}

	public boolean add(List<Grapheme> arg0) {
		return context_.add(arg0);
	}

	public boolean remove(Object arg0) {
		return context_.remove(arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		return context_.containsAll(arg0);
	}

	public boolean addAll(Collection<? extends List<Grapheme>> arg0) {
		return context_.addAll(arg0);
	}

	public boolean removeAll(Collection<?> arg0) {
		return context_.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0) {
		return context_.retainAll(arg0);
	}

	public void clear() {
		context_.clear();
	}
}
