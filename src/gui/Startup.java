package gui;

import gui.editor.PolicyEditor;

import gui.util.GUIConstants;
import gui.util.GUIUtil;
import gui.util.M_FileChooser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import util.Environment;
import util.FileOperation;
import util.MOptionPanes;

/**
 * Um den WS-Policy Editor zu starten, wird die Klasse "Startup" als
 * Applikation gestartet. Die Klasse "Startup" fragt den Benutzer nach
 * den Pfaden fuer die Workspaces fuer Policies und Policy Assertions.
 * Dabei soll der Benutzer jeweils ein Verzeichnis im Dateisystem spezifizeren,
 * das als Wurzelverzeichnis in den jeweiligen Workspaces dient. Nachdem der
 * Benutzer die zwei Verzeichnisse spezifiziert hat, wird das Hauptfenster von
 * WS-Policy Editor geladen.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class Startup extends JFrame {

	private static final long serialVersionUID = -8143892430444043675L;

	private static Startup startupFrame;
		
	private String policyWSPath;
	
	private String assertionWSPath;
	
	private JFileChooser fileChooserPol;
	
	private JFileChooser fileChooserAss;

	private JTextField txtAssertionWS;

	private JTextField txtPolicyWS;
	
	/** 
	 * Klassenkonstruktor.
	 */
	public Startup() {
		super(GUIConstants.APPLICATION_NAME);

		getContentPane().setLayout(null);

		int width = 500;
		int height = 323;
		int x = GUIUtil.getStartPositionX(width);
		int y = GUIUtil.getStartPositionY(height);

		setBounds(x, y, width, height);
		
		final JTextPane descriptionTitel = new JTextPane();
		descriptionTitel.setFont(new Font("", Font.BOLD, 14));
		descriptionTitel.setText("Select Workspaces");
		descriptionTitel.setRequestFocusEnabled(false);
		descriptionTitel.setFocusable(false);
		descriptionTitel.setFocusCycleRoot(false);
		descriptionTitel.setEditable(false);
		descriptionTitel.setBounds(0, 0, 494, 30);
		getContentPane().add(descriptionTitel);

		final JTextPane description = new JTextPane();
		description.setRequestFocusEnabled(false);
		description.setFocusable(false);
		description.setFocusCycleRoot(false);
		description.setEditable(false);
		String text = "This editor stores your policies in a folder called a "
			+ "\"Policy Workspace\" and stores your assertions in a folder "
			+ "called \"Assertion Workspace\".\r\n\r\nChoose the workspaceDir"
			+ " folders to use for this session.";
		description.setText(text);
		description.setBounds(0, 29, 494, 72);
		getContentPane().add(description);
		
		readWorkspacePath();
		
		final JLabel lbPolicyWS = new JLabel();
		lbPolicyWS.setText("Policy Workspace:");
		lbPolicyWS.setBounds(10, 114, 195, 15);
		getContentPane().add(lbPolicyWS);

		txtPolicyWS = new JTextField(policyWSPath);
		txtPolicyWS.setAutoscrolls(false);
		txtPolicyWS.setBounds(8, 131, 370, 25);
		txtPolicyWS.setEditable(false);
		getContentPane().add(txtPolicyWS);

		fileChooserPol = M_FileChooser.getWorkspaceChooser(policyWSPath);

		final JButton btPolicyWS = new JButton();
		
		btPolicyWS.setText("Browse...");
		btPolicyWS.setBounds(391, 131, 85, 25);
		getContentPane().add(btPolicyWS);

		final JLabel lbAssertionWS = new JLabel();
		lbAssertionWS.setText("Assertion Workspace:");
		lbAssertionWS.setBounds(10, 170, 208, 15);
		getContentPane().add(lbAssertionWS);

		txtAssertionWS = new JTextField(assertionWSPath);
		txtAssertionWS.setAutoscrolls(false);
		txtAssertionWS.setBounds(8, 186, 370, 25);
		txtAssertionWS.setEditable(false);
		getContentPane().add(txtAssertionWS);
		fileChooserAss = M_FileChooser.getWorkspaceChooser(assertionWSPath);
		
		final JButton btAssertionWS = new JButton();
		
		btAssertionWS.setText("Browse...");
		btAssertionWS.setBounds(391, 186, 85, 25);
		getContentPane().add(btAssertionWS);

		final JSeparator separator = new JSeparator();
		separator.setBounds(0, 101, 494, 3);
		getContentPane().add(separator);

		final JButton btCancel = new JButton();
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeWindow();
			}
		});
		btCancel.setText("Cancel");
		btCancel.setBounds(391, 244, 85, 25);
		getContentPane().add(btCancel);

		final JButton btOK = new JButton();
		btOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadEditor();
			}
		});
		btOK.setText("OK");
		btOK.setBounds(298, 244, 85, 25);
		getContentPane().add(btOK);    	
		if (!areExistWorkspacePaths(false)) {
			btOK.setEnabled(false);
		}
		
		btPolicyWS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getWorkspaceDirPol(txtPolicyWS);
				if (!areExistWorkspacePaths(false)) {
					btOK.setEnabled(false);
				}
				else 
				{
					btOK.setEnabled(true);
				}
			}
		});
		
		btAssertionWS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getWorkspaceDirAss(txtAssertionWS);
				if (!areExistWorkspacePaths(false))  {
					btOK.setEnabled(false);
				} else {
					btOK.setEnabled(true);
				}
			}
		});		
	}

	private void closeWindow(){
		dispose();
	}
	
	private void readWorkspacePath(){
		try {		
			Environment.checkWorkspacePathFile();
			FileOperation pRead = new FileOperation();
			policyWSPath = pRead.readFile(Environment.
					getPolicyWorkspacePathFile());
			assertionWSPath = pRead.readFile(Environment.
					getAssertionWorkspacePathFile());	
		} catch (Exception e) {
			e.printStackTrace();
		}					
	}
	
	private void getWorkspaceDirPol(JTextField txtField) {
		if (fileChooserPol.showOpenDialog(this) != 
			    JFileChooser.APPROVE_OPTION){
			return;
		}

		File workspaceDir = fileChooserPol.getSelectedFile();

		if (workspaceDir == null) {
			return;
		}

		txtField.setText(workspaceDir.getPath());
	}

	private void getWorkspaceDirAss(JTextField txtField) {
		if (fileChooserAss.showOpenDialog(this) != 
			    JFileChooser.APPROVE_OPTION) {
			return;
		}

		File workspaceDir = fileChooserAss.getSelectedFile();

		if (workspaceDir == null) {
			return;
		}

		txtField.setText(workspaceDir.getPath());
	}

	private boolean areExistWorkspacePaths(boolean showError) {
		assertionWSPath = txtAssertionWS.getText();
		policyWSPath = txtPolicyWS.getText();

		File assertionWSDir = new File(assertionWSPath);
		File policyWSDir = new File(policyWSPath);

		if (!assertionWSDir.exists()) {
			if (showError){
				MOptionPanes.showError(this,
			    	"The path for \"Assertion Workspace\" does not exist.\n"
				    + "Please verify your input.");
			}
			return false;
		}

		if (!policyWSDir.exists()) {
			if (showError) {
			    MOptionPanes.showError(this,
			    	"The path for \"Policy Workspace\" does not exist.\n"
			    	+ "Please verify your input.");
			}
			return false;
		}

		return true;
	}

	private void loadEditor() {
		if (!areExistWorkspacePaths(true)) {
			return;
		}

		showPolicyEditor();
	}

	private void showPolicyEditor() {

		PolicyEditor frame = new PolicyEditor(policyWSPath, assertionWSPath);
	       
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
	   
		frame.setVisible(true);
		
		dispose();
	}

	/**
	 * Die "main" Methode startet das Programm
	 */
	public static void main(String args[]) {
		
		try {
			startupFrame = new Startup();
			startupFrame.addWindowListener(new WindowAdapter() {
				@Override
					public void windowClosing(WindowEvent e) {
						System.exit(0);					
					}
			});
			startupFrame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
