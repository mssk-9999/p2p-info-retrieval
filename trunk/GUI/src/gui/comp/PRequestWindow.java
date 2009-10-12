package gui.comp;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class PRequestWindow extends PWindow{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6383984046501125366L;
	private static final String WINDOW_TITLE = "Some Title";
	private static final String SEARCH_FIELD_LABEL = "Search";
	private static final String SEARCH_BUTTON_LABEL = "Go";
	
	public PRequestWindow() {
		setWindowProperties();
		addPanel();
		
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
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
