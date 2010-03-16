package org.nebohodimo.scj.aot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.nebohodimo.scj.MorphAn;

public class PipeAot implements MorphAn {
	private Process aot;
	private InputStreamReader aotInput;
	private OutputStreamWriter aotOutput;
	
	public PipeAot(String language) throws IOException
	{
		String executablePath = System.getenv("RML") + "/Source/StreamLem/StreamLem";
		aot = new ProcessBuilder(new String[]{executablePath, language}).start();
		aotOutput = new OutputStreamWriter(aot.getOutputStream());
		aotInput = new InputStreamReader(aot.getInputStream());
	}
	
	@Override
	protected void finalize() throws IOException, InterruptedException
	{
		aotInput.close();
		aotOutput.close();
		aot.waitFor();
	}

	@Override
	public List<String> normalForm(String word) throws IOException {
		aotOutput.write(word);
		aotOutput.write('\n');
		aotOutput.flush();
		StringBuffer line;
		ArrayList<String> result = new ArrayList<String>();
		do
		{
			line = new StringBuffer();
			char c;
			while((c = (char)aotInput.read()) != '\n')
				line.append(c);
			String l = line.toString();
			if(l.length() > 0)
				result.add(l.substring(1));
		}
		while(line.length() > 0);
		return result;
	}

}
