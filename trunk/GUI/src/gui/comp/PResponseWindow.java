package gui.comp;

import gui.util.Result;
import gui.util.ResultsReader;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class PResponseWindow extends PWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8524993653000658410L;
	private static final String WINDOW_TITLE = "Response";
	
	private static final int WINDOW_WIDTH = 500;
	private static final int WINDOW_HEIGHT = 500;

	public PResponseWindow() {

		addPanel();
		setWindowProperties();

	}

	private void addPanel() {
		boolean hasResults = false;
		
		Container contentPanel = this.getContentPane();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(Color.WHITE);
		
		try {
			ResultsReader reader = new ResultsReader("./data/results.txt");
			Result line;
			while((line = reader.getResult()) != null) {
				if(!hasResults)
					hasResults = true;
				PResultButton result = new PResultButton(line);
				contentPanel.add(result);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!hasResults) {
			JLabel noResults = new JLabel("No results for the specified search");
			contentPanel.add(noResults);
		}
			
	}
	
	private void setWindowProperties() {
		this.setTitle(WINDOW_TITLE);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
	}
}
