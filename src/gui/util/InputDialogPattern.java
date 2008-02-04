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
 * Diese Klasse repraesentiert ein Muster des Inputdialogs, mit dem der Name 
 * das Praefix und das NamespaceURI des Objekts vom Benutzer eingelesen 
 * koennen. Bei den verschiednen Faellen wird dieser Dialog verwendet : 
 * 1) legt ein neues Paket an.
 * 2) stellt das defaulten Namespace
 * 3) legt eine neue Assertion an
 * 4) aendert eine Assertion
 * 
 * @author      Windy
 * @version     1.0
 */
public class InputDialogPattern extends JDialog{

	private static final long serialVersionUID = 4161907300522801787L;

	private JButton btOk;
	
	private JButton btCancel;
	
	private Prefix_Namespace_Selector pnSelector;
	
	private String Value;
	
	private String prefix;
	
	private String nsURI;
	
	private JLabel tipLabel;
	
	private JLabel nameLabel;
	
	private JTextField textField;
	
	private boolean isPrefixIgnorable;
	

	/**
	 * Legt ein neues Inputdialog an, ohne die vorgegebenen Name, Praefix
	 * sowie NamespaceURI.
	 * 
	 * @param isPrefixIgnorable    false - das Objekt unbedingt ein Praefix 
	 *                                     haben muss
	 *                             true - ein Praefix ist nicht benotigt
	 */
	public InputDialogPattern (boolean isPrefixIgnorable) {
		super();
		this.isPrefixIgnorable = isPrefixIgnorable;
		setBounds(100, 100, 464, 235);
		setTitle("Input Dialog");
		setModal(true);
		setDefaultLookAndFeelDecorated(true);
		getContentPane().setLayout(null);
		createWindow();
	    refreshTipTextField();
	}
	
	/**
	 * Legt ein neues Inputdialog an, mit den vorgegebenen Name, Praefix
	 * sowie NamespaceURI.
	 * 
	 * @param isPrefixIgnorable    false - das Objekt unbedingt ein Praefix 
	 *                                     haben muss
	 *                             true - ein Praefix ist nicht benotigt
	 */
	public InputDialogPattern (String oldValue, String oldPrefix, 
			String OldNsURI, boolean isPrefixIgnorable) {
		super();
		Value = oldValue;
		prefix = oldPrefix;
		nsURI = OldNsURI;
		this.isPrefixIgnorable = isPrefixIgnorable;
		setBounds(100, 100, 464, 235);
		setTitle("Input Dialog");
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

		nameLabel = new JLabel();
		nameLabel.setBounds(18, 41, 110, 26);
		getContentPane().add(nameLabel);

		textField = new JTextField(Value);
		textField.setBounds(134, 38, 302, 32);
		textField.setEditable(true);
		textField.addKeyListener(tipKeyListener);
		getContentPane().add(textField);
		
		pnSelector = new Prefix_Namespace_Selector(prefix, nsURI);
		prefix = null;
		nsURI = null;
		
		pnSelector.setBounds(0, 73, 450, 90);
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
		btOk.setBounds(254, 169, 82, 23);
		btOk.addActionListener(actionListener);
		btOk.setEnabled(false);
		getContentPane().add(btOk);

		btCancel = new JButton("CANCEL");
		btCancel.setBounds(356, 169, 82, 23);
		btCancel.addActionListener(actionListener);
		getContentPane().add(btCancel);
	}
	
	   private void refreshTipTextField() {
			if (textField.getText().isEmpty()){
				tipLabel.setText("Input field is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else if (!XMLUtil.isLegalNCName(textField.getText())){
				tipLabel.setText("The name you have entered is an illegal " +
						"name.");
				tipLabel.setVisible(true);
			    btOk.setEnabled(false);
			} else if (pnSelector.getPrefix().isEmpty() && !isPrefixIgnorable){
				tipLabel.setText("Prefix is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else if (pnSelector.getNsURI().isEmpty() && !isPrefixIgnorable){
				tipLabel.setText("Namespace URI is empty.");
				tipLabel.setVisible(true);
				btOk.setEnabled(false);
			} else {
			    tipLabel.setVisible(false);
				btOk.setEnabled(true);
			} 		
	    }
	   
	/**
	 * Stellt den JLabel, was wird auf dem angezeigt.
	 * 
	 * @param name1   der Name von NameLabel
	 * @param name2   der Name von PrefixLabel
	 * @param name3   der Name von NamespactURILabel
	 */   
	public void setLabelTitel(String name1, String name2, String name3){
		nameLabel.setText(name1);
		pnSelector.setLabelTitle(name2, name3);
		
	}
	
	/**
	 * Stellt den Inputfeld aenderbar bzw. nicht aenderbar
	 */
	public void setInputFieldEditable(boolean b){
		textField.setEditable(b);
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
	 * der vom Benutzer eingegebene Wert im ersten Inputfeld
	 */
	public String getValue(){
		return textField.getText();
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
	
	private ActionListener actionListener = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    if (e.getSource() == btOk) {
		    	prefix = pnSelector.getPrefix();
		    	nsURI = pnSelector.getNsURI();
		    	NSRegistry.check(prefix, nsURI);
			    dispose();
		    } else if (e.getSource() == btCancel){
			    dispose();
		    }
		}
    };

	
	
}