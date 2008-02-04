package gui.editor;

import gui.explorer.FileTree;
import gui.util.GUIUtil;
import gui.util.InputDialogPattern;
import gui.util.M_TreeCellsEditor;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.w3c.policy.util.PolicyConstants;

import util.DocumentUtil;
import util.MOptionPanes;
import util.Messages;
import util.StringUtil;


/**
 * Diese Klasse repraesentiert eine Sicht von einer policy in einer Baumstruktur.
 * Sie implementiert die wichtigsten Methoden fuer die Erstellung und Vearbeitung
 * von Policies.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class DomTree extends JTree implements DragGestureListener,
		DragSourceListener, DropTargetListener, TreeSelectionListener {

	private static final long serialVersionUID = -1444487659805031609L;

	private DragSource dragSource = null;

	private DefaultTreeModel model;
	
	private DomTreeNode dragingTreeNode; 

	private PolicyEditor policyEditor;
	
	private TreePath selectedTreePath;

	private Transferable transfer;
	
	private Node dragingNode;
	
	private M_TreeCellsEditor domTreeCellEditor;
	
	/**
	 * ob eine Dragaktion von der linken Maus verursacht wird
	 */
	public static boolean leftMouse;
	
	/**
	 * ob eine Dragaktion von der linken Maus noch mit der "Ctrl" Taste ist
	 */
	public static boolean ctrlKey;	

	/**
	 * Klassenkonstruktor.
	 * 
	 * Legt einen Dokumentbaum mit dem gegebenen Baummodell
	 * 
	 * @param treeModel   das gegebene Modell des Baums
	 */
	public DomTree(DefaultTreeModel treeModel) {
		super(treeModel);
		model = treeModel;		
		configDomTree();	
	}
	
	/**
	 * Legt einen neuen Kommentarknoten an. 
	 * 
	 * @see    #addTextNode()
	 * @see    #addAssertion()
	 *
	 */
	public void addComment() {
		/* Get the selected tree node of user. */
		TreePath path = getSelectionPath();
		DomTreeNode myXMLNode = getSelectedTreeNode();

		/* If no node is selected, then cancel */
		if (myXMLNode == null) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					Messages.NULL_TREENODE);
			return;
		}

		Node parent = myXMLNode.getDOMNode();

		if (parent == null) {
			return;
		}

		String strComment = MOptionPanes.getInput(GUIUtil.getEditor(this),
				"Please input the comment", null);

		if (strComment == null) {
			return;
		}

		Document doc = parent.getOwnerDocument();
		Comment comment = doc.createComment(strComment);

		DomTreeNode newXMLNode = new DomTreeNode(comment);
		myXMLNode.addNode(newXMLNode);

		model.nodeStructureChanged(myXMLNode);

		expandNode(myXMLNode);

		if (path != null) {
			path = path.pathByAddingChild(newXMLNode);
			setSelectionPath(path);
			scrollPathToVisible(path);			 
		}
	}	

	/**
	 * Fuegt eine neue Policy Assertion in die Policy hinzu. 
	 *
	 * @see   #deleteNode()
	 */
	public void addAssertion() {
		
		/* Get the selected tree node of user. */
		TreePath path = getSelectionPath();
		DomTreeNode myXMLNode = getSelectedTreeNode();

		/* If no node is selected, then cancel */
		if (myXMLNode == null) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					Messages.NULL_TREENODE);
			return;
		}

		/* Get the parent Node of the new Node */
		Node parent = myXMLNode.getDOMNode();

		if (parent == null) {
			return;
		}

		InputDialogPattern newAssertionDialog = new InputDialogPattern(false);
		newAssertionDialog.setLabelTitel("Assertion Name : ", "Prefix : ",
				"NamespaceURI : ");
		newAssertionDialog.setLocationRelativeTo(null);
		newAssertionDialog.setVisible(true);
  
        String prefix = newAssertionDialog.getPrefix();
        String nsURI = newAssertionDialog.getNsURI();
        String qName = prefix + ":" + newAssertionDialog.getValue();
        
        /* Input is null means user canceled the operation */
        if ((prefix == null) || (nsURI == null)){
  	        return;
        }

		Document doc = parent.getOwnerDocument();

		Element newElement = DocumentUtil.createElementNS(doc, nsURI,
				qName,true);

		DomTreeNode newXMLNode = new DomTreeNode(newElement);
		myXMLNode.addNode(newXMLNode);

		model.nodeStructureChanged(myXMLNode);

		expandNode(myXMLNode);

		if (path != null) {
			path = path.pathByAddingChild(newXMLNode);
			setSelectionPath(path);
			scrollPathToVisible(path);
			 
		}
	}

	private void addOperator(String qName) {
		/* Get the selected tree node of user. */
		TreePath path = getSelectionPath();
		DomTreeNode myXMLNode = getSelectedTreeNode();

		/* If no node is selected, then cancel */
		if (myXMLNode == null) {
			return;
		}

		/* Get the parent Node of the new Node */
		Node parent = myXMLNode.getDOMNode();

		if (parent == null) {
			return;
		}

		Document doc = parent.getOwnerDocument();

		Element newElement = doc.createElementNS(
				PolicyConstants.WS_POLICY_NAMESPACE_URI, qName);

		DomTreeNode newXMLNode = new DomTreeNode(newElement);
		myXMLNode.addNode(newXMLNode);

		model.nodeStructureChanged(myXMLNode);

		expandNode(myXMLNode);

		if (path != null) {
			path = path.pathByAddingChild(newXMLNode);
			setSelectionPath(path);
			scrollPathToVisible(path);
			 
		}
	}

	/**
	 * Laesst sich ein wsp:All Operator in die Policy einfuegen
	 * 
	 * @see   #addOperatorExactlyOne()
	 * @see   #addOperatorPolicy()
	 */
	public void addOperatorAll() {
		addOperator(PolicyConstants.QNAME_AND);		 
	}

	/**
	 * Laesst sich ein wsp:ExactlyOne Operator in die Policy einfuegen
	 *
	 * @see   #addOperatorAll()
	 * @see   #addOperatorPolicy()
	 */
	public void addOperatorExactlyOne() {
		addOperator(PolicyConstants.QNAME_XOR);	 
	}

	/**
	 * Laesst sich ein wsp:Policy Operator in die Policy einfuegen
	 *
	 * @see   #addOperatorAll()
	 * @see   #addOperatorExactlyOne()
	 */
	public void addOperatorPolicy() {
		addOperator(PolicyConstants.QNAME_POLICY);		 
	}
	
	/**
	 * Legt eine neue Policyreferenz an mit dem gegebenen URI
	 * 
	 * @param policyURI   das gegebene ReferenzURI
	 */
	public void addPolicyReference(String policyURI) {
		/* Get the selected tree node of user. */
		TreePath path = getSelectionPath();
		DomTreeNode myXMLNode = getSelectedTreeNode();

		/* If no node is selected, then cancel */
		if (myXMLNode == null) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					Messages.NULL_TREENODE);
			return;
		}

		/* Get the parent Node of the new Node */
		Node parent = myXMLNode.getDOMNode();

		if (parent == null) {
			return;
		}

		if (!parent.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX)) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					Messages.NO_POLICY_REFERENCE_ALLOWED);
		}

		if (policyURI == null) {
			return;
		}

		Document doc = parent.getOwnerDocument();

		Element policyRef = doc.createElementNS(
				PolicyConstants.WS_POLICY_NAMESPACE_URI,
				PolicyConstants.QNAME_POLICY_REFERENCE);

		policyRef.setAttribute("URI", policyURI);

		DomTreeNode newXMLNode = new DomTreeNode(policyRef);
		myXMLNode.addNode(newXMLNode);

		model.nodeStructureChanged(myXMLNode);

		expandNode(myXMLNode);

		if (path != null) {
			path = path.pathByAddingChild(newXMLNode);
			setSelectionPath(path);
			 
		}
	}

	/**
	 * Legt einen neuen Textknoten an.
	 *
	 * @see  #addComment()
	 * @see  #addAssertion()
	 */
	public void addTextNode() {
		/* Get the selected tree node of user. */
		TreePath path = getSelectionPath();
		DomTreeNode myXMLNode = getSelectedTreeNode();

		/* If no node is selected, then cancel */
		if (myXMLNode == null) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					Messages.NULL_TREENODE);
			return;
		}

		Node parent = myXMLNode.getDOMNode();

		if (parent == null) {
			return;
		}

		String strTextValue = MOptionPanes.getInput(GUIUtil.getEditor(this),
				Messages.INPUT_TEXT_VALUE, null);

		if (strTextValue == null) {
			return;
		}

		Document doc = parent.getOwnerDocument();
		Text text = doc.createTextNode(strTextValue);

		DomTreeNode newXMLNode = new DomTreeNode(text);
		myXMLNode.addNode(newXMLNode);

		model.nodeStructureChanged(myXMLNode);

		expandNode(myXMLNode);

		if (path != null) {
			path = path.pathByAddingChild(newXMLNode);
			setSelectionPath(path);
			scrollPathToVisible(path);			 
		}

	}

	/**
	 * Ermittelt, ob einen Knoten angezeigt werden kann.
	 * 
	 * @param node   der Knoten, der ermittelt werden soll.
	 * @return       false, falls er ein Textknoten ist, und leer oder nur
	 *               "\n" "\r\n" enthalten ist.
	 *               true, anderenfalls
	 */
	public boolean canDisplayNode(DomTreeNode node) {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			return true;
		case Node.TEXT_NODE:
			String text = node.toString();
			return !(text.equals("") || text.equals("\n") || text
					.equals("\r\n"));
		case Node.COMMENT_NODE:
			return true;
		}
		return true;
	}
	
	/**
	 * Gibt der Zelleditor des Baums zurueck
	 * 
	 * @return   der Zelleditor des Baums
	 */
	public M_TreeCellsEditor getDomTreeCellEditor(){
		return this.domTreeCellEditor;
	}

	/**
	 * Ein Knoten bricht zusammen. d.h. alle darunter liegende Dateien
	 * werden nicht mehr angezeigt 
	 * 
	 * @param node   der Knoten, der zusammenbrechen soll.
	 * @see          #expandNode(DomTreeNode)
	 * @see          #collapseTree()
	 */
	public void collapseNode(DomTreeNode node) {
		if (node == null)
			return;

		TreePath path = new TreePath(node.getPath());

		for (int k = 0; k < node.getChildCount(); k++) {
			DomTreeNode child = (DomTreeNode) node.getChildAt(k);
			if (child != null) {
				collapseNode(child);
			}
		}
		collapsePath(path);
	}

	/**
	 * Der ganze Baum bricht zusammen, d.h. nur der Wurzel wird angezeigt.
	 * 
	 * @see   #expandTree()
	 * @see   #collapseNode(DomTreeNode)
	 */
	public void collapseTree() {
		DomTreeNode root = (DomTreeNode) getModel().getRoot();

		collapseNode(root);
	}

	private void configDomTree() {
		
		putClientProperty("JTree.lineStyle", "Angled");

		addTreeSelectionListener(this);
		setEditable(true);
		setRootVisible(true);
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		setToggleClickCount(3);

		dragSource = DragSource.getDefaultDragSource();

		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_COPY_OR_MOVE, this);

		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent
				.BUTTON3_MASK);

		new DropTarget(this, this);

	}

	/**
	 * Legt einen neuen Baumknoten an
	 * 
	 * @param root   der Wurzel dieses Baums
	 * @return       der neu angelegt Baumknoten 
	 * @see          #deleteNode()
	 */
	public DomTreeNode createTreeNode(DomTreeNode root) {
		if (!canDisplayNode(root))
			return null;
		DomTreeNode treeNode = root;
		NodeList list = root.getChildrenNodes();
		for (int k = 0; k < list.getLength(); k++) {
			Node node = list.item(k);
			DomTreeNode nd = new DomTreeNode(node);
			DefaultMutableTreeNode child = createTreeNode(nd);
			if (child != null)
				treeNode.add(child);
		}
		return treeNode;
	}

	/**
	 * Loescht den selektierten Baumknoten aus dem Baum
	 *
	 */
	public void deleteNode() {
		DomTreeNode selectedTreeNode = getSelectedTreeNode();

		if (selectedTreeNode == (DomTreeNode) model.getRoot()) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					Messages.NO_ROOT_DELETION);
			return;
		}

		if (selectedTreeNode == null) {
			return;
		}

		final Node selectedNode = selectedTreeNode.getDOMNode();
		if (selectedNode == null) {
			return;
		}

		DomTreeNode parentTreeNode = (DomTreeNode) selectedTreeNode
		    .getParent();

		String name = null;
		switch (selectedNode.getNodeType()) {
		case Node.ELEMENT_NODE:
			name = selectedNode.getNodeName();
			break;
		case Node.TEXT_NODE:
			name = StringUtil.truncString(selectedNode.getNodeValue(), 30);
			break;
		case Node.COMMENT_NODE:
			name = StringUtil.truncString(selectedNode.getNodeValue(), 30);
			break;
		}

		int result = MOptionPanes.getComfirmResult(GUIUtil.getEditor(this),
				Messages.delWarning(name));

		if (result != JOptionPane.YES_OPTION) {
			return;
		}

		TreePath selectedPath = getSelectionPath();
		TreePath parentPath = selectedPath.getParentPath();

		Node parentNode = selectedNode.getParentNode();
		parentNode.removeChild(selectedNode);

		selectedTreeNode.removeFromParent();

		model.nodeStructureChanged(parentTreeNode);

		expandNode(parentTreeNode);

		setSelectionPath(parentPath);

		scrollPathToVisible(parentPath);
		 
	}
	
	private void addReferencePossible(DropTargetDropEvent dtde, 
			   JMenuItem addReferenceMenuItem) {
		
		if (dragingNode instanceof Text) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"Cannot drag and drop a Text Node");
			dtde.rejectDrop();
			return;
		}
		if (FileTree.policyName == null &&
			FileTree.policyId == null) {			
			addReferenceMenuItem.setEnabled(false); 
			return;
		}
		Point newLocation = dtde.getLocation();
		TreePath destiPath = getPathForLocation(newLocation.x,
					newLocation.y);
		/* If destination tree path is null, no drop action is possible. */
		if (destiPath == null) {
			dtde.rejectDrop();
			addReferenceMenuItem.setEnabled(false); 
			return;
		}
		DomTreeNode destiNode = (DomTreeNode) destiPath.getLastPathComponent();

		if ((destiPath == null) || (destiNode == null)) {
			addReferenceMenuItem.setEnabled(false);  
		}

		/* Get the parent Node of the new Node */
		Node node = destiNode.getDOMNode();

		if (node == null) {
			addReferenceMenuItem.setEnabled(false);  
		}

		if (!node.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX)) {
			//MOptionPanes.showError(GUIUtil.getEditor(this),
				//	Messages.NO_POLICY_REFERENCE_ALLOWED);
			addReferenceMenuItem.setEnabled(false);  
		}

		if (!(node instanceof Element
				&& (node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY)
					|| node.getNodeName().equals(
						PolicyConstants.QNAME_XOR)
					|| node.getNodeName().equals(
						PolicyConstants.QNAME_AND)))) {
			addReferenceMenuItem.setEnabled(false);  
			setSelectionPath(destiPath);
		}
	}

	private void addPolicyReference(DropTargetDropEvent dtde) {
		
		String policyURI = policyEditor.getPolicyReferenceURI();
		
		if (policyURI == null || policyURI.isEmpty()) {
			policyURI = FileTree.policyName;
		}

		if (policyURI.equals(null)) {
			policyURI = FileTree.policyId;
		}
		
		if (policyURI == null) {
			dtde.rejectDrop();
			return;
		}

		Point newLocation = dtde.getLocation();
		TreePath destiPath = getPathForLocation(newLocation.x,
				newLocation.y);

		/* If destination tree path is null, no drop action is possible. */
		if (destiPath == null) {
			dtde.rejectDrop();
			return;
		}

		DomTreeNode destiNode = (DomTreeNode) destiPath.getLastPathComponent();

		/* Get the parent Node of the new Node */
		Node node = destiNode.getDOMNode();

		Document doc = node.getOwnerDocument();

		Element policyRef = doc.createElementNS(
				PolicyConstants.WS_POLICY_NAMESPACE_URI,
				PolicyConstants.QNAME_POLICY_REFERENCE);

		policyRef.setAttribute("URI", policyURI);

		DomTreeNode newXMLNode = new DomTreeNode(policyRef);
		destiNode.addNode(newXMLNode);

		model.nodeStructureChanged(destiNode);

		expandNode(destiNode);

		if (destiPath != null) {
			destiPath = destiPath.pathByAddingChild(newXMLNode);
			setSelectionPath(destiPath);			 
		}
	}
	
	public void dropFile(DropTargetDropEvent dtde) {
	
		try {
			/*
			 * Get the transfered data from the instance of Transferable. In
			 * this case the data is the file path in String.
			 */
			String fileName = (String) transfer
					.getTransferData(DataFlavor.stringFlavor);

			/* Create a File from the file path */
			File assertionFile = new File(fileName);

			/* Get the tree path from the current location of the cursor. */
			Point newLocation = dtde.getLocation();
			TreePath destiPath = getPathForLocation(newLocation.x,
					newLocation.y);

			/* If destination tree path is null, no drop action is possible. */
			if (destiPath == null) {
				/*
				 * If the document has no root element, then prompt user to
				 * create one.
				 */
				if ((DomTreeNode) model.getRoot() == null) {
					MOptionPanes.showError(GUIUtil.getEditor(this),
							"Please crate a root element firstly.");
				}
				dtde.rejectDrop();
				return;
			}

			/*
			 * Parsing the document to drop and get the root element of the
			 * document.
			 */
			Document docToDrop = DocumentFactory.parseXML(assertionFile);
			Element rootToDrop = DocumentUtil.getDocumentRoot(docToDrop);

			/*
			 * Get the parent tree node and the parent dom node.
			 */
			DomTreeNode parentTreeNode = (DomTreeNode) destiPath
					.getLastPathComponent();
			Node parentNode = parentTreeNode.getDOMNode();

			String verifyNodeResult = verifyDestinationNode(rootToDrop,
					parentNode);

			if (verifyNodeResult != null) {
				dtde.rejectDrop();

				MOptionPanes.showError(GUIUtil.getEditor(this),
						verifyNodeResult);
				return;
			}

			Node subTreeRoot = null;

			/* Get the document owned the parent dom node. */
			Document document = parentNode.getOwnerDocument();

			/*
			 * Import the root element of the document to drop into the
			 * document of the parent dom node, in order to add the root
			 * element and all its descendants to the parent dom node. 
			 * The resulting node has no parent.
			 */
			subTreeRoot = document.importNode(rootToDrop, true);

			DomTreeNode subTreeTop = new DomTreeNode(subTreeRoot);

			/* Add the imported node to the parent node. */
			parentNode.appendChild(subTreeRoot);
			parentTreeNode.addNode(createTreeNode(subTreeTop));

			/* Signal that the drop is completed successfully. */
			dtde.getDropTargetContext().dropComplete(true);

			/*
			 * Notify the tree model that the structure of the tree node has
			 * changed.
			 */
			model.nodeStructureChanged(parentTreeNode);

			expandNode(parentTreeNode);
			scrollPathToVisible(new TreePath(subTreeTop.getPath()));

		} catch (UnsupportedFlavorException e) {
			dtde.rejectDrop();
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"Unsupported Flavor Exception by droping the file.");
			return;
		} catch (IOException e) {
			dtde.rejectDrop();
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"I/O error by droping the file.");
			return;
		}	
		 
	}

	/* Drop action for DnD nodes within DOM tree. */
	public void dropNode(DropTargetDropEvent dtde, boolean isCopy) {
		
		if (dragingNode instanceof Text) {
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"Cannot drag and drop a Text Node");
			dtde.rejectDrop();
			return;
		}

		DomTreeNode flyingTreeNode = null;

		try {
			/*
			 * Get the transfered data from the instance of Transferable. In
			 * this case the data is the dragged dom tree node.
			 */
			flyingTreeNode = (DomTreeNode) transfer
					.getTransferData(DomTreeNode.NODE_FLAVOR);
		} catch (UnsupportedFlavorException e1) {
			dtde.rejectDrop();
			e1.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"Unsupported Flavor Exception by getting transfered " +
					"data.");
			return;
		} catch (IOException e1) {
			dtde.rejectDrop();
			e1.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"I/O error by getting transfered data.");
			return;
		}

		if (flyingTreeNode == null) {
			return;
		}

		Node nodeToDrop = flyingTreeNode.getDOMNode();

		/* Get the tree path from the current location of the cursor. */
		Point newLocation = dtde.getLocation();
		TreePath destiPath = getPathForLocation(newLocation.x, newLocation.y);

		if (destiPath == null) {
			dtde.rejectDrop();
			return;
		}

		/* Verify the destination path */
		final String verifyPathResult = verifyDesitnationPath(selectedTreePath,
				destiPath);

		if (verifyPathResult != null) {
			dtde.rejectDrop();
			MOptionPanes.showError(GUIUtil.getEditor(this), verifyPathResult);
			return;
		}

		/* Get target node to append */
		DomTreeNode newParentTreeNode = (DomTreeNode) destiPath
				.getLastPathComponent();
		Node newParentNode = newParentTreeNode.getDOMNode();

		/* Verify the destination node */
		final String verifyNodeResult = verifyDestinationNode(nodeToDrop,
				newParentNode);

		if (verifyNodeResult != null) {
			dtde.rejectDrop();
			MOptionPanes.showError(GUIUtil.getEditor(this), verifyNodeResult);
			return;
		}

		/*
		 * nodeToDrop must be adopt form the target document firstly so that it
		 * can be added later in this document.
		 */

		Document document = newParentNode.getOwnerDocument();

		document.adoptNode(nodeToDrop);

		/* Get original parent node of the flying node */
		DomTreeNode selectedTreeNode = dragingTreeNode;
		DomTreeNode oldParentTreeNode = (DomTreeNode) selectedTreeNode
				.getParent();
		Node selectedNode = dragingNode;
		Node oldParentNode = selectedNode.getParentNode();

		try {
			if (!isCopy) {
				oldParentNode.removeChild(selectedNode);
				oldParentTreeNode.remove(selectedTreeNode);
			}

			newParentNode.appendChild(nodeToDrop);

			newParentTreeNode.addNode(flyingTreeNode);

		} catch (IllegalStateException e) {
			dtde.rejectDrop();
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"Illegal State Exception by droping the node.");
			return;
		}

		/* Singals that the drop is successful */
		dtde.getDropTargetContext().dropComplete(true);

		model.nodeStructureChanged(oldParentTreeNode);
		model.nodeStructureChanged(newParentTreeNode);
		treeDidChange();
		expandNode(newParentTreeNode);
		TreePath path = new TreePath(newParentTreeNode.getPath());

		setSelectionPath(path);
		scrollPathToVisible(path);		 		
	}

	/**
	 * Aendert den Wert der selektierten "Assertion"
	 * 
	 * @see    #editNode()
	 */
	public void editElement() {
		Element newElement;

		DomTreeNode treeNode = getSelectedTreeNode();

		Node editingNode = treeNode.getDOMNode();

		Node parent = editingNode.getParentNode();

		/* Get the old attribute name */
		String oldNSName;
		if (editingNode.getPrefix() == null) {
			oldNSName = PolicyConstants.XMLNS_PREFIX;
		} else {
			oldNSName = PolicyConstants.XMLNS_PREFIX + ":"
					+ editingNode.getPrefix();
		}

	    InputDialogPattern editNodeDialog = new InputDialogPattern(editingNode
	    		.getLocalName(), editingNode.getPrefix(), editingNode
	    		.getNamespaceURI(),false);
	    editNodeDialog.setLabelTitel("Assertion Name : ", "Prefix : ",
	    		"NamespaceURI : ");
	    editNodeDialog.setLocationRelativeTo(null);
	    editNodeDialog.setVisible(true);
  
        String prefix = editNodeDialog.getPrefix();
        String nsURI = editNodeDialog.getNsURI();
        String qName = prefix + ":" + editNodeDialog.getValue();
        
        if ((prefix == null) || (nsURI == null)){
  	        return;
        }

		String newNSAttributeName = PolicyConstants.XMLNS_PREFIX + 
		    ":" + prefix;

		Document doc = parent.getOwnerDocument();

		if (doc == null) {
			MOptionPanes
					.showError(GUIUtil.getEditor(this), "Document is null.");
			return;
		}

		newElement = doc.createElementNS(nsURI, qName);

		Attr attr;

		/* Add the namespace attribute */
		attr = doc.createAttribute(newNSAttributeName);
		attr.setNodeValue(nsURI);
		newElement.getAttributes().setNamedItem(attr);

		/* Import the attributes except the old namespace attribute */
		NamedNodeMap attrs = editingNode.getAttributes();
		if (attrs.getLength() != 0) {
			for (int i = 0; i < attrs.getLength(); i++) {
				attr = (Attr) attrs.item(i);
				if (attr.getNodeName().equals(oldNSName)) {
					continue;
				}
				Attr newAttr = (Attr) doc.importNode(attr, true);
				newElement.getAttributes().setNamedItem(newAttr);
			}
		}

		/* Add the children of the editing node to the new node */
		while (editingNode.hasChildNodes()) {
			newElement.appendChild(editingNode.getFirstChild());
		}

		/* Replace the editing node with the new one */
		editingNode.getParentNode().replaceChild(newElement, editingNode);

		treeNode.setUserObject(newElement);

		((DefaultTreeModel) getModel()).nodeStructureChanged(treeNode);

		expandNode(treeNode);
		
	}

	/**
	 * Aendert den Wert des beliebig selektierten Knotens
	 * 
	 * @see    #editElement()
	 */
	public void editNode() {
		Node node = getSelectedNode();
		if (node == null){
			return;
		}
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			editElement();
			break;
		case Node.TEXT_NODE:
			editTextNode(node);
			break;
		case Node.COMMENT_NODE:
			editTextNode(node);
			break;
		}		 
	}

	/**
	 * Aendert den Wert des selektierten Textknotens
	 * 
	 */
	public void editTextNode(Node node) {
		try {
			String input;
			input = MOptionPanes.getInput(GUIUtil.getEditor(this),
			    		Messages.INPUT_TEXT_VALUE, node.getNodeValue());
			
			if (input == null) {
				return;
			}

			DomTreeNode treeNode = getSelectedTreeNode();

			node.setNodeValue(input);

			model.nodeStructureChanged(treeNode);

			expandNode(treeNode);
		} catch (DOMException e) {
			e.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(this),
					"DOM Exception by deleting the attribute.");
		}	 
	}

	/**
	 * Ein Knoten breitet sich aus; d.h. alle darunter liegenden Dateien werden
	 * zur Schau gestellt.
	 * 
	 * @param node   der Knoten, der ausbreiten soll
	 * @see          #collapseNode(DomTreeNode)
	 * @see          #expandTree()
	 */
	public void expandNode(DomTreeNode node) {
		if (node == null)
			return;

		TreePath path = new TreePath(node.getPath());

		for (int k = 0; k < node.getChildCount(); k++) {
			DomTreeNode child = (DomTreeNode) node.getChildAt(k);
			if (child != null) {
				expandNode(child);
			}
		}
		expandPath(path);
	}
	
	/**
	 * Ein ganzer Baum breitet sich aus; d.h. der komplette Baum wird angezeigt
	 * 
	 * @see    #collapseTree()
	 * @see    #expandNode(DomTreeNode)
	 */
	public void expandTree() {
		DomTreeNode root = (DomTreeNode) getModel().getRoot();
		TreePath path = new TreePath(root);
		expandPath(path);
		for (int k = 0; k < root.getChildCount(); k++) {
			DomTreeNode child = (DomTreeNode) root.getChildAt(k);
			expandNode(child);
		}
	}

	/**
	 * Ermittelt, welcher Knoten nun selektiert ist.
	 * 
	 * @return   der selektierte Knoten von Type "Node"
	 * @see      #getSelectedTreeNode()
	 * @see      #getSelectedTreePath()
	 */
	public Node getSelectedNode() {
		DomTreeNode node = getSelectedTreeNode();
		if (node == null) {
			return null;
		}
		return node.getDOMNode();
	}

	/**
	 * Ermittelt, welcher Knoten nun selektiert ist.
	 * 
	 * @return   der selektierte Knoten von Type "DomTreeNode"
	 * @see      #getSelectedNode()
	 * @see      #getSelectedTreePath()
	 */
	public DomTreeNode getSelectedTreeNode() {
		TreePath path = this.getSelectionPath();
		if (path == null) {
			return null;
		}
		Object obj = path.getLastPathComponent();
		if (!(obj instanceof DomTreeNode)) {
			return null;
		}
		return (DomTreeNode) obj;
	}

	/**
	 * Ermittelt den Pfad des nun selektierten Knoten
	 *  
	 * @return   der Pfad des aktuell selektierten Knoten 
	 * @see      #getSelectedNode()
	 * @see      #getSelectedTreeNode()
	 */
	public TreePath getSelectedTreePath() {
		return new TreePath(getSelectedTreeNode().getParent());
	}

	private Cursor selectCursor(int dragAction) {
		if (dragAction == DnDConstants.ACTION_COPY) {
			return DragSource.DefaultCopyDrop;
		}
		return DragSource.DefaultMoveDrop;
	}

	private String verifyDesitnationPath(TreePath source, TreePath target) {
		if (target == null) {
			return "Invalid drop location.";
		}

		DomTreeNode destiNode = (DomTreeNode) target.getLastPathComponent();
		if (!destiNode.getAllowsChildren()) {
			return "This node does not allow children.";
		}

		if (target.equals(source)) {
			return "Destination cannot be the same as source.";
		}

		if (source.isDescendant(target)) {
			return "Cannot move nodes to its children.";
		}

		return null;
	}

	private String verifyDestinationNode(Node source, Node target) {
		if (target == null) {
			return "Invalid drop destination.";
		}

		String targetNodeName = target.getNodeName();

		if (source instanceof Text) {

			if (target instanceof Element) {

				if (targetNodeName.equals(PolicyConstants.QNAME_POLICY)) {
					return "Cannot move a \"Text\" node to the operator " +
							"\"Policy\".";
				}

				if (targetNodeName.equals(PolicyConstants.QNAME_XOR)) {
					return "Cannot move a \"Text\" node to the operator " +
							"\"ExactlyOne\".";
				}

				if (targetNodeName.equals(PolicyConstants.QNAME_AND)) {
					return "Cannot move a \"Text\" node to the operator " +
							"\"All\".";
				}

				if (targetNodeName
						.equals(PolicyConstants.QNAME_POLICY_REFERENCE)) {
					return "Cannot move a \"Text\" node to a \"Policy " +
							"Reference\".";
				}
			}

			if (target instanceof Text) {
				return "Cannot move a \"Text\" node to a \"Text\" node.";
			}

			if (target instanceof Comment) {
				return "Cannot move a \"Text\" node to a \"Comment\"";
			}
		}

		if (source instanceof Element) {
			if (target instanceof Text) {
				return "\"Text\" node cannot have any children";
			}

			if (target instanceof Element) {
				if (targetNodeName
						.equals(PolicyConstants.QNAME_POLICY_REFERENCE)) {
					return "\"Policy Reference\" cannot have any children " +
							"elements.";
				}

				if (source.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)
						&& ((target.getPrefix() != null && !target.getPrefix()
								.equals(PolicyConstants.WS_POLICY_PREFIX)) || 
								target.getPrefix() == null)) {
					return "Cannot move a \"Policy Reference\" node to an " +
							"\"Assertion\".";
				}
			}

			if (target instanceof Comment) {
				return "\"Comment\" node cannot have any children";
			}
		}

		if (source instanceof Comment) {
			if (target instanceof Text || target instanceof Comment) {
				return "A \"Text\" node or a \"Comment\" node cannot have any" +
						" children.";
			}

			String nodeName = target.getNodeName();
			if (nodeName.equals(PolicyConstants.QNAME_AND)
					|| nodeName.equals(PolicyConstants.QNAME_XOR)
					|| nodeName.equals(PolicyConstants
							.QNAME_POLICY_REFERENCE)){
				return "Sorry, you cannot add comment to \""+nodeName + "\"";
			}
		}

		return null;
	}
	
	private void rightMouseFile(final DropTargetDropEvent dtde) {
		final JPopupMenu popupMenu = new JPopupMenu();   
		final JMenuItem copyMenuItem = new JMenuItem("Copy");   
		final JMenuItem addReferenceMenuItem = new JMenuItem
		    ("Add Reference ");  
		final JMenuItem cancelMenuItem = new JMenuItem("Cancel");     		
  	
		popupMenu.add(copyMenuItem);   
		popupMenu.add(addReferenceMenuItem); 
		popupMenu.addSeparator();
		popupMenu.add(cancelMenuItem); 
	        
		copyMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {   
				dropFile(dtde);
			}    		
		});  

		addReferenceMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {  
				addPolicyReference(dtde);
			}   
		});  
	
		cancelMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {   
				return;
			}   
		});  
		popupMenu.show(this, (int)dtde.getLocation().getX(), 
					     (int)dtde.getLocation().getY());   
		
		addReferencePossible(dtde, addReferenceMenuItem);		
	}
	
	private void leftMouseDom(final DropTargetDropEvent dtde){
		final JPopupMenu popupMenu = new JPopupMenu();   
		final JMenuItem copyMenuItem = new JMenuItem("Copy");   
		final JMenuItem moveMenuItem = new JMenuItem("Move");    
		final JMenuItem cancelMenuItem = new JMenuItem("Cancel");     		
  	
		popupMenu.add(copyMenuItem);   
		popupMenu.add(moveMenuItem); 
		popupMenu.addSeparator();
		popupMenu.add(cancelMenuItem); 
		
		copyMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {   
				dropNode(dtde, true);
			}    		
		});  

		moveMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {  
				dropNode(dtde, false);
			}   
		});  
		
		cancelMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {   
				return;
			}   
		});  
		
		popupMenu.show(this, (int)dtde.getLocation().getX(), 
					     (int)dtde.getLocation().getY()); 
	}
	
	private void rightMouseDom(final DropTargetDropEvent dtde) {
		final JPopupMenu popupMenu = new JPopupMenu();   
		final JMenuItem copyMenuItem = new JMenuItem("Copy");   
		final JMenuItem moveMenuItem = new JMenuItem("Move");   
		final JMenuItem addReferenceMenuItem = new JMenuItem
		    ("Add Reference "); 
		final JMenuItem cancelMenuItem = new JMenuItem("Cancel");     		
	
		popupMenu.add(copyMenuItem);   
		popupMenu.add(moveMenuItem); 
		popupMenu.add(addReferenceMenuItem); 
		popupMenu.addSeparator();
		popupMenu.add(cancelMenuItem); 
	
		copyMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {   
				dropNode(dtde, true);
			}    		
		});  

		moveMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {  
				dropNode(dtde, false);
			}   
		});  

		addReferenceMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {  
		
			}   
		});  
	
		cancelMenuItem.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent e) {   
				return;
			}   
		});  
	
		popupMenu.show(this, (int)dtde.getLocation().getX(), 
				     (int)dtde.getLocation().getY());   
		addReferencePossible(dtde, addReferenceMenuItem);	
	}
    /*
     * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized
	 * (java.awt.dnd.DragGestureEvent)
	 */
	/* ******************************************************************** */
	/* ---------------DragGestureListener interface methods --------------- */
	/* ******************************************************************** */
	public void dragGestureRecognized(DragGestureEvent dge) {
		
		dragingTreeNode = getSelectedTreeNode();
		dragingNode = getSelectedNode();
		
		if (dragingTreeNode != null) {

			Cursor cursor = selectCursor(dge.getDragAction());

			dragSource.startDrag(dge, cursor, dragingTreeNode, this);
		}	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragSourceListener#dragDropEnd
	 * (java.awt.dnd.DragSourceDropEvent)
	 */
	
	/* ******************************************************************** */
	/* ---------------DragSourceListener interface methods ---------------- */
	/* ******************************************************************** */	
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragSourceListener#dragEnter
	 * (java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragEnter(DragSourceDragEvent dsde) {		
		if (dsde.getGestureModifiers() == InputEvent.BUTTON3_MASK) {
			leftMouse = false; 
		} 
		else  {
			leftMouse = true;	
			if (dsde.getGestureModifiers() == 
					InputEvent.BUTTON1_MASK + InputEvent.CTRL_MASK) {
				ctrlKey = true;
			}
			else {
				ctrlKey = false;
			}
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragSourceListener#dragExit
	 * (java.awt.dnd.DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dse) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragSourceListener#dragOver
	 * (java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragOver(DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged
	 * (java.awt.dnd.DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
		if (dsde.getGestureModifiers() == 
				InputEvent.BUTTON1_MASK + InputEvent.CTRL_MASK) {
			ctrlKey = true;
		}
		else {
			ctrlKey = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragEnter
	 * (java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent dtde) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragOver
	 * (java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent dtde) {
	}

	/* ******************************************************************** */
	/* ---------------DropTargetListener interface methods ---------------- */
	/* ******************************************************************** */
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged
	 * (java.awt.dnd.DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragExit
	 * (java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent dte) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#drop
	 * (java.awt.dnd.DropTargetDropEvent)
	 */

	public void drop(DropTargetDropEvent dtde) {
		
		transfer = dtde.getTransferable();
		Point newLocation = dtde.getLocation();
		TreePath destiPath = getPathForLocation(newLocation.x,
					newLocation.y);
		/* If destination tree path is null, no drop action is possible. */
		
		/* Drop action for DnD assertion files to DOM tree. */
		if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				final String fileName = (String) transfer
					.getTransferData(DataFlavor.stringFlavor);
				
				if (fileName.endsWith(".xml") &&
					FileTree.rightMouse ) {
					rightMouseFile(dtde);							
				}	
				else {
					dropFile(dtde);
				}
			} catch (UnsupportedFlavorException e) {
				dtde.rejectDrop();
				e.printStackTrace();
				MOptionPanes.showError(GUIUtil.getEditor(this),
						"Unsupported Flavor Exception by droping the file.");
				return;
			} catch (IOException e) {
				dtde.rejectDrop();
				e.printStackTrace();
				MOptionPanes.showError(GUIUtil.getEditor(this),
						"I/O error by droping the file.");
				return;
			}	
		}
	
		/* Drop action for DnD nodes within DOM tree. */
		if (transfer.isDataFlavorSupported(DomTreeNode.NODE_FLAVOR)) {
			if (destiPath == null) {
				dtde.rejectDrop();	
				return;
			}
			if (leftMouse == true) { 
				if (ctrlKey == true) {					
					leftMouseDom(dtde);						
				} 
				else {			
					dropNode(dtde, false);
				}				
			}
			else {
				rightMouseDom(dtde);
			}
		}
		/* When no flavors are satisfied, reject drop */
		dtde.rejectDrop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TreeSelectionListener#valueChanged
	 * (javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		selectedTreePath = e.getNewLeadSelectionPath();
	}

}
