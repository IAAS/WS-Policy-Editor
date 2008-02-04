package util;

import gui.util.GUIConstants;
import gui.util.ListFileFilter;
import gui.util.M_FileFilter;

import java.awt.Frame;
import java.awt.List;
import java.io.File;
import java.io.FileFilter;

import javax.swing.JOptionPane;

/**
 * Diese Klasse enthaelt alle nutzliche Methoden, mit denen einige Oprationen 
 * im System ausgefuert werden koennen.
 * 
 * @author      Zhilei Ma
 * @version     1.0 
 *
 */
public class IOUtils {
	
	private static final ListFileFilter assertionFilter = M_FileFilter
			.getListAssertionFilter();

	private static final ListFileFilter policyFilter = M_FileFilter
			.getListPolicyFilter();

	/**
	 * Entfernt einen Ordner aus dem System
	 * 
	 * @param aDir    der Ordner, der entfernt werden soll
	 * @return        ob diese Operation gelungen ist.
	 */
	public static boolean deleteDir(File aDir) {	
		
		if (aDir == null || aDir.isHidden()) {
			JOptionPane.showMessageDialog(Frame.getFrames()[0],
					"Cannot delete hidden directories.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			File[] files = aDir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File iDir = files[i];
					if (iDir.isDirectory()) {
						deleteDir(iDir);
					} else {
						iDir.delete();
					}
				}
			}
			if (aDir.delete()) {
				return true;
			}
			JOptionPane.showMessageDialog(Frame.getFrames()[0],
					"Failed to delete \ndirectory \"" + aDir.getName() + "\"",
					"Error", JOptionPane.ERROR_MESSAGE);

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Frame.getFrames()[0], e.toString(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return false;
	}

	/**
	 * Entfernt eine Datei aus dem System
	 * 
	 * @param aFile   die Datei, die entfernt werden soll
	 * @return        ob diese Operation gelungen ist.
	 */
	public static boolean deleteFiles(File aFile) {

		if (aFile == null || aFile.isHidden()) {
			JOptionPane.showMessageDialog(Frame.getFrames()[0],
					"Cannot delete hidden files.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			if (aFile.isDirectory()) {
				deleteDir(aFile);
				return true;
			} else {
				if (aFile.delete()) {
					return true;
				}
				JOptionPane.showMessageDialog(Frame.getFrames()[0],
						"Failed to delete \nfile \"" + aFile.getName() + "\"",
						"Error", JOptionPane.ERROR_MESSAGE);
				}		
		} catch (Exception e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"Error by deleting the file.");
		}
		return false;
	}

	/**
	 * Fasst alle Assertions unter einem bestimmten Ordner zusammen,
	 * dann als eine Liste zurueckgeben
	 */
	public static List listAssertions(File dir) {
		return getSortedFileList(dir, assertionFilter);
	}

	/**
	 * Fasst alle Policies unter einem bestimmten Ordner zusammen,
	 * dann als eine Liste zurueckgeben
	 */
	public static List listPolicies(File dir) {
		return getSortedFileList(dir, policyFilter);
	}

	/**
	 * Returns a List with all directories appear before the files.
	 */
	public static List getSortedFileList(File aFile, FileFilter aFilter) {
		File[] files = listFiles(aFile, aFilter);
		
		List fileList = new List();

		if (files == null) {
			return null;
		}
		
		/* Firstly sort the fileList */		
		int start = 0;
		int r = files.length - 1;		
		int p, q;
		
		for (p = start; p < r; p++) {
			for (q = r; q > p; q--) {
				if ((files[q-1].getName()).
						compareToIgnoreCase(files[q].getName()) > 0) { 
					File Temp = files[q-1];
					files[q-1] = files[q];
					files[q] = Temp;
				}
			}
		}
		
		/* Secondly add the directories to the fileList */
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				fileList.add(files[i].toString());
			}
		}

		/* Finally add the files to the fileList */
		for (int j = 0; j < files.length; j++) {
			if (files[j].isFile()) {
				fileList.add(files[j].toString());				
			}
		}
		
		return fileList;
	}

	/**
	 * Ermittelt, ob unter einem Ordner eine Assertion exsitiert.
	 * 
	 */
	public static boolean hasAssertion(File dir) {
		return hasChildrenFile(dir, assertionFilter);
	}

	/**
	 * Ermittelt, ob unter einem Ordner eine Policy exsitiert.
	 * 
	 */
	public static boolean hasPolicy(File dir) {
		return hasChildrenFile(dir, policyFilter);
	}

	/**
	 * Return true, if a directory has at least one file as child.
	 */
	public static boolean hasChildrenFile(File aDir, FileFilter fileFilter) {
		if (!aDir.isDirectory()) {
			return false;
		}

		File[] files = listFiles(aDir, fileFilter);

		if (files == null || files.length == 0) {
			return false;
		}

		return true;
	}

	/**
	 * Return a array of files of a directory
	 */
	protected static File[] listFiles(File aFile, FileFilter aFilter) {
		if (!aFile.isDirectory()) {
			return null;
		}

		try {
			return aFile.listFiles(aFilter);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(Frame.getFrames()[0],
					"Error reading directory '" + aFile.getAbsolutePath()
							+ "'!", GUIConstants.APPLICATION_NAME,
					JOptionPane.WARNING_MESSAGE);
			return null;
		}

	}

}
