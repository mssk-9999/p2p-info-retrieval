package gui.comp;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JEditorPane;
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
		JPanel contentPanel = new JPanel(new BorderLayout());
		JEditorPane resultsPane = createEditorPane();
		contentPanel.add("Center", resultsPane);
		this.add(contentPanel);
	}
	
	 private JEditorPane createEditorPane() {
	        JEditorPane editorPane = new JEditorPane();
	        editorPane.setEditable(false);
	        java.net.URL helpURL = PResponseWindow.class.getResource(
	                                        "results.html");
	        if (helpURL != null) {
	            try {
	                editorPane.setPage(helpURL);
	            } catch (IOException e) {
	                System.err.println("Attempted to read a bad URL: " + helpURL);
	            }
	        } else {
	            System.err.println("Couldn't find file: results.html");
	        }

	        return editorPane;
	    }



	private void setWindowProperties() {
		this.setTitle(WINDOW_TITLE);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
