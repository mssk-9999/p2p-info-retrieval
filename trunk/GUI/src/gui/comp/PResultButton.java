package gui.comp;

import gui.util.Result;

import java.awt.Insets;

import javax.swing.JButton;

public class PResultButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7922890280779915224L;

	public PResultButton( Result result ) {
//		this.setIcon(icon);
		this.setBorderPainted(false);
		this.setMargin(new Insets(0,0,0,0));
		this.setText(result.getText());
	}
}
