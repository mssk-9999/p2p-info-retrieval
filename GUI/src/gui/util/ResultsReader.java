package gui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ResultsReader {

	File file = null;
	FileReader fr = null;
	BufferedReader br = null;
	private int i = 0;

	public ResultsReader( String fileName ) throws Exception {
//		file = new File( fileName );
//
//		try {
//			fr = new FileReader(file);
//			br = new BufferedReader(fr);
//		} catch (FileNotFoundException e) {
//			throw new Exception("Could not find the file: " + fileName);
//		}
	}

	public void close() throws Exception {
//		try{
//			fr.close();
//			br.close();
//		} catch (IOException e) {
//			throw new Exception("Exception in FileReader.close: " + e.getMessage());
//		}
	}
	
	public Result getResult() throws Exception {
//		try {
			if(i<10){
				i++;
				Result result = new Result("Some text", "http://www.google.com");
//				return br.readLine();
				return result;
			}else{
				return null;
			}
			
//		} catch (IOException e) {
//			throw new Exception("Exception in FileReader.getLine: " + e.getMessage());
//		}
	}

}
