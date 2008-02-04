package util;


/**
 * Diese Klasse enthaelt alle nutzliche Methoden, mit denen einige Oprationen 
 * auf einer XML Datei ausgefuert werden koennen.
 * 
 * @author      Zhilei Ma
 * @version     1.0 
 *
 */
public class XMLUtil {

	public XMLUtil() {

	}

	
	/**
	 * Trennt das Praefix aus dem QName ab
	 * 
	 * @param input    der QName
	 * @return         das Praefix
	 */
	public static String getPrefix(String input) {
		/* Get the position of the first occurrence of ":" */
		int colonPosition = input.indexOf(":");
	 
		/* No colon was found */
		if (colonPosition == -1) {
			return null;
		} 
	
		return input.substring(0, colonPosition);
	}

	/**
	 * Trennt den Lokalname aus dem QName ab
	 * 
	 * @param input    der QName
	 * @return         der Lokalname
	 */
	public static String getLocalPart(String input) {
		/* Get the position of the first occurrence of ":" */
		int colonPosition = input.indexOf(":");
	
		/* No colon was found */
		if (colonPosition == -1) {
			return input;
		} 
	
		return input.substring(colonPosition + 1, input.length());
	}



	/**
	 * Ueberprueft, ob der gegebene Attributname gueltig ist.
	 * 
	 */
	public static boolean isLegalAttrName(String name) {
		String prefix = getPrefix(name);
		String localPart = getLocalPart(name);
		if (prefix == null) {
			if (isLegalNCName(localPart)) {
				return true;
			} 
			return false;
		}
		if (isLegalNCName(prefix) && isLegalNCName(localPart)) {
			return true;
		}
		return false;
	}
	

	/**
	 * Ueberprueft, ob das gegebene String einen gueltigen NCName ist.
	 */
	public static boolean isLegalNCName(String ncName) {
		if (ncName == null || ncName.length() == 0)
			return false;

		for (int k = 0; k < ncName.length(); k++) {
			char ch = ncName.charAt(k);
			if (Character.isLetter(ch)
					|| (ch == '_')
					|| (k > 0 && (Character.isDigit(ch) || (ch == '.') || (ch == '-'))))
				continue;
			return false;
		} 

		return true;
	} 

	/**
	 * Ueberprueft, ob der gegebene Qname gueltig ist.
	 */
	public static boolean isLegalQName(String qName) {
		String prefix = getPrefix(qName);
		String localPart = getLocalPart(qName);
		if (prefix == null) {
			return false;
		}
		if (isLegalNCName(prefix) && isLegalNCName(localPart)) {
			return true;
		}
		return false;
	}

}
