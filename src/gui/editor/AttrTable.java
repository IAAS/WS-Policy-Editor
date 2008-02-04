package gui.editor;

import gui.util.GUIUtil;
import gui.util.NewAttrDialog;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.policy.util.NSRegistry;
import org.w3c.policy.util.PolicyConstants;

import util.MOptionPanes;
import util.Messages;
import util.XMLUtil;

/**
 *  In dieser Klasse werden die Attribute vom selektierten Knoten in einem
 *  Baum einer Policy in einer Tabellensicht angezeigt. Diese Tabelle besteht 
 *  aus zwei Spalten. Eine davon ist fuer die Namen der Attribute und die 
 *  andere ist fuer die Werte der Attribute. In diese Klasse werden auch die
 *  Methoden fuer Einfuegen, Bearbeiten und Loeschen von Attributen implementiert.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         AttrTableModel
 * @see         PropertiesTable
 */
public class AttrTable extends JTable {

	private static final long serialVersionUID = -5427116104318685214L;

	private String policyReferenceURI;

	private AttrTableModel tableModel;


	/**
	 * Erzeuge eine neue Attributtabelle mit dem gegebenen Tabellenmodell
	 *  
	 * @param model    das gegebene Tabellenmodell
	 */
	public AttrTable(AttrTableModel model) {
		super(model);
		this.tableModel = model;
		setCellSelectionEnabled(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * Lege ein neues Attribut unter der selektierten Assertion an.
	 * Durch Beschraenkung des Eingabendialogs wird gewaehrleistet, dass jedes
	 * Attribut ein Praefix und einen entsprechenden Namensraum besitzt.
	 * 
	 * @see   #deleteAttribute()
	 * @see   #editAttribute()
	 */
	public void addAttribute() {
		Node sourceNode = tableModel.getSourceNode();
		if (!(sourceNode instanceof Element)) {
			return;
		}
		
	    NewAttrDialog attrInputDialog = new NewAttrDialog(null, null, 
	    		sourceNode.getPrefix(),sourceNode.getNamespaceURI());
	    attrInputDialog .setLocationRelativeTo(null);
	    attrInputDialog .setVisible(true);
	  
	    if (attrInputDialog.getAttrName() == null) {
			return;
		}
	    
	    String prefix = attrInputDialog.getPrefix();
	    String nsURI = null;
	    String attrName = prefix + ":" + attrInputDialog.getAttrName();
	    String attrValue = attrInputDialog.getAttrValue();

		/*
		 * If true, add the namespace of the attribute as an attribute of the
		 * source node.
		 */
		boolean addNSAsAttribute = false;

		/*
		 * If attribute has prefix and the attribute does not represents a
		 * namespace, find out wheather the namespace for the prefix exists
		 * already. If not, get a namespace for this prefix.
		 */
		if (prefix != null
				&& !prefix.equals(PolicyConstants.XMLNS_PREFIX)) {
			String namespaceURI = sourceNode.lookupNamespaceURI(prefix);
			if (namespaceURI == null) {
				nsURI = attrInputDialog.getNsURI();
				addNSAsAttribute = true;
			} else {
				nsURI = namespaceURI;
			}
		}

		if (addNSAsAttribute) {
			((Element) sourceNode).setAttribute(
				PolicyConstants.XMLNS_PREFIX + ":" + prefix, nsURI);
		}

		((Element) sourceNode).setAttribute(attrName, attrValue);

		tableModel.fireTableStructureChanged();
		
		displayAttributes(sourceNode);
	}

	/**
	 * Lege ein neues Attribut "wsp:Optional" unter der selektierten Assertion
	 * an und setzt seinen Wert auf "true".
	 *
	 *@see   #removeAttributeOptional()
	 *@see   #addAttributeIgnorable()
	 */
	public void addAttributeOptional() {
		Node sourceNode = tableModel.getSourceNode();
	
		if (!(sourceNode instanceof Element)) {
			return;
		}
		
		String namespaceURI = sourceNode.lookupNamespaceURI(PolicyConstants.WS_POLICY_PREFIX);
		if (namespaceURI == null) {
		    ((Element) sourceNode).setAttribute(
			    PolicyConstants.XMLNS_PREFIX + ":" + PolicyConstants.WS_POLICY_PREFIX, 
			    PolicyConstants.WS_POLICY_NAMESPACE_URI);
	    }
		
		((Element) sourceNode).setAttribute(PolicyConstants.QNAME_OPTIONAL,
				"true");
	
		tableModel.fireTableStructureChanged();
		
		displayAttributes(sourceNode);
	}

	/**
	 * Lege ein neues Attribut "wsp:Ignorable" unter der selektierten Assertion
	 * an und setzt seinen Wert auf "true".
	 *
	 *@see   #removeAttributeIgnorable()
	 *@see   #addAttributeOptional()
	 */
	public void addAttributeIgnorable() {
		Node sourceNode = tableModel.getSourceNode();
	
		if (!(sourceNode instanceof Element)) {
			return;
		}
	
		String namespaceURI = sourceNode.lookupNamespaceURI(PolicyConstants.WS_POLICY_PREFIX);
		if (namespaceURI == null) {
		    ((Element) sourceNode).setAttribute(
			    PolicyConstants.XMLNS_PREFIX + ":" + PolicyConstants.WS_POLICY_PREFIX, 
			    PolicyConstants.WS_POLICY_NAMESPACE_URI);
	    }
		
		((Element) sourceNode).setAttribute(PolicyConstants.QNAME_IGNORABLE,
			"true");
	
		tableModel.fireTableStructureChanged();
		
		displayAttributes(sourceNode);
	}
	
	/**
	 * Loesche das selektierte Attribut
	 * 
	 * @see   #addAttribute()
	 * @see   #editAttribute()
	 */
	public void deleteAttribute() {
		Node node = tableModel.getSourceNode();
		if (!(node instanceof Element)) {
			return;
		}

		int row = getSelectedRow();
		if (row < 0) {
			return;
		}

		int column = AttrTableModel.COL_ATTR;
		String name = (String) tableModel.getValueAt(row, column);

		int result = MOptionPanes.getComfirmResult(GUIUtil.getEditor(this),
				Messages.delWarning(name));
		if (result != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			((Element) node).removeAttribute(name);
		} catch (DOMException e) {
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"DOM Exception by deleting the attribute.");
		}

		tableModel.fireTableRowsDeleted(row, row);

		displayAttributes(node);
	}

	/**
	 * Sobald der selektierte Knoten gewechselt wird, ist diese Methode
	 * aufzurufen, mit der die Tabelle aktualisiert wird.
	 * 
	 * @param aNode   der neue selektierte Knoten, dessen Attribute angezeigt
	 *                werden soll
	 */
	public void displayAttributes(Node aNode) {
		final Node node = aNode;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableModel.setSourceNode(node);
				tableChanged(new TableModelEvent(tableModel));
			}
		});
	}

	/**
	 * Modifiziere ein Attribut
	 * 
	 * @see   #addAttribute()
	 * @see   #deleteAttribute()
	 */
	public void editAttribute() {
		int row = getSelectedRow();
		int column = getSelectedColumn();
		String columnName = getColumnName(column);

		if ((row != -1) && (column != -1)) {

			Node sourceNode = tableModel.getSourceNode();

			/* Editing column is column "Attribute" */
			if (columnName == AttrTableModel.columnNames[0]) {
				String oldName = getValueAt(row, column).toString();

				/* Does not allow to change WS-Policy namespace */
				if (oldName.equals(PolicyConstants.WS_POLICY_NS_NAME)) {
					MOptionPanes.showError(GUIUtil.getEditor(this),
							Messages.NO_CHANGE_ON_POLICY_NS);
					return;
				}

				String oldNCName = XMLUtil.getLocalPart(oldName);
				String oldValue = getValueAt(row, AttrTableModel.COL_VALUE)
				    .toString();
				String oldPrefix = XMLUtil.getPrefix(oldName);
				String oldNsURI = NSRegistry.lookup(oldPrefix);
				NewAttrDialog attrInputDialog = new NewAttrDialog(oldNCName,
						oldValue, oldPrefix, oldNsURI);
				attrInputDialog.setValueInputEditabale(false);
				attrInputDialog.setLocationRelativeTo(null);
				attrInputDialog.setVisible(true);
				  
				if (attrInputDialog.getAttrName() == null) {
				    return;
				}
				    
				String prefix = attrInputDialog.getPrefix();
				String nsURI = null;
				String attrName = prefix + ":" + attrInputDialog.getAttrName();

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
						&& !prefix.equals(PolicyConstants.XMLNS_PREFIX)){
					String namespaceURI = sourceNode.lookupNamespaceURI
					    (prefix);
					if (namespaceURI == null) {
						nsURI = attrInputDialog.getNsURI();
						addNSAsAttribute = true;
					} else {
						nsURI = namespaceURI;
					}

					if (nsURI == null) {
						return;
					}
				}

				String oldAttrName = getValueAt(row, column).toString();

				String value = getValueAt(row, AttrTableModel.COL_VALUE)
						.toString();

				/* Delete the attribute with the old name. */
				((Element) sourceNode).removeAttribute(oldAttrName);

				if (addNSAsAttribute) {
					((Element) sourceNode)
							.setAttribute(PolicyConstants.XMLNS_PREFIX
									+ ":" + prefix, nsURI);
				}

				/* Add the new attribute to the source node. */
				((Element) sourceNode).setAttribute(attrName, value);
			} else {
				/* Editing column is column "Value" */
				String oldValue = getValueAt(row, column).toString();

				/* Does not allow to change WS-Policy namespace */
				if (oldValue.equals(PolicyConstants.WS_POLICY_NAMESPACE_URI)) {
					MOptionPanes.showError(GUIUtil.getEditor(this),
							Messages.NO_CHANGE_ON_POLICY_NS);
					return;
				}

				String attrValue;
				String attrName = getValueAt(row, AttrTableModel.COL_ATTR)
				    .toString();

				/* Get the new value for this attribute. */
				if (sourceNode.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)) {
					attrValue = getPolicyURI();
				} else { 
					
					String oldNCName = XMLUtil.getLocalPart(attrName);
					String oldPrefix = XMLUtil.getPrefix(attrName);
					String oldNsURI = NSRegistry.lookup(oldPrefix);
					
					NewAttrDialog attrInputDialog = new NewAttrDialog
					    (oldNCName, oldValue, oldPrefix, oldNsURI);
					attrInputDialog.setNameInputEditabale(false);
					attrInputDialog.setLocationRelativeTo(null);
					attrInputDialog.setVisible(true);
					  
					if (attrInputDialog.getAttrValue() == null) {
					    return;
					}
					
					attrValue = attrInputDialog.getAttrValue();
				}

				if (attrValue == null) {
					return;
				}

				/* Add the new attribute to the source node. */
				((Element) sourceNode).setAttribute(attrName, attrValue);
			}

			tableModel.fireTableStructureChanged();

			displayAttributes(sourceNode);
		}
	}

	private String getPolicyURI() {
		/*
		 * Get the URI for policy referece, which is set by the policy editor.
		 * See PolicyEditor#createAttrTable()#mouseClicked().
		 */
		return policyReferenceURI;
	}
	
	/**
	 * Loesche das Attribut "wsp:Optional" von der selektierten Assertion.
	 *
	 *@see   #addAttributeOptional()
	 *@see   #removeAttributeIgnorable()
	 */
	public void removeAttributeOptional() {
		Node node = tableModel.getSourceNode();
		
		if (!(node instanceof Element)) {
			return;
		}

		try {
			((Element) node).removeAttribute(PolicyConstants.QNAME_OPTIONAL);
		} catch (DOMException e) {
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"DOM Exception by deleting the attribute.");
		}

		tableModel.fireTableStructureChanged();

		displayAttributes(node);
	}
	
	/**
	 * Loesche das Attribut "wsp:Ignorable" von der selektierten Assertion.
	 *
	 *@see   #addAttributeIgnorable()
	 *@see   #removeAttributeOptional()
	 */
	public void removeAttributeIgnorable() {
		Node node = tableModel.getSourceNode();
		
		if (!(node instanceof Element)) {
			return;
		}

		try {
			((Element) node).removeAttribute(PolicyConstants.QNAME_IGNORABLE);
		} catch (DOMException e) {
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"DOM Exception by deleting the attribute.");
		}

		tableModel.fireTableStructureChanged();

		displayAttributes(node);
	}

	/**
	 * Setze den PolicyReference URI auf dem angegebenen URI
	 * 
	 * @param policyReferenceURI   das zu setzende URI
	 */
	public void setPolicyReferenceURI(String policyReferenceURI) {
		this.policyReferenceURI = policyReferenceURI;
	}
	
}
