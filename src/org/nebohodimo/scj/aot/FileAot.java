package org.nebohodimo.scj.aot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nebohodimo.scj.MorphAn;

public class FileAot implements MorphAn {
	private String executablePath;
	private String language_;
	private Map<String, List<String>> results;

	// private BufferedReader morphOutput;
	// private OutputStreamWriter morphInput;

	public FileAot(String language) throws IOException {
		language_ = language;
		results = new HashMap<String, List<String>>();
	}

	private void launchAot(String language, String file) throws IOException,
			InterruptedException {
		executablePath = System.getenv("RML") + "/Bin/TestLem";
		Process aot = new ProcessBuilder(file == null ? new String[] { executablePath,
				language_ } : new String[] { executablePath, language_, file })
				.start();

		aot.waitFor();
	}

	private List<String> defaultList(String word)
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(word);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.nebohodimo.scj.IMorphAn#normalForm(java.lang.String)
	 */
	public List<String> normalForm(String word) throws IOException {
		if(results.containsKey(word))
			return results.get(word);
		else
		{
			List<String> result = queryNormalForm(word);
			results.put(word, result);
			return result;
		}
	}

	public List<String> queryNormalForm(String word) throws IOException {
		File tmpFile = File.createTempFile("aot", "");
		try {
			OutputStreamWriter tmpFileWriter = new OutputStreamWriter(
					new BufferedOutputStream(new FileOutputStream(tmpFile)),
					Charset.forName("cp1251"));
			tmpFileWriter.write(word + "\n");
			tmpFileWriter.close();

			try {
				launchAot(language_, tmpFile.getAbsolutePath());
			} catch (InterruptedException ex) {
				tmpFile.delete();
				return defaultList(word);
			}

			File aotResultFile = new File(tmpFile.getAbsolutePath() + ".lem");
			try {

				BufferedReader tmpFileReader = new BufferedReader(
						new InputStreamReader(new BufferedInputStream(
								new FileInputStream(aotResultFile)), Charset
								.forName("cp1251")));

				String line = tmpFileReader.readLine();
				tmpFileReader.close();
				if(line == null)
					return defaultList(word);

				ArrayList<String> result = new ArrayList<String>();
				//System.out.println(line);
				Pattern pattern = Pattern.compile("(\\#|\\> )([^ ]+) ");
				Matcher matcher = pattern.matcher(line);
				// СТЕКЛО -> СТЕКЛО еаег#СТЕЧЬ кй#
				while (matcher.find()) {
					String nf = matcher.group(2).toLowerCase();
					//System.out.println(nf);
					result.add(nf);
				}
				return result;
			} finally {
				aotResultFile.delete();
			}
		} finally {
			tmpFile.delete();
		}
	}
}
