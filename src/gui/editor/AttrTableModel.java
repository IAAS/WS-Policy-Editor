package gui.editor;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.policy.util.PolicyConstants;

import util.MOptionPanes;
import util.XMLUtil;

/**
 * das Modell einer Attributtabelle
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         AttrTable
 */
public class AttrTableModel extends AbstractTableModel {

	/**
	 * Spaltindex des Spalts von Name
	 */
	public static final int COL_ATTR = 0;
	
	/**
	 * Spaltindex des Spalts von Value
	 */
	public static final int COL_VALUE = 1;

	/**
	 * Ein Feld, in dem die Namen der Spalte gespeichert werden.
	 */
	public static String[] columnNames = { "Attribute", "Value" };

	private static final long serialVersionUID = 9057055966874388908L;

	private NamedNodeMap attributes;

	private Node node;

	private int selectedRowIndex;

	private int selectedColumnIndex;

	public AttrTableModel() {
		super();
	}

	/**
	 * Wenn der selektierte Knoten sich aendert, erneuert die Attributtabelle
	 * und dann alle Attribute des neuen Knotens werden angezeigt.
	 * 
	 * @param aNode   der neue Quellenknoten
	 * @see           #getSourceNode()
	 */
	public void setSourceNode(Node aNode) {
		node = aNode;
		if (aNode == null) {
			attributes = null;
		} else {
			attributes = aNode.getAttributes();
		}
		selectedRowIndex = -1;
		selectedColumnIndex = -1;
	}

	/**
	 * Ermittelt, wie viele Spalten es in der Tabelle gibt.
	 * 
	 * @return   Anzahl der Spalten
	 * @see      #getRowCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Ermittelt den Name eines bestimmten Spaltes.
	 * 
	 * @param columnIndex   der Spaltindex
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	/**
	 * Ermittelt, wie viele Zeilen es in der Tabelle gibt.
	 * 
	 * @return   Anzahl der Zeilen
	 * @see      #getColumnCount()
	 */
	public int getRowCount() {
		if (attributes == null) {
			return 0;
		}
		return attributes.getLength();
	}

	/**
	 * Ermittelt, von welchem Knoten die Attribute angezeigt werden.
	 * 
	 * @return   der Quelleknoten
	 * @see      #setSourceNode(Node)
	 */
	public Node getSourceNode() {
		return node;
	}

	/**
	 * Setzt eine bestimmten Zelle in der Tabelle auf dem angegebenen Wert.
	 * 
	 * @param aValue        der Wert dieser Zelle
	 * @param rowIndex      der Zeilenindex dieser Zelle
	 * @param columnIndex   der Spaltindex dieser Zelle
	 * @see   #getValueAt(int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		setCellEditable(-1, -1);

		if (aValue.toString().equals(getValueAt(rowIndex, columnIndex)
				.toString())) {
			return;
		}
		
		Attr attr = (Attr) attributes.item(rowIndex);

		if (attr == null) {
			return;
		}
		String attrName;
		String attrValue;
		
		switch (columnIndex) {
		case 0:// Name_COLUMN
            attrName = aValue.toString();
            if (!XMLUtil.isLegalAttrName(attrName)){
            	MOptionPanes.showError(null, "The name you have entered " +
            			"is an illegal name");
            	return;
            }
            attrValue = getValueAt(rowIndex, AttrTableModel.COL_VALUE)
                .toString();
            String oldAttrName = getValueAt(rowIndex, AttrTableModel.COL_ATTR)
                .toString();
       
			String prefix = XMLUtil.getPrefix(attrName);
			String nsURI = null;

			/*
			 * If true, add the namespace of the attribute as an attribute
			 * of the source node.
			 */
			boolean addNSAsAttribute = false;

			/*
			 * If attribute has prefix and the attribute does not represents
			 * a namespace, find out wheather the namespace for the prefix
			 * exists already. If not, get a namespace for this prefix.
			 */
			if (prefix != null
					&& !prefix.equals(PolicyConstants.XMLNS_PREFIX)) {
				String namespaceURI = node.lookupNamespaceURI(prefix);
				if (namespaceURI == null) {
					nsURI = MOptionPanes.getNamespaceURI(null, "");
					addNSAsAttribute = true;
				} else {
					nsURI = namespaceURI;
				}

				if (nsURI == null) {
					return;
				}
			}

			/* Delete the attribute with the old name. */
			((Element) node).removeAttribute(oldAttrName);

			if (addNSAsAttribute) {
				((Element) node).setAttribute(PolicyConstants
						.XMLNS_PREFIX + ":" + prefix, nsURI);
			}

			/* Add the new attribute to the source node. */
			((Element) node).setAttribute(attrName, attrValue);
			fireTableStructureChanged();
			break;
			
		case 1:// VALUE_COLUMN
			/* Editing column is column "Value" */

			attrName = getValueAt(rowIndex, AttrTableModel.COL_ATTR)
			    .toString();
			attrValue = aValue.toString();

			/* Add the new attribute to the source node. */
			((Element) node).setAttribute(attrName, attrValue);
			fireTableStructureChanged();
			break;
		}

	}

	/**
	 * Ermittelt, welchen Wert eine bestimmte Zelle in der Tabelle besitzt.
	 * 
	 * @param rowIndex      der Zeilenindex dieser Zelle
	 * @param columnIndex   der Spaltindex dieser Zelle
	 * @return              der Wert dieser Zelle
	 * @see                 #setValueAt(Object, int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (attributes == null || rowIndex < 0 || rowIndex >= getRowCount()) {
			return "";
		}

		Attr attr = (Attr) attributes.item(rowIndex);

		if (attr == null) {
			return "";
		}

		switch (columnIndex) {
		case 0:// Name_COLUMN
			return attr.getName();
		case 1:// VALUE_COLUMN
			return attr.getValue();
		}

		return "";
	}

	/**
	 * Jedes Mal darf nur eine Zelle modifiziert werden. Durch diese Methode
	 * kann man einstellen, welche Zelle darf nun veraendert werden. 
	 * 
	 * @param rowIndex      der Zeilenindex dieser Zelle
	 * @param columnIndex   der Spaltindex dieser Zelle
	 * @see                 #isCellEditable(int, int)
	 */
	public void setCellEditable(int rowIndex, int columnIndex) {
		selectedRowIndex = rowIndex;
		selectedColumnIndex = columnIndex;
	}

	/**
	 * Ermittelt, ob eine bestimmte Zelle momentan modifiziert werden darf.
	 * 
	 * @param rowIndex      der Zeilenindex dieser Zelle
	 * @param columnIndex   der Spaltindex dieser Zelle
	 * @return              ob diese Zelle nun veraendert werden darf oder nicht
	 * @see                 #setCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if ((rowIndex == selectedRowIndex)
				&& (columnIndex == selectedColumnIndex)) {
			return true;
		} else {
			return false;
		}
	}

}
