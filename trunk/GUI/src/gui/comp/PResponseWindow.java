package gui.comp;

import gui.util.Result;
import gui.util.ResultsReader;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JPanel;


public class PResponseWindow extends PWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8524993653000658410L;
	private static final String WINDOW_TITLE = "Response";

	public PResponseWindow() {
		setWindowProperties();
		addPanel();

	}

	private void addPanel() {
		JPanel contentPanel = new JPanel(new FlowLayout());
		JPanel resultsPane = createResultsPane();
		contentPanel.add("Center", resultsPane);
		this.add(contentPanel);
	}

	private JPanel createResultsPane() {
		JPanel editorPane = new JPanel();
//		editorPane.setEditable(false);
		editorPane.setSize(500, 500);
		editorPane.setBackground(Color.WHITE);
		
		try {
			ResultsReader reader = new ResultsReader("data/results.txt");
			Result line;
			while((line = reader.getResult()) != null) {
				PResultButton result = new PResultButton(line);
				editorPane.add(result);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return editorPane;
	}
	
	private void setWindowProperties() {
		this.setTitle(WINDOW_TITLE);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
