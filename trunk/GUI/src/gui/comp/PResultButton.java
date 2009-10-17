package gui.comp;

import gui.util.Result;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class PResultButton extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7922890280779915224L;
	JButton button;
	String link = "";

	public PResultButton(Result result) {
		button = new JButton();
		// this.setIcon(icon);
		button.setBorderPainted(false);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setText(result.getText());
		link = result.getUrl();
		button.setActionCommand("activateLink");
		button.addActionListener(this);

		this.add(button);
	}

	private void openLink(String link) {
		if (!java.awt.Desktop.isDesktopSupported()) {
			System.err.println("Desktop is not supported (fatal)");
			System.exit(1);
		}

		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
			System.err.println("Desktop doesn't support the browse action (fatal)");
			System.exit(1);
		}

		try {
			java.net.URI uri = new java.net.URI(link);
			desktop.browse(uri);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("activateLink".equals(e.getActionCommand())) {
			System.out.println("Button: " + button.getText() + " pressed.");
			openLink(link);
		}

	}

}
