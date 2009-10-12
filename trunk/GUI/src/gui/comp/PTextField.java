package gui.comp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PTextField extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2968117372252629936L;
	private static final String LABEL_SEPARATOR = ": ";

	public PTextField() {
		JTextField field = new JTextField();
		this.add(field);
	}

	public PTextField(String labelText) {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;

		// Add the label
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;

		JLabel label = new JLabel(labelText + LABEL_SEPARATOR);
		this.add(label, c);

		// Add the textfield
		c.gridwidth = GridBagConstraints.REMAINDER;     //end row
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		JTextField field = new JTextField(30);
		this.add(field, c);
	}

}
