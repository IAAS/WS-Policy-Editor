
package gui.util;

/**
 * Verwaltet alle DateiFilter.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class M_FileFilter {

	/**
	 * Der Dateifilter, der speziell fuer Assertions ist.
	 * 
	 * @return   der entsprechende Filter von Type "ChooserFileFilter" 
	 * @see      #getChoosePolicyFilter()
	 */
	public static ChooserFileFilter getChooseAssertionFilter() {
		ChooserFileFilter assertionFileFilter = new ChooserFileFilter(
				GUIConstants.ASSERTION_EXTENSION, "Assertion Files");

		return assertionFileFilter;
	}

	/**
	 * Der Dateifilter, der speziell fuer Policies ist.
	 * 
	 * @return   der entsprechende Filter von Type "ChooserFileFilter" 
	 * @see      #getChooseAssertionFilter()
	 */
	public static ChooserFileFilter getChoosePolicyFilter() {
		ChooserFileFilter policyFileFilter = new ChooserFileFilter(
				GUIConstants.POLICY_EXTENSION, "XML Files");

		return policyFileFilter;
	}

	/**
	 * Der Dateifilter, der speziell fuer Assertions ist.
	 * 
	 * @return   der entsprechende Filter von Type "ListFileFilter" 
	 * @see      #getListPolicyFilter()
	 */
	public static ListFileFilter getListAssertionFilter() {
		return new ListFileFilter(GUIConstants.ASSERTION_EXTENSION);
	}

	/**
	 * Der Dateifilter, der speziell fuer Policies ist.
	 * 
	 * @return   der entsprechende Filter von Type "ListFileFilter" 
	 * @see      #getListPolicyFilter()
	 */
	public static ListFileFilter getListPolicyFilter() {
		return new ListFileFilter(GUIConstants.POLICY_EXTENSION);
	}

}
