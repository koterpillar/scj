package org.nebohodimo.scj;

import java.util.Hashtable;
import java.util.Set;

/**
 * A default implementation of Grapheme.
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class DefaultGrapheme implements Grapheme {
	private static final long serialVersionUID = 1;

	private String grapheme_;
	private Hashtable<String, Object> properties_;

	public DefaultGrapheme(String grapheme) {
		grapheme_ = grapheme;
		properties_ = new Hashtable<String, Object>();
	}

	public String getGrapheme() {
		return grapheme_;
	}

	public Object getProperty(String property) {
		return properties_.get(property);
	}

	public Set<String> getPropertyNames() {
		return properties_.keySet();
	}

	public void setProperty(String property, Object value) {
		properties_.put(property, value);
	}

	public int hashCode() {
		return grapheme_.hashCode();// ^ properties_.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DefaultGrapheme other = (DefaultGrapheme) obj;
		return this.grapheme_.equals(other.grapheme_);
		/*
		 * if (this.properties_ != other.properties_ && (this.properties_ ==
		 * null || !this.properties_.equals(other.properties_))) { return false;
		 * } return true;
		 */
	}

	public String toString() {
		return grapheme_;
	}
}
