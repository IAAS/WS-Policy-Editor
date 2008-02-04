package util;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Diese Klasse sammelt alle eventuelle OptionPanes
 * 
 * @author      Zhilei Ma
 * @version     1.0 
 *
 */
public class MOptionPanes {


	/**
	 * Liest die Bestaetigung vom Benuter ein
	 */
	public static int getComfirmResult(Component owner, String msg) {
		return JOptionPane.showConfirmDialog(owner, msg, "Confirmation Dialog",
				JOptionPane.YES_NO_OPTION);
	}

	/**
	 * Liest einige benoetigte Informationen vom Benutzer ein
	 */
	public static String getInput(Component owner, String msg,
			String initialValue) {
		Object obj=  JOptionPane.showInputDialog(owner, msg, "Input Dialog",
				JOptionPane.QUESTION_MESSAGE, null, null, initialValue);

		while (obj != null && obj.equals("")) {
			obj = JOptionPane.showInputDialog(owner, msg, "Input Dialog",
					JOptionPane.QUESTION_MESSAGE, null, null, initialValue);
		}
		
		if (obj != null) {
			return obj.toString();
		}
		
		return null;
	}

	/**
	 * Liest das Namespace URI fuer ein Praefix ein.
	 * 
	 */
	public static String getNamespaceURI(Component owner, String initialValue) {

		String input = getInput(owner, Messages.NO_NS_DEFINED, initialValue);
		while (input != null && input.equals("")) {
			input = getReinput(owner, Messages.NO_NS_DEFINED);
		}

		return input;
	}


	/**
	 * Liest einige benoetigte Informationen vom Benutzer noch mal ein
	 */
	public static String getReinput(Component owner, String msg) {
		return JOptionPane.showInputDialog(owner, msg, "Input Dialog",
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Gibt eine Fehlermeldung aus.
	 */
	public static void showError(Component owner, String message) {
		JOptionPane.showMessageDialog(owner, message, "Warning",
				JOptionPane.WARNING_MESSAGE);
	}

}
