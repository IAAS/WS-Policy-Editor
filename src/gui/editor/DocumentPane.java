package gui.editor;

import gui.images.Images;

import gui.editor.PolicyEditor;
import gui.editor.AttrTable;

import gui.util.M_TreeCellsEditor;
import gui.util.M_TreeCellsRenderer;

import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
//import javax.swing.event.TreeModelEvent;
//import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.policy.util.PolicyConstants;

import util.DocumentUtil;

/**
 * 
 * DocumentPane represents a policy document. Every policy will be display in a
 * document pane, which again will be added to a tabbed pane in the policy editor.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class DocumentPane extends JScrollPane {
	
	private static final long serialVersionUID = 100933155559463562L;
	
	private PolicyEditor owner;
	
	private AttrTable attrTable;

	private Document document;

	private DomTree domTree;

	private File file;

	private boolean fileChanged = false;

	private boolean isProcessed;

	private Element root;

	private DefaultTreeModel treeModel;

	private JPopupMenu popupMenu;

	/**
	 * Klassenkonstruktor.
	 * 
	 * @param file        die Datei, die hier angezeigt wird
	 * @param owner       eine Oberkomponente, Policy Editor
	 * @param attrTable   die dazu gehoerende Attributtabelle
	 */
	public DocumentPane(File file, PolicyEditor owner, AttrTable attrTable) {
		super();
		this.file = file;
		this.owner = owner;
		this.attrTable = attrTable;
		createTreeModel();
		createTree();
		setViewportView(domTree);
		domTree.addMouseListener(popupMenuMouseListener);
		
	}

	private void createTree() {
		domTree = new DomTree(treeModel);

		M_TreeCellsRenderer renderer = new M_TreeCellsRenderer();
		domTree.setCellRenderer(renderer);

		M_TreeCellsEditor domTreeCellEditor = new M_TreeCellsEditor(domTree,
				renderer);
		domTree.setCellEditor(domTreeCellEditor);
	}

	private void createTreeModel() {
		treeModel = new DefaultTreeModel(null);
	}

	/**
	 * Returns the document.
	 */
	public Document getDocument() {
		return this.document;
	}

	/**
	 * Returns the domTree.
	 */
	public DomTree getDomTree() {
		return this.domTree;
	}

	/**
	 * Returns the file.
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * Returns the root.
	 */
	public Element getRoot() {
		return this.root;
	}

	/**
	 * Returns the treeModel.
	 */
	public DefaultTreeModel getTreeModel() {
		return this.treeModel;
	}

	/**
	 * Returns the fileChanged.
	 */
	public boolean isFileChanged() {
		return this.fileChanged;
	}

	/**
	 * Returns the isProcessed.
	 */
	public boolean isProcessed() {
		return this.isProcessed;
	}

	/**
	 * Legt ein Dokument fuer die neue Policy an
	 * 
	 * @param policyName   der Name der neuen Policy
	 * @param Id           Id der neuen Policy
	 * @param description  die Beschreibung der neuen Policy
	 */
	public void newDocument(String policyName, String Id, String description) {
		document = DocumentFactory.createNewDocument();

		root = DocumentUtil.createElementNS(document,
				PolicyConstants.WS_POLICY_NAMESPACE_URI,
				PolicyConstants.QNAME_POLICY, true);
		
		if (!policyName.isEmpty()){
			root.setAttribute(PolicyConstants.WS_POLICY_PREFIX + ":" + "Name", policyName);
		}
		
		if (!Id.isEmpty()){
			root.setAttribute(PolicyConstants.XMLNS_PREFIX + ":" + PolicyConstants.WSU_PREFIX,
					PolicyConstants.WSU_NAMESPACE_URI);
			root.setAttribute(PolicyConstants.WSU_PREFIX + ":" + "Id", Id);
		}
		
		if (!description.isEmpty()){
			root.setAttribute(PolicyConstants.XMLNS_PREFIX + ":" + PolicyConstants.WSPE_PREFIX,
					PolicyConstants.WSPE_NAMESPACE_URI);
			root.setAttribute(PolicyConstants.WSPE_PREFIX + ":" + "Description", description);
		}
		
		root.normalize();

		document.appendChild(root);

		DomTreeNode top = new DomTreeNode(root);

		treeModel.setRoot(top);

		domTree.treeDidChange();

		DocumentFactory.write(document, file);

		setFileChanged(false);
	}


	/**
	 * Oeffnet ein Dokument
	 * 
	 * @param doc   das Dokument, das oeffnet werden soll
	 */
	public void openDocument(Document doc) {
		document = doc;

		root = DocumentUtil.getDocumentRoot(document);

		DefaultMutableTreeNode top = domTree.createTreeNode(new DomTreeNode(
				root));

		treeModel.setRoot(top);

		domTree.treeDidChange();

		setFileChanged(false);
	}

	/**
	 * @param fileChanged
	 *            The fileChanged to set.
	 */
	public void setFileChanged(boolean fileChanged) {	   
		this.fileChanged = fileChanged;
	}

	/**
	 * @param isProcessed
	 *            The isProcessed to set.
	 */
	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	
	private void createPopupMenu() {
		popupMenu = new JPopupMenu();
		
		Action actionOpen = getActionOpen();
		popupMenu.add(actionOpen);
		
		Action actionSave = getActionSave();
		popupMenu.add(actionSave);
		
		Action actionSaveAs = getActionSaveAs();
		popupMenu.add(actionSaveAs);
		
		Action actionViewSource = getActionViewSource();
		popupMenu.add(actionViewSource);
		
		popupMenu.addSeparator();
		
		Action actionPolicy = getActionPolicy ();
		popupMenu.add(actionPolicy );
		
		Action actionExactlyOne = getActionExactlyOne();
		popupMenu.add(actionExactlyOne);
		
		Action actionAll = getActionAll();
		popupMenu.add(actionAll);
		
		Action actionPolicyReference = getActionPolicyReference();
		popupMenu.add(actionPolicyReference);
		
		popupMenu.addSeparator();
		
		Action actionAddElement = getActionAddElement();
		popupMenu.add(actionAddElement);
		
		Action actionSetOptional = getActionSetOptional();
	    popupMenu.add(actionSetOptional);
		
		Action actionSetIgnorable = getActionSetIgnorable();
	    popupMenu.add(actionSetIgnorable);
	    
		Action actionAddComment = getActionAddComment();
		popupMenu.add(actionAddComment);
		
		Action actionAddText = getActionAddText();
		popupMenu.add(actionAddText);
		
		Action actionEditElement = getActionEditElement();
		popupMenu.add(actionEditElement);
		
		Action actionDeleteElement = getActionDeleteElement();
		popupMenu.add(actionDeleteElement);
		
		popupMenu.addSeparator();
		
		Action actionAddAttribute = getActionAddAttribute();
		popupMenu.add(actionAddAttribute);
		
		Action actionEditAttribute = getActionEditAttribute();
		popupMenu.add(actionEditAttribute);
		
		Action actionDeleteAttribute = getActionDeleteAttribute();
		popupMenu.add(actionDeleteAttribute);
		
		popupMenu.addSeparator();
		
		popupMenu.addSeparator();
		
		Action actionExpandAll = getActionExpandAll();
		popupMenu.add(actionExpandAll);
		
		Action actionExpand = getActionExpand();
		popupMenu.add(actionExpand);
		
		Action actionCollapseAll = getActionCollapseAll();
		popupMenu.add(actionCollapseAll);
		
		Action actionCollapse = getActionCollapse();
		popupMenu.add(actionCollapse);
		
	}
		
	
	protected Action getActionOpen(){
		Action iAction = new AbstractAction("Open a Policy File",
				Images.ICON_OPEN_24) {

			private static final long serialVersionUID = -7188721979631605407L;

			public void actionPerformed(ActionEvent e) {
			owner.openDocument(null);
			}
		};
		return iAction;
	}
	
	protected Action getActionSave(){
		Action iAction = new AbstractAction("Save",
				Images.ICON_SAVE_24) {

			private static final long serialVersionUID = -6476406102467356096L;

			public void actionPerformed(ActionEvent e) {
			owner.save();
			}
		};
		iAction.setEnabled(owner.isCurrentFileChanged());
		return iAction;
	}
	
	protected Action getActionSaveAs(){
		Action iAction = new AbstractAction("Save As...",
				Images.ICON_SAVE_AS_24) {

		    private static final long serialVersionUID = -7816647171763248539L;

			public void actionPerformed(ActionEvent e) {
				owner.saveAs();
			}
		};
		return iAction;
	}

	protected Action getActionViewSource(){
		Action iAction = new AbstractAction("View Source",
				Images.ICON_VIEW_SOURCE_24) {
			
			private static final long serialVersionUID = -8271588451784765792L;

			public void actionPerformed(ActionEvent e) {
				owner.showSourceViewer();
			}
		};
		boolean b1 = !isFileChanged();
	    iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionPolicy(){
		Action iAction = new AbstractAction("Add Operator \"wsp:Policy\"",
				Images.ICON_POLICY_OPERATOR_24) {

		    private static final long serialVersionUID = 4039933539753322024L;

			public void actionPerformed(ActionEvent e) {
				domTree.addOperatorPolicy();
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();
		
		boolean b1;
		if (!(node instanceof Element)
				|| node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)) {
			b1 = false;
		} else {
			b1 = true;
		}
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionExactlyOne(){
		Action iAction = new AbstractAction("Add Operator \"wsp:ExactlyOne\"",
				Images.ICON_XOR_24) {
	
		    private static final long serialVersionUID = -8782909843027487324L;

			public void actionPerformed(ActionEvent e) {
				domTree.addOperatorExactlyOne();
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (!(node instanceof Element)
				|| node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)) {
			b1 = false;
		} else {
			b1 = true;
		}
		iAction.setEnabled(b1);	
		return iAction;
	}
	
	protected Action getActionAll(){
		Action iAction = new AbstractAction("Add Operator \"wsp:All\"",
				Images.ICON_ALL_24) {

			private static final long serialVersionUID = -1616143345279650867L;

			public void actionPerformed(ActionEvent e) {
				domTree.addOperatorAll();
			}
		};	
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (!(node instanceof Element)
				|| node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)) {
			b1 = false;
		} else {
			b1 = true;
		}
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionPolicyReference() {
		Action iAction = new AbstractAction("Add Policy Reference",
				Images.ICON_POLICY_REFERENCE_24) {

			private static final long serialVersionUID = -7356747064105345127L;

			public void actionPerformed(ActionEvent e) {
				owner.addPolicyReference();
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();
		
		boolean b;
		if (node == null) {
			b = false;
		} else {
			b = node instanceof Element
					&& (node.getNodeName().equals(PolicyConstants.QNAME_POLICY)
							|| node.getNodeName().equals(
									PolicyConstants.QNAME_XOR) || node
							.getNodeName().equals(PolicyConstants.QNAME_AND));
		}
		iAction.setEnabled(b);
		return iAction;
	}
	
	protected Action getActionAddElement(){
		Action iAction = new AbstractAction("Add an Assertion",
				Images.ICON_ADD_Element_24) {

		    private static final long serialVersionUID = 7688989694868180507L;

			public void actionPerformed(ActionEvent e) {
				domTree.addAssertion();
			}
			
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (!(node instanceof Element)
				|| node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)) {
			b1 = false;
		} else {
			b1 = true;
		}
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionSetOptional(){
		String name;
		ImageIcon icon;
		if (domTree.getSelectedTreeNode() == null){			
		    name = "Set the Selected Assertion Optional";
		    icon = Images.ICON_ADD_OPTIONAL;
	     } else {
	    	 if (domTree.getSelectedTreeNode().getNodeType() == Node.ELEMENT_NODE){
	            if (domTree.getSelectedTreeNode().isOptional()){
		            name = "Set the Selected Assertion Non-optional";
			        icon = Images.ICON_REMOVE_OPTIONAL;
	            } else {
		            name = "Set the Selected Assertion Optional";
			        icon = Images.ICON_ADD_OPTIONAL;
	            }
	         } else {
		         name = "Set the Selected Assertion Optional";
			     icon = Images.ICON_ADD_OPTIONAL;	
	         }
	     }	

		Action iAction = new AbstractAction(name, icon) {

			private static final long serialVersionUID = 205727830902630788L;

			public void actionPerformed(ActionEvent e) {	
			    DomTreeNode treeNode = domTree.getSelectedTreeNode();
			    if (treeNode.isOptional()) {
					owner.setAssertionOptional(false);
					return;
				}
				owner.setAssertionOptional(true);
			}
		};

		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (node == null) {
			b1 = false;
		} else {
			b1 = node instanceof Element
					&& (node.getPrefix() == null || ((node.getPrefix() != null) && !node
							.getPrefix().equals(
									PolicyConstants.WS_POLICY_PREFIX)));
		}	
		iAction.setEnabled(b1);
		return iAction;
	}
	
	
	protected Action getActionSetIgnorable(){
		String name;
		ImageIcon icon;
		if (domTree.getSelectedTreeNode() == null){			
		    name = "Set the Selected Assertion Ignorable";
		    icon = Images.ICON_ADD_IGNORABLE;
	     } else {
	    	 if (domTree.getSelectedTreeNode().getNodeType() == Node.ELEMENT_NODE){
	            if (domTree.getSelectedTreeNode().isIgnorable()){
		            name = "Set the Selected Assertion Non-ignorable";
			        icon = Images.ICON_REMOVE_IGNORABLE;
	            } else {
		            name = "Set the Selected Assertion Ignorable";
			        icon = Images.ICON_ADD_IGNORABLE;
	            }
	         } else {
		         name = "Set the Selected Assertion Ignorable";
			     icon = Images.ICON_ADD_IGNORABLE;	
	         }
	     }	

		Action iAction = new AbstractAction(name, icon) {

			private static final long serialVersionUID = 205727830902630788L;

			public void actionPerformed(ActionEvent e) {	
			    DomTreeNode treeNode = domTree.getSelectedTreeNode();
			    if (treeNode.isOptional()) {
					owner.setAssertionIgnorable(false);
					return;
				}
				owner.setAssertionIgnorable(true);
			}
		};

		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (node == null) {
			b1 = false;
		} else {
			b1 = node instanceof Element
					&& (node.getPrefix() == null || ((node.getPrefix() != null) && !node
							.getPrefix().equals(
									PolicyConstants.WS_POLICY_PREFIX)));
		}	
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionAddComment(){
		Action iAction = new AbstractAction("Add Comment",
				Images.ICON_COMMENT_24) {
	
			private static final long serialVersionUID = 8037696011595399158L;

			public void actionPerformed(ActionEvent e) {
		       domTree.addComment();
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		/* Allow policy and assertions to have comments. */
		boolean b1 = node instanceof Element
				&& (node.getPrefix() == null || (node.getPrefix() != null && (!node
						.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX) || node
						.getNodeName().equals(PolicyConstants.QNAME_POLICY))));
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionAddText(){
		Action iAction = new AbstractAction("Add Text Value",
				Images.ICON_ADD_TEXT_24) {

			private static final long serialVersionUID = -1906265636688688256L;

			public void actionPerformed(ActionEvent e) {
				domTree.addTextNode();
			}
		};
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1 = (node instanceof Element && (node.getPrefix() == null || !(node
				.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX))));

		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionEditElement(){
		Action iAction = new AbstractAction("Edit the Selected Node",
				Images.ICON_EDIT_ELEMENT_24) {

			private static final long serialVersionUID = -1397374277371119998L;

			public void actionPerformed(ActionEvent e) {
				domTree.editNode();
				attrTable.displayAttributes(domTree.getSelectedNode());
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (node == null
				|| (node instanceof Element && (node.getPrefix() == null || node
						.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX)))) {
			b1 = false;
		} else {
			b1 = true;
		}	
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionDeleteElement(){
		Action iAction = new AbstractAction("Delete the Selected Node",
				Images.ICON_DELETE_ELEMENT_24) {

			private static final long serialVersionUID = -6443642831488519784L;

			public void actionPerformed(ActionEvent e) {
				domTree.deleteNode();
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1 = node != null;	
		iAction.setEnabled(b1);	
		return iAction;
	}
	
	
	protected Action getActionAddAttribute(){
		Action iAction = new AbstractAction("Add a New Attribute",
				Images.ICON_ADD_ATTRIBUTE_24) {


			private static final long serialVersionUID = 709007299040992161L;

			public void actionPerformed(ActionEvent e) {
				attrTable.addAttribute();
			}
		};
		
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (!(node instanceof Element) || node.getNodeName().equals
				(PolicyConstants.QNAME_XOR) || node.getNodeName().equals 
				(PolicyConstants.QNAME_AND)) {
			b1 = false;
		} else {
			b1 = true;
		}

		iAction.setEnabled(b1);	
		return iAction;
	}
	
	protected Action getActionEditAttribute(){
		Action iAction = new AbstractAction("Edit the Selected Attribute",
				Images.ICON_EDIT_ATTRIBUTE_24) {
        
			private static final long serialVersionUID = 1011066168923682124L;

			public void actionPerformed(ActionEvent e) {
				attrTable.editAttribute();
			}
		};
		
    	boolean b1 = attrTable.getSelectedRowCount() == 1;
		iAction.setEnabled(b1);	
		return iAction;
	}
	
	protected Action getActionDeleteAttribute(){
		Action iAction = new AbstractAction("Delete the Selected Attribute",
				Images.ICON_DELETE_ATTRIBUTE_24) {
			
			private static final long serialVersionUID = 6908323059731546750L;

			public void actionPerformed(ActionEvent e) {
				attrTable.deleteAttribute();
			}
		};

		boolean b1 = attrTable.getSelectedRowCount() == 1;
		iAction.setEnabled(b1);
		return iAction;
	}

	protected Action getActionExpandAll(){
		Action iAction = new AbstractAction("Expand the Whole Tree",
				Images.ICON_EXPAND_ALL_24){

			private static final long serialVersionUID = -702830913799760633L;

			public void actionPerformed(ActionEvent e) {
				domTree.expandTree();
				owner.enableExpandCollapse();
			}
		};
		
		boolean b1 = document != null
		&& DocumentUtil.getDocumentRoot(document).hasChildNodes();
		
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionExpand(){
		Action iAction = new AbstractAction("Expand the Selected Node",
				Images.ICON_EXPAND_24){

			private static final long serialVersionUID = -1255581452801199418L;

			public void actionPerformed(ActionEvent e) {
				domTree.expandNode(domTree.getSelectedTreeNode());
				owner.enableExpandCollapse();
			}
		};
		
		boolean b1 = domTree == null ? false
				: domTree.getSelectedNode() instanceof Element
						&& domTree.getSelectedNode().hasChildNodes();
		boolean b2 = domTree == null ? false : domTree.isExpanded(domTree
				.getSelectionPath());
		iAction.setEnabled(b1 && !b2);
		return iAction;
	}
	
	protected Action getActionCollapseAll(){
		Action iAction = new AbstractAction("Collapse the Whole Tree",
				Images.ICON_COLLAPSE_ALL_24) {

			private static final long serialVersionUID = 7645214195981580518L;

			public void actionPerformed(ActionEvent e) {
				domTree.collapseTree();
				owner.enableExpandCollapse();
			}
		};
		boolean b1 = document != null
		&& DocumentUtil.getDocumentRoot(document).hasChildNodes();
		iAction.setEnabled(b1);
		return iAction;
	}
	
	protected Action getActionCollapse(){
		Action iAction = new AbstractAction("Collapse the Selected Node",
				Images.ICON_COLLAPSE_24) {

			private static final long serialVersionUID = 8601928763273627228L;
					
			public void actionPerformed(ActionEvent e) {
				domTree.collapseNode(domTree.getSelectedTreeNode());
				owner.enableExpandCollapse();
			}
		};
		boolean b1 = domTree == null ? false
				: domTree.getSelectedNode() instanceof Element
						&& domTree.getSelectedNode().hasChildNodes();
		boolean b2 = domTree == null ? false : domTree.isExpanded(domTree
				.getSelectionPath());
		iAction.setEnabled(b1 && b2);
		return iAction;
	}

	private MouseListener popupMenuMouseListener = new MouseListener(){
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				int x = e.getX();
				int y = e.getY();
				createPopupMenu();
			    popupMenu.show(domTree, x, y);
			}
		}
		
		public void mouseClicked(MouseEvent e) {
		}
		
		public void mouseEntered(MouseEvent e) {
		}
		
		public void mouseExited(MouseEvent e) {
		}
		
		public void mousePressed(MouseEvent e) {
		}
	};

}
