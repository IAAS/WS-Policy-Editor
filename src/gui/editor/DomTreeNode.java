package gui.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;
import java.io.Serializable;

import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.policy.util.PolicyConstants;

import util.StringUtil;

/**
 * Jeder Knoten im Baum einer Policy ist ein DomTreeNode. In einem DomTreeNode 
 * ist ein Node eingekapselt. Ein Node repraesentiert einen Knoten im Baum 
 * einer Policy. Er kann ein Element, ein Text, ein Kommentar oder ein Attribut
 * usw. sein.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class DomTreeNode extends DefaultMutableTreeNode implements
		Transferable, Serializable {

	private static final long serialVersionUID = -297767899016412512L;

	public static final DataFlavor NODE_FLAVOR = new DataFlavor(
			DomTreeNode.class, "DOM Tree Node");

	protected static final DataFlavor flavors[] = { NODE_FLAVOR };

	/**
	 * class constructor
	 * 
	 * Creates a tree node with no parent, no children, but which allows
	 * children, and initializes it with the specified user object.
	 * 
	 * @param node   the specified user object
	 */
	public DomTreeNode(Node node) {
		super(node);
	}
	
	/**
	 * Fugt einen Kinderknoten ein.
	 * 
	 * @param child   das gegebene Kinderelement von Type "DomTreeNode"  
	 */
	public void addNode(DomTreeNode child) {
		Node node = getDOMNode();

		if (node == null) {
			return;
		}

		node.appendChild(child.getDOMNode());
		add(child);
	}

	/**
	 * Wandelt diesen Knoten von Type "DomTreeNode" zur Type "Node" um
	 * 
	 * @return   der entsprechende Knoten von Type "Node"
	 */
	public Node getDOMNode() {
		Object obj = getUserObject();

		if (obj instanceof Node) {
			return (Node) obj;
		}
		return null;
	}

	/**
	 * Ermittelt, was fuer ein Knoten es ist.
	 * 
	 * @return   die Type des Knotens
	 * @see      org.w3c.dom.Node#getNodeType()
	 */
	public int getNodeType() {
		return getDOMNode().getNodeType();
	}

	/**
	 * eine Liste, die alle Kinderknoten enthaelt
	 * 
	 * @see  org.w3c.dom.Node#getChildNodes()
	 */
	public NodeList getChildrenNodes() {
		return getDOMNode().getChildNodes();
	}

	/**
	 * Ermittelt, ob der Knoten einen Kinderknoten hat.
	 * 
	 * @return   true, falls mindestens ein Kinderknoten vorhanden ist,
	 *           false, anderenfalls
	 */
	public boolean hasTextChild() {
		return getChildTextNode() != null;
	}

	/**
	 * Sucht den Kindeknoten, der als Text darstellt wird.
	 * 
	 * @return  der entsprechende Knoten von Type "DomTreeNode"
	 * @see     #getChildTextNode()
	 */
	public DomTreeNode getChildTextTreeNode() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			DomTreeNode child = (DomTreeNode) getChildAt(i);
			if (child.getDOMNode() instanceof Text) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Sucht den Kindeknoten, der als Text darstellt wird.
	 * @return  der entsprechende Knoten von Type "Text"
	 * @see     #getChildTextTreeNode()
	 */
	public Text getChildTextNode() {
		if (getChildTextTreeNode() != null) {
			return (Text) getChildTextTreeNode().getDOMNode();
		}
		return null;
	}

	/**
	 * Liefert den Inhalt des Knotens, der in dem Baum angezeigt werden.
	 */
	@Override
	public String toString() {
		Node node = getDOMNode();

		if (node == null) {
			return getUserObject().toString();
		}
		StringBuffer sb = new StringBuffer();
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			sb.append(StringUtil.normalizeString(node.getNodeName()));
			break;
		case Node.TEXT_NODE:
			sb.append(StringUtil.normalizeString(node.getNodeValue()));
			break;
		case Node.COMMENT_NODE:
			sb.append(StringUtil.normalizeString(node.getNodeValue()));
			break;
		}

		return sb.toString();
	}

	/* ******************************************************************** */
	/* --------------------Transferable interface methods ----------------- */
	/* ******************************************************************** */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(NODE_FLAVOR)) {
			return this;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(NODE_FLAVOR);
	}

	public boolean isOptional() {
		NamedNodeMap attributes = getDOMNode().getAttributes();

		if (attributes.getNamedItem(PolicyConstants.QNAME_OPTIONAL) != null) {
			return true;
		}

		return false;
	}
	
	public boolean isIgnorable() {
		NamedNodeMap attributes = getDOMNode().getAttributes();

		if (attributes.getNamedItem(PolicyConstants.QNAME_IGNORABLE) != null) {
			return true;
		}

		return false;
	}


}
