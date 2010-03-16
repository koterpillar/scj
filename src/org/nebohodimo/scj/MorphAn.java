package org.nebohodimo.scj;

import java.io.IOException;
import java.util.List;

public interface MorphAn {

	public abstract List<String> normalForm(String word) throws IOException;

}