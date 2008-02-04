package gui.explorer;

import gui.editor.DocumentFactory;
import gui.explorer.Explorer;
import gui.util.GUIUtil;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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
import java.awt.event.InputEvent;

import java.io.File;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import util.DocumentUtil;
import util.FileOperation;
import util.IOUtils;
import util.MOptionPanes;
import util.Messages;

/**
 * Diese Klasse repraesentiert eine Sicht von einem Dateisystem in einer 
 * Baumstruktur. 
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class FileTree extends JTree implements DragGestureListener,
		DragSourceListener, DropTargetListener, TreeExpansionListener {

	private static final long serialVersionUID = 6035922291978495858L;

	private static DefaultMutableTreeNode flyingTreeNode;
	
	private Document document;
	
	private DragSource dragSource;
	
	private FileNode editingNode;
	
	private Explorer explorer;

	private DefaultTreeModel model;
	
	private Element root;
	
	private Transferable transfer;
	
	public static boolean rightMouse;	
	
	public static String policyName;
	
	public static String policyId;
	
	public FileTree(DefaultTreeModel model, Explorer explorer) {
		super(model);
		this.model = model;
		this.explorer = explorer;

		addTreeExpansionListener(this);
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		putClientProperty("JTree.lineStyle", "Angled");
		setEditable(true);
		setShowsRootHandles(false);

		dragSource = DragSource.getDefaultDragSource();
		
		DragGestureRecognizer dgr = 
				dragSource.createDefaultDragGestureRecognizer(
						this, DnDConstants.ACTION_COPY_OR_MOVE, this);	
		
		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent
				.BUTTON3_MASK);

		new DropTarget(this, this);
	}

	/**
	 * Der aktuell bearbeitete Knoten
	 */
	public FileNode getEditingNode() {
		return editingNode;
	}

	/**
	 * Wandelt einen Knoten von Typ "DefaultMutableTreeNode" zu Type
	 * "FileNode" um.
	 * 
	 * @param node   der originale Knoten von Type "DefaultMutableTreeNode"
	 * @return       der entsprechende Knoten von Type "FileNode"
	 */
	public FileNode getFileNode(DefaultMutableTreeNode node) {
		if (node == null)
			return null;
		Object obj = node.getUserObject();
		if (obj instanceof IconData)
			obj = ((IconData) obj).getObject();
		if (obj instanceof FileNode)
			return (FileNode) obj;

		return null;
	}

	/**
	 * Der selektierte Knoten
	 */
	public DefaultMutableTreeNode getSelectedTreeNode() {
		TreePath path = this.getSelectionPath();
		if (path == null) {
			return null;
		}

		return (DefaultMutableTreeNode) path.getLastPathComponent();
	}

	@Override
	/**
	 * Ob der Knoten mit dem gegebenen TreePath modifizierbar ist.
	 */
	public boolean isPathEditable(TreePath path) {
		if (path == null) {
			return false;
		}
		FileNode iFileNode = getFileNode(
				(DefaultMutableTreeNode) path.getLastPathComponent());
		if (iFileNode == null) {
			return false;
		}
		File iFile = iFileNode.getFile();
		if (iFile != null) {
			setEditingNode(iFileNode);
			return true;
		}
		return false;
	}

	private Cursor selectCursor(int dragAction) {
		if (dragAction == DnDConstants.ACTION_COPY) {
			return DragSource.DefaultCopyDrop;
		}
		return DragSource.DefaultMoveDrop;
	}

	/**
	 * Stellt den aktuell bearbeiteten Knoten.
	 */
	public void setEditingNode(FileNode editingNode) {
		this.editingNode = editingNode;
	}
	
	private boolean verifyDestinationNode(DefaultMutableTreeNode source, 
										  DefaultMutableTreeNode target) {
		if (source == null) {
			return false;
		}
		if (target == null) {
			return false;
		}
		if (source.isNodeDescendant(target)) {
			return false;
		}
		if (!(target.getRoot()).equals(source.getRoot())) {
			return false;	
		}	
		if (target.equals(source.getParent())) {
			return false;
		}
		return true;
	}
	
	private void hasNameOrId() {
		
		FileNode flyingFileNode = getFileNode(flyingTreeNode);
		File flyingFile = flyingFileNode.getFile();
		
		if (flyingFile == null) {
			MOptionPanes.showError(this, Messages.NO_FILE_FOUND);
		}
		
		if (!flyingFile.isFile())
			return;

		try {
			document = DocumentFactory.parseXML(flyingFile);
			if (document != null) {
				root = DocumentUtil.getDocumentRoot(document);
			}
			NamedNodeMap attributes = root.getAttributes();;
			for (int i = 0; i < attributes.getLength(); i++) {
				Attr attri = (Attr) attributes.item(i);
				if (attri.getName().endsWith("Name")) {
					policyName = attri.getValue();					
				}
				if (attri.getName().endsWith(":ID")) {				
					policyId = attri.getValue();					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			MOptionPanes.showError(this, "Error by opening the file.");
		}
	}
	
	private void dropFile(DropTargetDropEvent dtde) {
		
		transfer = dtde.getTransferable();
		try {
			/*
			 * Get the transfered data from the instance of Transferable. In
			 * this case the data is the file path in String.
			 */
			String fileName = (String) transfer
					.getTransferData(DataFlavor.stringFlavor);

			/* Get the tree path from the current location of the cursor. */
			Point newLocation = dtde.getLocation();
			TreePath destiPath = getPathForLocation(newLocation.x,
					newLocation.y);
			
			/* If destination tree path is null, no drop action is possible. */
			if (destiPath == null) {
				dtde.rejectDrop();
				return;
			}
			
			File file = getFileNode(flyingTreeNode).getFile();			
			DefaultMutableTreeNode destiNode = (DefaultMutableTreeNode) 
					destiPath.getLastPathComponent();
			DefaultMutableTreeNode parentNode;
			TreePath parentPath;			
			if (getFileNode(destiNode).getFile().isDirectory()) {		
				parentPath = destiPath.getParentPath();
				parentNode = destiNode;
			}
			else {
				parentPath = destiPath.getParentPath();
				parentNode = (DefaultMutableTreeNode) parentPath
				    .getLastPathComponent();
			}
			
			/* Verify the Destination, to see if drop possible. */
			if (!verifyDestinationNode(flyingTreeNode, parentNode)) {
				dtde.rejectDrop();
				return;				
			}	
			
			File pFile = getFileNode(parentNode).getFile();
			String virtual = pFile.getAbsolutePath() + File.separator + 
			    file.getName();
			if (!virtual.equalsIgnoreCase(fileName) && 
				(new File(virtual)).exists()) {				
				dtde.rejectDrop();
				return;
			}	
	
			FileNode iFileNode = getFileNode(parentNode);
		 	File iFile = iFileNode.getFile();		
		 	try {
		 		FileOperation fileOper = new FileOperation();
		 		try {
		 			fileOper.copyFolder(fileName, iFile.getAbsolutePath());
		 		} catch (Exception e) {
		 		}
		 	} catch (Throwable e) {
		 		e.printStackTrace();
		 	}
		 	
		 	if (file != null) {
		 		if (!IOUtils.deleteFiles(file)) {	
		 			dtde.rejectDrop();
		 			return;
		 		}
		 	}
		 	
			explorer.selectedNode = (DefaultMutableTreeNode) flyingTreeNode
			    .getParent();
		 	explorer.selectedFile = getFileNode(explorer.selectedNode)
		 	    .getFile();			
			explorer.refresh();			
			explorer.selectedNode = parentNode;
			explorer.selectedFile = getFileNode(parentNode).getFile();			 	
			explorer.refresh();						
		 	
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
	
	/* ******************************************************************** */
	/* ---------------DropTargetListener interface methods ---------------- */
	/* ******************************************************************** */
	/**
	 * DropTargetListener interface methods
	 */
	public void dragEnter(DropTargetDragEvent dtde) {	
	}
	
	/**
	 * DropTargetListener interface methods
	 */
	public void dragOver(DropTargetDragEvent dtde) {
	}
	
	/**
	 * DropTargetListener interface methods
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
	
	/**
	 * DropTargetListener interface methods
	 */
	public void dragExit(DropTargetEvent dte) {
	}
	
	/**
	 * DropTargetListener interface methods
	 */
	public void drop(DropTargetDropEvent dtde) {
		dropFile(dtde);		
	}
		
	/* ******************************************************************** */
	/* ---------------DragSourceListener interface methods ---------------- */
	/* ******************************************************************** */
	/**
	 * DragSourceListener interface methods
	 */
	public void dragEnter(DragSourceDragEvent dsde) {

		if (dsde.getGestureModifiers() == InputEvent.BUTTON3_MASK ) {
			rightMouse = true;		
			hasNameOrId();			
		} 
		else {
			rightMouse = false;
		}
	}

	/**
	 * DragSourceListener interface methods
	 */
	public void dragOver(DragSourceDragEvent dsde) {
	}

	/**
	 * DragSourceListener interface methods
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	/**
	 * DragSourceListener interface methods
	 */
	public void dragExit(DragSourceEvent dse) {
	}

	/**
	 * DragSourceListener interface methods
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	/* ******************************************************************** */
	/* ---------------DragGestureListener interface methods --------------- */
	/* ******************************************************************** */
	/**
	 * DragGestureListener interface methods
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		
		//DefaultMutableTreeNode 
		flyingTreeNode = getSelectedTreeNode();
		if (flyingTreeNode == null) {
			return;
		}

		FileNode flyingFileNode = getFileNode(flyingTreeNode);
		File flyingFile = flyingFileNode.getFile();
		

		if (flyingFile != null) {
			StringSelection filePath = new StringSelection(flyingFile
					.getAbsolutePath());
			Cursor cursor = selectCursor(dge.getDragAction());
			dragSource.startDrag(dge, cursor, filePath, this);

		} 
	}

	/* ******************************************************************** */
	/* ---------------TreeExpandsionListener interface methods -------------*/
	/* ******************************************************************** */
	/**
	 * TreeExpandsionListener interface methods
	 */
	public void treeExpanded(TreeExpansionEvent event) {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) event
				.getPath().getLastPathComponent();
		final FileNode fnode = getFileNode(node);

		Thread runner = new Thread() {
			@Override
			public void run() {
				if (fnode != null && fnode.expand(node)) {
					Runnable runnable = new Runnable() {
						public void run() {

							model.reload(node);
						}
					};
					SwingUtilities.invokeLater(runnable);
				}
			}
		};
		runner.start();
	}

	/**
	 * TreeExpandsionListener interface methods
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
	}

}
