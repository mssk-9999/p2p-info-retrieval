import gui.util.ResultsReader;
import junit.framework.TestCase;


public class ResultReaderTests extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testResultsReader() throws Exception {
		ResultsReader reader = new ResultsReader("GUI/data/results.txt");
		reader.getResult();
		reader.close();
	}

}
