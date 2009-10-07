package gui;

import gui.comp.PWindow;

public class MainUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				PWindow window = new PWindow();
				window.showWindow();
			}
		});

	}

}
