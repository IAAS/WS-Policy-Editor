package gui.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Vector;

import util.StringUtil;

/**
 * Diese Klasse repraesentiert einen Dateifilter, mit dem alle Dateien unter 
 * WorkSpacePath gefiltertet werden koennen, nur die Dateien mit den
 * erlaubten Endungen duerfen im Baum anzeigen.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * 
 */
public class ListFileFilter implements FileFilter {

	/* Use to store allowed extensions */
	private Vector<String> extensionVector = new Vector<String>();

    /**
     * Legt einen neuen Dateifilter und die erlaubte Endungen
     * wird nicht spezifiziert.
     *
     */
	public ListFileFilter() {
	}

    /**
     * Legt einen neuen Dateifilter und die erlaubte Endungen
     * wird gleichzeitig spezifiziert.
     *
     */
	public ListFileFilter(String fileExtension) {
		extensionVector.add(fileExtension);
	}

	/**
	 * ob die Datei eine erlaubte Endung besitzt
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		if (pathname.isDirectory() && !(pathname.getName().startsWith("."))) {
			return true;
		}

		String extension = StringUtil.getExtension(pathname.getName());

		if (extension == null) {
			return false;
		}

		if (extensionVector.contains(extension.toLowerCase())) {
			return true;
		}

		return false;
	}

	/**
	 * Fuegt eine erlaubte Endung hinzu
	 */
	public void addExtension(String extension) {
		if (!extensionVector.contains(extension)) {
			extensionVector.add(extension);
		}
	}

}
