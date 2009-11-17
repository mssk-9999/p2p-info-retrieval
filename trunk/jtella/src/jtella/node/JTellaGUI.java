package jtella.node;
import javax.swing.*;

import com.kenmccrary.jtella.GNUTellaConnection;
import com.kenmccrary.jtella.NodeConnection;

import java.awt.event.*;
import java.util.List;


public class JTellaGUI extends JFrame implements ActionListener {
    /**
	 * to suppress warning
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The Main controller of the UP2P stand-alone application 
	 */
	private JTellaNode controller;
	
	/**
     * JTextArea for the active connections.
     */
    private JTextArea ta1;

    /**
     * JTextArea for incoming messages.
     */
    private JTextArea ta2;

    /**
     * JTextArea for the responses from the network.
     */
    private JTextArea status;

    protected JTextField textField;
    private final static String newline = "\n";
    
    private ConnectionViewer connections;
    
    /**
     * Build the GUI.
     */
    public JTellaGUI(String title, JTellaNode controller) {
        super(title); //create JFrame
        
        //add the controller
        this.controller = controller;
        
        /*
         * All the GUI stuff
         * 
         */
        Box box = Box.createVerticalBox();

        ta1 = new JTextArea(10,40);
        ta1.setEditable(false);
        JScrollPane pane1 =
            new JScrollPane(ta1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane1.setBorder(BorderFactory.createTitledBorder("Active Connections"));
        box.add(pane1);

        ta2 = new JTextArea(10, 40);
        ta2.setEditable(false);
        JScrollPane pane2 =
            new JScrollPane(ta2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane2.setBorder(BorderFactory.createTitledBorder("Incoming messages"));
        box.add(pane2);
        
        textField = new JTextField(20);
        textField.addActionListener(this);
        JScrollPane pane3 =
            new JScrollPane(textField, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane3.setBorder(BorderFactory.createTitledBorder("Manual message injector"));
        box.add(pane3);
        
        status = new JTextArea(10, 40);
        status.setEditable(false);
        JScrollPane pane4 =
            new JScrollPane(status, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane4.setBorder(BorderFactory.createTitledBorder("Responses..."));
        box.add(pane4);

        getContentPane().add(box);
        
        connections = new ConnectionViewer(controller.getConnection(), ta1);
        connections.start();
    }

    
    /** 
     * the user types something in the input box
     */
    public void actionPerformed(ActionEvent evt) {
        String text = textField.getText();
         
        controller.injectmessage(text.trim(), "randomID");
        textField.selectAll(); //select all so that next input removes...
    }
    
    
    /** a searchMessage arrives*/
	public void incomingMsg(String next) {
	
		ta2.append("JTella Says: "+ next );
		ta2.append("\n");
	}

	/**
	 *  this should be a response from an input search
	 * @param s
	 */
	public void callBack(String s) {
		status.append("New Response : "+s+" \n" );

	}

	private class ConnectionViewer extends Thread {

		private JTextArea ta;
		GNUTellaConnection gnutellaConnection;

		public ConnectionViewer(GNUTellaConnection gconn, JTextArea ta2) {
			gnutellaConnection = gconn;
			ta = ta2;
		}


		public void run() {

			while (true){ //TODO add a shutdown flag
				ta.setText("");

				List<NodeConnection> l = gnutellaConnection.getConnectionList();

				String output = "";

				for (NodeConnection conn : l) {
					output = output + "Live connection: " + conn.getConnectedServant() + " status:"+ conn.getStatus() + "\n" ;
				}

				ta.setText(output);

				ta.setCaretPosition(Math.max(0, ta.getText().length()-1));
				//wait two seconds
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}