package gui.comp;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class PWindow extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String WINDOW_TITLE = "Some Title";
	private static final String SEARCH_FIELD_LABEL = "Search";
	private static final String LOOKANDFEEL = "System";
	
	public PWindow() {
		initLookAndFeel();
		setWindowProperties();
		addPanel();
	}

	private void addPanel() {
		JPanel contentPanel = new JPanel(new BorderLayout());
		PTextField searchField = new PTextField(SEARCH_FIELD_LABEL);
		contentPanel.add(searchField);
		this.add(contentPanel);
	}

	private void setWindowProperties() {
		this.setTitle(WINDOW_TITLE);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/*
	 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
	 */
	private static void initLookAndFeel() {
        String lookAndFeel = null;

        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                                   + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                                   + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                                   + lookAndFeel
                                   + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                                   + lookAndFeel
                                   + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }
	
	public void showWindow() {
		this.pack();
		this.setVisible(true);
	}

}
