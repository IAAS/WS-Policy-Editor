package gui.util;

import java.io.File;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import util.StringUtil;

/**
 * Diese Klasse repraesentiert einen Dateifilter, mit dem alle Dateien unter 
 * WorkSpacePath gefiltert werden koennen, nur die Dateien mit den
 * erlaubten Endungen duerfen im Baum angezeigt werden.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * 
 */
public class ChooserFileFilter extends FileFilter {
	/* The description of this filter. */
	private String description;

	/* Use to store allowed extensions */
	private Vector<String> extensionVector = new Vector<String>();
	
    /**
     * Legt einen neuen Dateifilter ohne Beschreibung an und die erlaubte Endungen
     * sind nicht spezifiziert.
     *
     */
	public ChooserFileFilter() {
	}

    /**
     * Legt einen neuen Dateifilter mit Beschreibung an aber die erlaubte Endungen
     * sind nicht spezifiziert.
     *
     */
	public ChooserFileFilter(String description) {
		this.description = description;
	}
	
    /**
     * Legt einen neuen Dateifilter mit Beschreibung an und die erlaubte Endungen
     * sind gleichzeitig spezifiziert.
     *
     */
	public ChooserFileFilter(String fileExtension, String description) {
		extensionVector.add(fileExtension);
		this.description = description;

	}



	/**
	 * Fuegt eine erlaubte Endung hinzu
	 */
	public void addExtension(String extension) {
		if (!extensionVector.contains(extension)) {
			extensionVector.add(extension);
		}
	}

	/**
	 * ob die Datei eine erlaubte Endung besitzt
	 * 
	 * 
	 * @param file   die Datei, die ueberprueft werden soll
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {

		if (file.isDirectory() && !(file.getName().startsWith("."))) {
			return true;
		}

		String extension = StringUtil.getExtension(file.getName());

		if (extension == null) {
			return false;
		}

		if (extensionVector.contains(extension.toLowerCase())) {
			return true;
		}

		return false;
	}

	/**
	 * Die Beschreibung dieses Filters
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

}
