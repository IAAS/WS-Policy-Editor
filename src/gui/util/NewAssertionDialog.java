package gui.util;

import gui.explorer.AssertionExplorer;
import gui.images.Images;

import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import util.XMLUtil;

/**
 *  Diese Klasse bietet einen Inputdialog an, mit dem der Benutzer eine
 *  Assertion mit den benoetigten Attribute anlegen kann.
 * 
 * @author      Windy
 * @version     1.0
 */
public class NewAssertionDialog  extends JDialog{
	
	private static final long serialVersionUID = 8712680335095577395L;
	
	private JTextField inputFieldDescription;
	
	private JTextField inputFieldAssertionName;
	
	private JTextField inputFieldFileName;
	
	private JCheckBox defaultFileNameCheckBox;
	
	private JLabel tipLabel;
	
	private JButton btOk;
	
	private JButton btCancel;
	
	private Prefix_Namespace_Selector pnSelector;
	
	private String prefix;
	
	private String nsURI;
	
	private String typedFileName;
	
	private AssertionExplorer cause;

	/**
	 * Create the dialog
	 */
	public NewAssertionDialog (AssertionExplorer cause, String defaultPrefix, String defaultNsURI) {
		super();
		getContentPane().setLayout(null);
		this.cause = cause;
		prefix = defaultPrefix;
		nsURI = defaultNsURI;
		setTitle("input dialog");
		setModal(true);
		setDefaultLookAndFeelDecorated(true);
		setBounds(100, 100, 464, 333);
		
        createWindow();

	}

	
	private void createWindow(){
		
		tipLabel = new JLabel();
		tipLabel.setBounds(126, 0, 299, 25);
		tipLabel.setText("Assertion name is empty.");
		tipLabel.setIcon(Images.ICON_ERROR);
        tipLabel.setVisible(true);
		getContentPane().add(tipLabel);

		final JLabel assertionNameLabel = new JLabel();
		assertionNameLabel.setBounds(17, 32, 103, 27);
		assertionNameLabel.setText("Assertion Name :");
		getContentPane().add(assertionNameLabel);
		
		inputFieldAssertionName = new JTextField();
		inputFieldAssertionName.setBounds(126, 31, 299, 27);
		inputFieldAssertionName.setFont(new Font("", Font.PLAIN, 14));
		inputFieldAssertionName.addKeyListener(tipKeyListener);
		inputFieldAssertionName.addKeyListener(escKeyListener);
		getContentPane().add(inputFieldAssertionName);
		
		pnSelector = new Prefix_Namespace_Selector(prefix, nsURI);
		prefix = null;
		nsURI = null;
		pnSelector.setBounds(0, 64, 450, 90);
		pnSelector.getPrefixComboBox().addActionListener(pnSelectorActionListener);
		pnSelector.getNamespaceComboBox().addActionListener(pnSelectorActionListener);
		((JTextField) pnSelector.getPrefixComboBox().getEditor().getEditorComponent()).addKeyListener(tipKeyListener);
		((JTextField) pnSelector.getPrefixComboBox().getEditor().getEditorComponent()).addKeyListener(escKeyListener);
		((JTextField) pnSelector.getNamespaceComboBox().getEditor().getEditorComponent()).addKeyListener(tipKeyListener);
		((JTextField) pnSelector.getNamespaceComboBox().getEditor().getEditorComponent()).addKeyListener(escKeyListener);
		getContentPane().add(pnSelector);

		defaultFileNameCheckBox = new JCheckBox();
		defaultFileNameCheckBox.setBounds(17, 160, 408, 23);
		defaultFileNameCheckBox.setText("use the default file name ( = Prefix_AssertionName )");
		defaultFileNameCheckBox.setSelected(true);
		defaultFileNameCheckBox.addActionListener(actionListener);
		getContentPane().add(defaultFileNameCheckBox);
		
		final JLabel fileNameLabel = new JLabel();
		fileNameLabel.setText("    File Name : ");
		fileNameLabel.setBounds(0, 189, 99, 27);
		getContentPane().add(fileNameLabel);
		
		inputFieldFileName = new JTextField();
		inputFieldFileName.setBounds(101, 189, 293, 27);
		inputFieldFileName.setEditable(false);
		getContentPane().add(inputFieldFileName);
		
		final JLabel descriptionLabel = new JLabel();
		descriptionLabel.setBounds(0, 222, 99, 27);
		descriptionLabel.setText("    Description : ");
		getContentPane().add(descriptionLabel);
		
		inputFieldDescription = new JTextField();
		inputFieldDescription.setBounds(101, 221, 293, 27);
		inputFieldDescription.setFont(new Font("Dialog", Font.PLAIN, 14));
		inputFieldDescription.addKeyListener(escKeyListener);
		getContentPane().add(inputFieldDescription);
		
		btOk = new JButton("OK");
		btOk.setBounds(232, 261, 93, 23);
		btOk.addActionListener(actionListener);
		btOk.setEnabled(false);
		getContentPane().add(btOk);

		btCancel = new JButton("CANCEL");
		btCancel.setBounds(331, 261, 93, 23);
		btCancel.addActionListener(actionListener);
		getContentPane().add(btCancel);
		
	}
	
