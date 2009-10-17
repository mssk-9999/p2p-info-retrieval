package gui.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PRequestWindow extends PWindow {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6383984046501125366L;
	private static final String WINDOW_TITLE = "Request";
	private static final String SEARCH_FIELD_LABEL = "Search";
	private static final String SEARCH_BUTTON_LABEL = "Go";
	
	private static final int WINDOW_WIDTH = 500;
	private static final int WINDOW_HEIGHT = 500;
	
	public PRequestWindow() {
		addPanel();
		setWindowProperties();		
	}

	private void addPanel() {
		JPanel contentPanel = new JPanel(new BorderLayout());
		
		PTextField searchField = new PTextField(SEARCH_FIELD_LABEL);
		contentPanel.add("Center", searchField);
		
		PSearchButton searchButton = new PSearchButton(SEARCH_BUTTON_LABEL, searchField);
		contentPanel.add("South", searchButton);
		
		this.add(contentPanel);
	}

	private void setWindowProperties() {
		this.setTitle(WINDOW_TITLE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
	}

}
