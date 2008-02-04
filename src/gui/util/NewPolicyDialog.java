package gui.util;

import gui.explorer.PolicyExplorer;
import gui.images.Images;

import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

/**
 *  Diese Klasse bietet einen Inputdialog an, mit dem der Benutzer eine
 *  Policy mit den benoetigten Attribute anlegen kann.
 * 
 * @author      Windy
 * @version     1.0
 */
public class NewPolicyDialog  extends JDialog{
	
	private static final long serialVersionUID = 8712680335095577395L;
	
	private JTextField inputFieldPolicyName;
	
	private JTextField inputFieldFileName;
	
	private JTextField inputFieldID;	
	
	private JTextField inputFieldDescription;
	
	private JLabel tipLabel;
	
	private JButton btOk;
	
	private JButton btCancel;
	
	private JCheckBox defaultFileNameCheckBox;
	
	private String fileName = null;
	
	private String typedFileName;
	
	private PolicyExplorer cause;

	/**
	 * Create the dialog
	 */
	public NewPolicyDialog (PolicyExplorer cause) {
		super();
		getContentPane().setLayout(null);
		setTitle("input dialog");
		setModal(true);
		setDefaultLookAndFeelDecorated(true);
		setBounds(100, 100, 446, 260);
		
		this.cause = cause;
		
        createWindow();

	}

	
	private void createWindow(){
		
		tipLabel = new JLabel();
		tipLabel.setBounds(126, 0, 299, 25);
		tipLabel.setText("File name is empty.");
		tipLabel.setIcon(Images.ICON_ERROR);
        tipLabel.setVisible(true);
		getContentPane().add(tipLabel);
		
		final JLabel policyNameLabel = new JLabel();
		policyNameLabel.setBounds(26, 31, 81, 27);
		policyNameLabel.setText("Policy Name :");
		getContentPane().add(policyNameLabel);
		
		inputFieldPolicyName = new JTextField();
		inputFieldPolicyName.setBounds(119, 31, 306, 27);
		inputFieldPolicyName.addKeyListener(escKeyListener);
		inputFieldPolicyName.addKeyListener(tipKeyListener);
		getContentPane().add(inputFieldPolicyName);
		
		defaultFileNameCheckBox = new JCheckBox();
		defaultFileNameCheckBox.setText("use default file name ( = policy name )");
		defaultFileNameCheckBox.setBounds(36, 64, 253, 25);
		defaultFileNameCheckBox.setSelected(true);
		defaultFileNameCheckBox.addActionListener(actionListener);
		getContentPane().add(defaultFileNameCheckBox);
		
		final JLabel fileNameLabel = new JLabel();
		fileNameLabel.setText("File Name :");
		fileNameLabel.setBounds(32, 95, 81, 27);
		getContentPane().add(fileNameLabel);

		inputFieldFileName = new JTextField();
		inputFieldFileName.setBounds(119, 95, 306, 27);
		inputFieldFileName.setEditable(false);
		getContentPane().add(inputFieldFileName);
		
		final JLabel IDLabel = new JLabel();
		IDLabel.setText("wsu : Id = ");
		IDLabel.setBounds(32, 128, 81, 27);
		getContentPane().add(IDLabel);
		
		inputFieldID = new JTextField();
		inputFieldID.setBounds(119, 128, 306, 27);
		inputFieldID.addKeyListener(escKeyListener);
		getContentPane().add(inputFieldID);
		

		final JLabel descriptionLabel = new JLabel();
		descriptionLabel.setBounds(32, 161, 81, 27);
		descriptionLabel.setText("Description : ");
		getContentPane().add(descriptionLabel);
		
		inputFieldDescription = new JTextField();
		inputFieldDescription.setBounds(119, 161, 306, 27);
		inputFieldDescription.addKeyListener(escKeyListener);
		getContentPane().add(inputFieldDescription);
		
		btOk = new JButton("OK");
		btOk.setBounds(231, 194, 93, 23);
		btOk.addActionListener(actionListener);
		btOk.setEnabled(false);
		getContentPane().add(btOk);

		btCancel = new JButton("CANCEL");
		btCancel.setBounds(332, 194, 93, 23);
		btCancel.addActionListener(actionListener);
		getContentPane().add(btCancel);
		
	}
	
	
    /**
     * der vom Benutzer eingegebene Dateiname, als default ist Dateiname
     * gleich wie Policyname, aber es ist auch erlaubt, dass der Benutzer 
     * einen anderen Name eintippen.
     */
	public String getFileName() {
		return fileName;
	}
	
    /**
     * der vom Benutzer eingegebene Policyname
     */
    public String getPolicyName() {
    	return inputFieldPolicyName.getText();
    }
    
    /**
     * das vom Benutzer eingegebene PolicyId
     */
    public String getID() {
    	return inputFieldID.getText();
    }
    
    /**
     * die vom Benutzer eingegebene Beschreibung
     */
    public String getDescription() {
        return inputFieldDescription.getText();
    }
    
    private void refreshTipTextField() {
		if (inputFieldFileName.getText().isEmpty()){
			tipLabel.setText("File name is empty.");
			tipLabel.setVisible(true);
			btOk.setEnabled(false);
		} else if (cause.exists(inputFieldFileName.getText()+ GUIConstants.POLICY_EXTENSION)){
			tipLabel.setText("The File already exists.");
			tipLabel.setVisible(true);
		    btOk.setEnabled(false);
		} else {
		    tipLabel.setVisible(false);
			btOk.setEnabled(true);
		} 		
    }
 
    private ActionListener actionListener = new ActionListener(){ 
    	public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == btOk) {
    			fileName = inputFieldFileName.getText();
    			dispose();
    		} else if (e.getSource() == btCancel){
    			fileName = null;
    			dispose();
    	    } else if (e.getSource() == defaultFileNameCheckBox){
    			if (defaultFileNameCheckBox.isSelected()) {
    				typedFileName = inputFieldFileName.getText();
    				inputFieldFileName.setEditable(false);
    				inputFieldFileName.removeKeyListener(tipKeyListener);
    				inputFieldFileName.removeKeyListener(escKeyListener);
    				inputFieldFileName.setText(inputFieldPolicyName.getText());
    			} else {
    				if (typedFileName == null){
    					typedFileName = inputFieldFileName.getText();
    				}
    				inputFieldFileName.setEditable(true);
    				inputFieldFileName.setText(typedFileName);
    				inputFieldFileName.addKeyListener(tipKeyListener);
    				inputFieldFileName.addKeyListener(escKeyListener);
    			}
    	    }
    	}
    };
	

private KeyListener tipKeyListener = new KeyListener(){

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (defaultFileNameCheckBox.isSelected()) {
			inputFieldFileName.setText(inputFieldPolicyName.getText());
		}
        refreshTipTextField();
		
	}

	public void keyTyped(KeyEvent e) {		
	}
};

private KeyListener escKeyListener = new KeyListener(){

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			fileName = null;
			dispose();
		}
	}

	public void keyTyped(KeyEvent e) {
	}
};

	
	
}
