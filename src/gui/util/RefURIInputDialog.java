package gui.util;

import gui.editor.PolicyEditor;

import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 *  Diese Klasse bietet einen Inputdialog an, mit dem der Benutzer ein
 *  PolicyReference anlegen kann.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class RefURIInputDialog extends JDialog {


	private static final long serialVersionUID = 7944902125839301212L;

	private JTextField textField;
	
	private PolicyEditor owner;
	
	private JTextPane descriptionTextPane;

	public RefURIInputDialog(Frame owner, final String oldURI)
			throws HeadlessException {
		super(owner, GUIConstants.APPLICATION_NAME, true);

		this.owner = (PolicyEditor) owner;

		getContentPane().setLayout(null);
		int width = 500;
		int height = 226;
		int startPositionX = GUIUtil.getStartPositionX(width);
		int startPositionY = GUIUtil.getStartPositionY(height);

		setBounds(startPositionX, startPositionY, 404, 224);

		final JTextPane titelTextPane = new JTextPane();
		titelTextPane.setFont(new Font("", Font.BOLD, 16));
		titelTextPane.setText("Specify Policy Reference URI");
		titelTextPane.setRequestFocusEnabled(false);
		titelTextPane.setFocusable(false);
		titelTextPane.setFocusCycleRoot(false);
		titelTextPane.setEditable(false);
		titelTextPane.setBounds(0, 0, 494, 30);
		getContentPane().add(titelTextPane);

		descriptionTextPane = new JTextPane();
		descriptionTextPane.setRequestFocusEnabled(false);
		descriptionTextPane.setFocusable(false);
		descriptionTextPane.setFocusCycleRoot(false);
		descriptionTextPane.setEditable(false);
		descriptionTextPane
				.setText("You can enter a resolvable URI or the file path of the referenced policy");
		descriptionTextPane.setBounds(0, 30, 494, 42);
		getContentPane().add(descriptionTextPane);

		final JSeparator separator = new JSeparator();
		separator.setBounds(0, 72, 494, 3);
		getContentPane().add(separator);

		textField = new JTextField();
		textField.setBounds(8, 91, 370, 25);
		textField.setText(oldURI);
		textField.addKeyListener(inputKeyListener);
		getContentPane().add(textField);

		final JButton btOK = new JButton();
		btOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textField.getText().equals(oldURI)){
					setPolicyReferenceURI(null);
				} else {
			        setPolicyReferenceURI(textField.getText());
				}
				dispose();
			}
		});
		btOK.setText("OK");
		btOK.setBounds(199, 145, 85, 25);
		getContentPane().add(btOK);

		final JButton btCancel = new JButton();
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPolicyReferenceURI(null);
				dispose();
			}
		});
		btCancel.setText("Cancel");
		btCancel.setBounds(290, 145, 85, 25);
		getContentPane().add(btCancel);

		ExitListener exitListener = new ExitListener();
		this.addWindowListener(exitListener);
	}

	private void setPolicyReferenceURI(String uri) {
		owner.setPolicyReferenceURI(uri);
	}
	
	private KeyListener inputKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {	
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				setPolicyReferenceURI(textField.getText());
				dispose();
				break;
			case KeyEvent.VK_ESCAPE:
				setPolicyReferenceURI(null);
				dispose();
				break;
			}
		}

		public void keyReleased(KeyEvent arg0) {			
		}

		public void keyTyped(KeyEvent arg0) {			
		}
		
	};
	

}
