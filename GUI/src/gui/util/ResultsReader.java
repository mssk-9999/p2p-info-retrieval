package gui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ResultsReader {

	private static final String LINK_END_TAG = "</link>";
	private static final String LINK_START_TAG = "<link>";
	private static final String TXT_END_TAG = "</txt>";
	private static final String TXT_START_TAG = "<txt>";
	File file = null;
	FileReader fr = null;
	BufferedReader br = null;

	public ResultsReader( String fileName ) throws Exception {
		file = new File( fileName );

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find the file: " + fileName);
		}
	}

	public void close() throws Exception {
		try{
			fr.close();
			br.close();
		} catch (IOException e) {
			throw new Exception("Exception in FileReader.close: " + e.getMessage());
		}
	}

	public Result getResult() throws Exception {
		try {
			Result result;
			while((result = parseResult(br.readLine())) != null && result.equals( new Result() )) {
				continue;
			}
			return result;

		} catch (Exception e) {
			throw new Exception("Exception in FileReader.getLine: " + e.getMessage());
		}
	}

	private Result parseResult( String line ) throws Exception {
		if(line == null)
			return null;

		int textStart = line.indexOf(TXT_START_TAG);
		int textEnd = line.indexOf(TXT_END_TAG);
		int linkStart = line.indexOf(LINK_START_TAG);
		int linkEnd = line.indexOf(LINK_END_TAG);

		System.out.println("Line: " + line);

		if(textStart < 0 || textEnd < 0 || linkStart < 0 || linkEnd < 0) {
			System.out.println("Ignoring line - Bad formatting");
			return new Result();
		}

		String text = line.substring(textStart + TXT_START_TAG.length(), textEnd);
		System.out.println("Text: " + text);

		String link = line.substring(linkStart + LINK_START_TAG.length(), linkEnd);
		System.out.println("Link: " + link);

		return new Result(text, link);
	}

}
