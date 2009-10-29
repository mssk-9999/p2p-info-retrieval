package gui.comp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class PSearchButton extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 937659545648212474L;
	private String labelText = "";
	private PTextField searchField;
	
	public PSearchButton(String labelText, PTextField searchField) {
		this.labelText = labelText;
		this.searchField = searchField;
		createButton();
	}
	
	private void createButton() {
		JButton searchButton = new JButton(labelText);
		searchButton.setActionCommand("performSearch");
		searchButton.addActionListener(this);
		this.add(searchButton);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if ("performSearch".equals(arg0.getActionCommand())) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					PResponseWindow window = new PResponseWindow();
					window.showWindow();
				}
			});
		}

	}

}