package util;

import java.util.regex.Pattern;

/**
 * Diese Klasse enthaelt alle nutzliche Methoden, mit denen einige Oprationen 
 * auf String ausgefuert werden koennen.
 * 
 * @author      Zhilei Ma
 * @version     1.0 
 *
 */
public class StringUtil {

	public StringUtil() {
	}

	/**
	 * Trennt die Endung von dem Dateiname ab.
	 * 
	 * @param fileName   der gegebene Dateiname
	 * @return           die Endung
	 */
	public static String getExtension(String fileName) {
		int seperator = fileName.lastIndexOf(".");

		if (seperator == -1) {
			return null;
		} 
 
		return fileName.substring(seperator, fileName.length());
	}



	/**
	 * All withespaces which appears adjecent more than one time will be
	 * replaced through one whitespace. \s represents a whitespace
	 * character: [ \t\n\x0B\f\r] \\s{1,}+ meas this pattern must appers
	 * more than one time.
	 */
	public static String normalizeString(String str) {
		Pattern p = Pattern.compile("\\s{1,}+");
		String string = str.replaceAll(p.toString(), " ");
		if (string.startsWith(" ")) {
			string = string.substring(1);
		}
		
		return string;
	} 

	/**
	 * bringt ein Sting in Ordnung
	 */
	public static String trimString(String str) {
		Pattern p = Pattern.compile("\\s{1,}+");
		return str.replaceAll(p.toString(), "");
	} 

	/**
	 * falls ein String laenger als die gewuenschte Laenge ist, entfernt den
	 * ueberfluessigen Teil und fuegt am End "..." hinzu
	 * 
	 * @param str       das String, die Ueberprueft werden soll
	 * @param length    die gewuenschte Laenge
	 * @return          das nachher enstehende String
	 */
	public static String truncString(String str, int length) {
		if (str.length() > length) {
			str = str.substring(0, length) + "...";
			return str;
		} 
		return str;
	}

	/**
	 * jede Zeile duerfen hoechstens 30 Zeichen enthaelt, die ueberfluessige
	 * Zeichen werden in der naechsten Zeile verteilt.
	 */
	public static String wrapString(String str) {
		String result = null;
		int j;
		if (str.length() > 30) {
			j = 30;
		} 
		j = str.length();
		for (int i = 0; i < str.length(); i += j) {
			result = str.substring(i, j) + "\n";
		} 
		return result;
	}

}
