package gui.util;

import gui.images.Images;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.w3c.policy.util.NSRegistry;

import util.XMLUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *  Diese Klasse bietet einen Inputdialog an, mit dem der Benutzer ein
 *  Attribut anlegen kann.
 * 
 * @author      Windy
 * @version     1.0
 */
public class NewAttrDialog extends JDialog{

	private static final long serialVersionUID = 4161907300522801787L;

	private JButton btOk;
	
	private JButton btCancel;
	
	private Prefix_Namespace_Selector pnSelector;
	
	private String attrName;
	
	private String attrValue;
	
	private String prefix;
	
	private String nsURI;
	
	private JLabel tipLabel;
	
	private JLabel attrNameLabel;
	
	private JLabel attrValueLabel;
	
	private JTextField nameInputField;
	
	private JTextField valueInputField;
	
	
	/**
	 * Create the dialog
	 */	
	public NewAttrDialog (String attrName, String attrValue, String prefix,
			String nsURI) {
		super();
		
		this.attrName = attrName;
		this.attrValue = attrValue;
		this.prefix = prefix;
		this.nsURI = nsURI;
		
		setBounds(100, 100, 464, 284);
		setTitle("Attribute Input Dialog");
		setModal(true);
		setDefaultLookAndFeelDecorated(true);
		getContentPane().setLayout(null);
		
		createWindow();
		
		refreshTipTextField();
	}
	
	private void createWindow(){
		
		tipLabel = new JLabel();
		tipLabel.setBounds(134, 0, 302, 32);
		tipLabel.setIcon(Images.ICON_ERROR);
		tipLabel.setVisible(false);
		getContentPane().add(tipLabel);

		attrNameLabel = new JLabel();
		attrNameLabel.setText(" Attribute Name : ");
		attrNameLabel.setBounds(18, 41, 110, 26);
		getContentPane().add(attrNameLabel);

		nameInputField = new JTextField();
		nameInputField.setBounds(134, 38, 302, 32);
		nameInputField.setEditable(true);
		nameInputField.setText(attrName);
		attrName = null;
		nameInputField.addKeyListener(tipKeyListener);
		getContentPane().add(nameInputField);
		
		attrValueLabel = new JLabel();
		attrValueLabel.setText(" Attribute Value :");
		attrValueLabel.setBounds(18, 79, 110, 26);
		getContentPane().add(attrValueLabel);
		
		valueInputField = new JTextField();
		valueInputField.setBounds(134, 76, 302, 32);
		valueInputField.setEditable(true);
		valueInputField.setText(attrValue);
		attrValue = null;
		valueInputField.addKeyListener(tipKeyListener);
		getContentPane().add(valueInputField);
		
		pnSelector = new Prefix_Namespace_Selector(prefix, nsURI);
		prefix = null;
		nsURI = null;
		
		pnSelector.setBounds(0, 114, 450, 90);
		pnSelector.getPrefixComboBox().addActionListener
		        (pnSelectorActionListener);
		pnSelector.getNamespaceComboBox().addActionListener
		        (pnSelectorActionListener);
		((JTextField) pnSelector.getPrefixComboBox().getEditor()
				.getEditorComponent()).addKeyListener(tipKeyListener);
		((JTextField) pnSelector.getNamespaceComboBox().getEditor()
				.getEditorComponent()).addKeyListener(tipKeyListener);
		getContentPane().add(pnSelector);

		
		btOk = new JButton("OK");
		btOk.setBounds(257, 215, 82, 23);
		btOk.addActionListener(actionListener);
		btOk.setEnabled(false);
		getContentPane().add(btOk);

		btCancel = new JButton("CANCEL");
		btCancel.setBounds(356, 215, 82, 23);
		btCancel.addActionListener(actionListener);
		getContentPane().add(btCancel);
	}
	

     private void refreshTipTextField() {
    	 if (nameInputField.isEditable() && nameInputField.getText()
    			 .isEmpty()){
				tipLabel.setText("Attribute name is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else if (nameInputField.isEditable() && !XMLUtil.isLegalNCName
					(nameInputField.getText())){
				tipLabel.setText("The name you have entered is an illegal" +
						" name.");
				tipLabel.setVisible(true);
			    btOk.setEnabled(false);
			} else if (valueInputField.isEditable() && valueInputField
					.getText().isEmpty()){
				tipLabel.setText("Attribute value is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else if (pnSelector.isEditable() && pnSelector.getPrefix()
					.isEmpty()){
				tipLabel.setText("Prefix is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else if (pnSelector.isEditable() && pnSelector.getNsURI()
					.isEmpty()){
				tipLabel.setText("Namespace URI is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else {
			    tipLabel.setVisible(false);
				btOk.setEnabled(true);
			} 		
			
	    } 
	
	
     /**
      * der vom Benutzer eingegebene Attributname
      */
     public String getAttrName(){
	  	return attrName;
	 }
	
     /**
      * der vom Benutzer eingegebene Wert des Attributs
      */
      public String getAttrValue(){
  		return attrValue;
	  }
      
     /**
      * das vom Benutzer ausgewaehlte Praefix
      */
	 public String getPrefix(){
		return prefix;
	 }
	
     /**
      * das vom Benutzer ausgewaehlte NamespaceURI
      */
	 public String getNsURI(){
		return nsURI;
	 }


      /**
       * Stellt, ob der Benutzer den Inputfeld von Attributname aendern darf 
       */  
	public void setNameInputEditabale(boolean editable){
		nameInputField.setEditable(editable);
		pnSelector.setSelectorEditable(editable);
		refreshTipTextField();
	}
	
    /**
     * Stellt, ob der Benutzer den Inputfeld von Attributwert aendern darf 
     */
	public void setValueInputEditabale(boolean editable){
		valueInputField.setEditable(editable);
		refreshTipTextField();
	}
	
	private KeyListener tipKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
				dispose();
			}else {
				refreshTipTextField();
			}
		}
		
		public void keyTyped(KeyEvent e) {		
		}
	};
	
	private ActionListener pnSelectorActionListener = new ActionListener(){

		public void actionPerformed(ActionEvent arg0) {
			refreshTipTextField();
		}
		
	};
	
	private ActionListener actionListener = new ActionListener (){
		public void actionPerformed(ActionEvent e) {
		    if (e.getSource() == btOk) {
		    	attrName = nameInputField.getText();
		    	prefix = pnSelector.getPrefix();
		    	nsURI = pnSelector.getNsURI();
		    	attrValue = valueInputField.getText();
		    	NSRegistry.check(prefix, nsURI);
			    dispose();
		    } else if (e.getSource() == btCancel){
			    dispose();
		    }
	    }
	};

	
	
}