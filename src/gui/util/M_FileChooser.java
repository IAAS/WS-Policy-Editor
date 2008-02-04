package gui.util;

import javax.swing.JFileChooser;

/**
 * Bietet dem benutzer die Moeglichkeit, eine Datei aus Dateisystem auszuwaehlen
 * 
 * @author      Zhilei Ma
 * @version     1.0
 *
 */
public class M_FileChooser {

	private static final long serialVersionUID = 88820559679687269L;

	/**
	 * legt einen Dateiwaehler an, der speziell fuer Wahl einer Assertion Datei ist.
	 * 
	 */
	public static JFileChooser getAssertionFileChooser(String currentDirectoryPath) {
		JFileChooser fileChooser;
		if (currentDirectoryPath == null) {
			fileChooser = new JFileChooser(".");
		} else {
			fileChooser = new JFileChooser(currentDirectoryPath);
		}

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		fileChooser.setFileFilter(M_FileFilter.getChooseAssertionFilter());

		return fileChooser;
	}
	
	/**
	 * legt einen Dateiwaehler an, der speziell fuer Wahl einer Policy Datei ist.
	 * 
	 */
	public static JFileChooser getPolicyFileChooser(String currentDirectoryPath) {
		JFileChooser fileChooser;
		if (currentDirectoryPath == null) {
			fileChooser = new JFileChooser(".");
		} else {
			fileChooser = new JFileChooser(currentDirectoryPath);
		}

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		fileChooser.setFileFilter(M_FileFilter.getChoosePolicyFilter());

		return fileChooser;
	}

	/**
	 * legt einen Dateiwaehler an, der speziell fuer Workspace ist.
	 * 
	 */
	public static JFileChooser getWorkspaceChooser(String currentDirectoryPath) {
		JFileChooser fileChooser;
		if (currentDirectoryPath == null) {
			fileChooser = new JFileChooser(".");
		} else {
			fileChooser = new JFileChooser(currentDirectoryPath);
		}

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		return fileChooser;
	}

}