	/**
	 * der vom Benutzer eingegebene Assertionname
	 */
    public String getAssertionName(){
    	return inputFieldAssertionName.getText();
    }
	
    /**
     * die vom Benutzer eingegebene Beschreibung
     */
    public String getDescription(){
        return inputFieldDescription.getText();
    }
    
    /**
     * das vom Benutzer ausgewaehlte Praefix
     */
    public String getPrefix(){
    	return this.prefix;
    }
    
    /**
     * das vom Benutzer ausgewaehlte Namespace
     */
    public String getNsURI(){
    	return this.nsURI;
    }
    
    /**
     * der vom Benutzer eingegebene Dateiname, als default ist Dateiname
     * "Prefix_Assertion", aber es ist auch erlaubt, dass der Benutzer einen
     * anderen Name eintippen.
     */
    public String getFileName(){
    	return inputFieldFileName.getText();
    }
    
    private void refreshTipTextField() {
		if (getAssertionName().isEmpty()){
			tipLabel.setText("Assertion name is empty.");
			tipLabel.setVisible(true);
			btOk.setEnabled(false);
		} else if (!XMLUtil.isLegalNCName(getAssertionName())){
			tipLabel.setText("The name you have entered is an illegal name.");
			tipLabel.setVisible(true);
		    btOk.setEnabled(false);
		} else if (pnSelector.getPrefix().isEmpty()){
			tipLabel.setText("Prefix is empty.");
			tipLabel.setVisible(true);
			btOk.setEnabled(false);
		} else if (pnSelector.getNsURI().isEmpty()){
			tipLabel.setText("Namespace URI is empty.");
			tipLabel.setVisible(true);
			btOk.setEnabled(false);
		} else if (getFileName().isEmpty()){
			tipLabel.setText("File name is empty.");
			tipLabel.setVisible(true);
			btOk.setEnabled(false);
		} else if (cause.exists(getFileName()+ GUIConstants.ASSERTION_EXTENSION)){
			tipLabel.setText("The File already exists.");
			tipLabel.setVisible(true);
		    btOk.setEnabled(false);
		} else {
		    tipLabel.setVisible(false);
			btOk.setEnabled(true);
		} 		
    }
    
    private void refreshFileNameTextField(){
		if (getAssertionName().isEmpty()){
			inputFieldFileName.setText("");
		} else if (pnSelector.getPrefix().isEmpty()){
			inputFieldFileName.setText(inputFieldAssertionName.getText());
		} else {
			inputFieldFileName.setText(pnSelector.getPrefix() + "_" + inputFieldAssertionName.getText());
		}
    }
    
    private ActionListener actionListener = new ActionListener(){ 
    	public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == btOk) {
    			prefix = pnSelector.getPrefix();
    			nsURI = pnSelector.getNsURI();
    			dispose();
    		} else if (e.getSource() == btCancel){
    			prefix = null;
    			nsURI = null;
    			dispose();
    		} else if (e.getSource() == defaultFileNameCheckBox){
    			if (defaultFileNameCheckBox.isSelected()) {
    				typedFileName = inputFieldFileName.getText();
    				inputFieldFileName.setEditable(false);
    				inputFieldFileName.removeKeyListener(tipKeyListener);
    				inputFieldFileName.removeKeyListener(escKeyListener);
    				refreshFileNameTextField();
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

		public void keyReleased(KeyEvent arg0) {
			if (defaultFileNameCheckBox.isSelected()) {
				refreshFileNameTextField();
			}
	        refreshTipTextField();
			
		}

		public void keyTyped(KeyEvent arg0) {		
		}
	};


	
	private KeyListener escKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
				prefix = null;
				nsURI = null;
				dispose();
			}
		}

		public void keyTyped(KeyEvent e) {
		}
	};

	private ActionListener pnSelectorActionListener = new ActionListener(){

		public void actionPerformed(ActionEvent arg0) {
			if (defaultFileNameCheckBox.isSelected()) {
				refreshFileNameTextField();
			}
	        refreshTipTextField();
		}
	};
	
	
}
