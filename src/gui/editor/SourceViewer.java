package gui.editor;

import gui.util.GUIConstants;
import gui.util.GUIUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.policy.Policy;
import org.w3c.policy.PrimitiveAssertion;
import org.w3c.policy.util.PolicyReader;
import org.w3c.policy.util.PolicyWriter;

import util.MOptionPanes;
import util.StringUtil;

/**
 * Bietet dem Benutzer die Moeglichkeit an, den Quellecode einer Policy 
 * oder einer PolicyAssertion anzeigen zu lassen. 
 *  
 * @author      Zhilei Ma
 * @version     1.0
 */
public class SourceViewer extends JDialog {

	private JTextArea textArea;

	private File file;

	private static final String APP_NAME = GUIConstants.APPLICATION_NAME
			+ " - Source Viewer";
	
	private Component owner;
	
	private static final long serialVersionUID = -3280978766843670378L;

	/**
	 * Legt ein neues "SourceViewer" fuer eine Policy an.
	 */
	public SourceViewer(JFrame frame, File file) {
		super(frame, APP_NAME, true);
		this.owner = frame;
		this.file = file;
		createSourceViewer();
	}

	/**
	 * Legt ein neues "SourceViewer" fuer eine Assertion an.
	 */
	public SourceViewer(JDialog dialog, File file) {
		super(dialog, APP_NAME, true);
		this.owner = dialog;
		this.file = file;
		createSourceViewer();
	}

	private void createSourceViewer() {
		int width = GUIUtil.getDefaultWindowWidth() * 2 / 3;
		int height = GUIUtil.getDefaultWindowHeight() * 2 / 3;
		int x = GUIUtil.getStartPositionX(width);
		int y = GUIUtil.getStartPositionY(height);

		String extension = StringUtil.getExtension(file.getName());

		if (extension.equals(GUIConstants.POLICY_EXTENSION)) {
			Policy policy = PolicyReader.parsePolicy(file);
			PolicyWriter writer = new PolicyWriter(file);
	     	writer.writePolicy(policy);
		} else if (extension.equals(GUIConstants.ASSERTION_EXTENSION)) {
			PrimitiveAssertion assertion = PolicyReader
					.parseAssertion(file);
			PolicyWriter writer = new PolicyWriter(file);
			writer.writePrimitiveAssertion(assertion);
		}

		setBounds(x, y, width, height);

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(textArea);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		readFile();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	/**
	 * 
	 */
	private void readFile() {
		try {
			
			BufferedReader input = new BufferedReader(new FileReader(file));

			String line;

			while ((line = input.readLine()) != null) {
				textArea.append(line);
				textArea.append(System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			MOptionPanes.showError(owner,"File was not found");
		} catch (IOException ex) {
			MOptionPanes.showError(owner,"I/O Exception: " + ex.getMessage());
			ex.printStackTrace();
		}

	}

}
