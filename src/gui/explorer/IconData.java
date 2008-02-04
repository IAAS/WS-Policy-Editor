package gui.explorer;
 
import javax.swing.Icon;

/**
 * Diese Klasse repraesentiert ein Ikon, das im Baum vor dem Anzeigen des Knotens 
 * das gebenenes Data speichert.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class IconData {
	
	private Icon m_icon;

	private Icon m_expandedIcon;

	private Object m_data;

	/**
	 * Legt ein neues Ikon mit dem bestimmten Data an.
	 * 
	 * @param icon   das angezeigte Ikon
	 * @param data   das gespeicherte Data
	 */
	public IconData(Icon icon, Object data) {
		m_icon = icon;
		m_expandedIcon = null;
		m_data = data;
	}
	
	/**
	 * Legt ein neues Ikon mit dem bestimmten Data an, das nach der Erweiterung
	 * angezeigte Ikon spezifiziert wird.
	 *  
	 * 
	 * @param icon            das angezeigte Ikon vor der Erweiterung
	 * @param expandedIcon    das angezeigte Ikon nach der Erweiterung
	 * @param data            das gespeicherte Data 
	 */
	public IconData(Icon icon, Icon expandedIcon, Object data) {
		m_icon = icon;
		m_expandedIcon = expandedIcon;
		m_data = data;
	}

	/**
	 * Das angezeigte Ikon vor der Erweiterung
	 */
	public Icon getIcon() {
		return m_icon;
	}

	/**
	 * Das angezeigte Ikon nach der Erweiterung
	 */
	public Icon getExpandedIcon() {
		return m_expandedIcon != null ? m_expandedIcon : m_icon;
	}

	/**
	 *  Das gespeicherte Data 
	 */
	public Object getObject() {
		return m_data;
	}

	/**
	 *  Liefert das gespeicherte Data als Type "String"
	 */
	public String toString() {
		return m_data.toString();
	}
}
