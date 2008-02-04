package util;

/**
 * Diese Klasse sammelt alle eventuelle Meldungen
 * 
 * @author      Zhilei Ma
 * @version     1.0 
 *
 */
public class Messages {
	
	public static final String FILE_EXISTS = "The file you specified exists already."
			+ "\nPlease input a new name.";

	public static final String ILLEGAL_QNAME = "The name you have entered is an illegal name."
			+ " \nPlease input a new name";

	public static final String INPUT_ATTR_NAME = "Please enter new attribute name";

	public static final String INPUT_ATTR_VALUE = "Please enter a value for this attribute";

	public static final String INPUT_QNAME = "Please enter the name for the new assertion.";

	public static final String INPUT_TEXT_VALUE = "Please enter the text value of the assertion.";

	public static final String NO_CHANGE_ON_POLICY_NS = "Cannot change namespace of WS-Policy.";

	public static final String NO_FILE_FOUND = "Cannot find the file.";

	public static final String NO_NS_DEFINED = "The namespace of this assertion is not defined. \n"
			+ "Please enter the namespace URI for this assertion";

	public static final String NO_ROOT_DELETION = "Sorry, you cannot delete a root element.";

	public static final String NO_SAVE_EMPTY_DOC = "Cannot save a empty assertion."
			+ "\n Click \"Yes\" to delete this assertion."
			+ " \n Click \"No\" to continue working with this assertion.";

	public static final String NO_SCHEMA = "No schema is avaiable.";

	public static final String NULL_TREENODE = "Please selecte a element in the tree.";

	public static final String INPUT_PACKAGE_NAME = "Please enter the new package name.";

	public static String delWarning(String nodeName) {
		return "Are you sure you want to delete attribute '" + nodeName + "' ?";
	}

	public static String savePromption(String fileName) {
		return "Save changes to \"" + fileName + "\"?";
	}

	public static final String NO_POLICY_REFERENCE_ALLOWED = "Policy Reference can only be added as "
			+ "child element of \"Policy\", \"ExactlyOne\" or \"All\" operators.";
}
