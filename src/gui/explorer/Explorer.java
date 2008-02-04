package gui.explorer;

import gui.images.Images;
import gui.util.GUIConstants;
import gui.util.GUIUtil;
import gui.util.M_FileChooser;
import gui.util.M_TreeCellsEditor;
import gui.util.M_TreeCellsRenderer;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import util.IOUtils;
import util.MOptionPanes;
import util.Messages;

import java.lang.String;
import java.util.Vector;

/**
 * Diese Klasee fasst die gemeinsamen Methoden zusammen, die in den Klassen 
 * "PolicyExplorer" und "AssertionExplorer" verwendet werden.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         PolicyExplorer
 * @see         AssertionExplorer
 */
public abstract class Explorer extends JFrame {

	private static final long serialVersionUID = -8441713146007061291L;

	protected Action expandAction;
	
    protected Action refreshAction;
	
	protected Action newPackageAction;
	
	protected Action deleteAction;
	
	protected Action renameAction;

	public FileTree fileTree;

	protected DefaultTreeModel treeModel;

	protected String newFileName;

	protected DefaultMutableTreeNode newTreeNode;

	protected File parentDir;

	protected TreePath pathToExpand;

	protected JPopupMenu popupMenu;

	protected JScrollPane scrollPaneWorkspace;

	protected File selectedFile;

	protected DefaultMutableTreeNode selectedNode;

	protected TreePath selectedPath;

	protected File workspaceDir;

	protected String workspaceName;
	
	protected M_TreeCellsEditor fileTreeCellEditor;
	
	protected Vector<TreePath> removedPaths;

	protected IconData createIconData(File file) {
		
		FileNode fileNode = new FileNode(file, workspaceName);
		IconData iconData = null;

		if (file.isDirectory()) {
			iconData = new IconData(Images.ICON_PACKAGE_16,
					Images.ICON_PACKAGE_EXPANDED_16, fileNode);
		} else {
			if (workspaceName.equals(GUIConstants.POLICY_WORKSPACE_NAME)) {
				iconData = new IconData(Images.ICON_POLICY_16, null, fileNode);
			} else {
				iconData = new IconData(Images.ICON_ASSERTION_16, null,
						fileNode);
			}
		}

		return iconData;
	}


	protected void createTree() {
		treeModel = new DefaultTreeModel(getWorkspaceRoot());

		fileTree = new FileTree(treeModel, this);

		M_TreeCellsRenderer renderer = new M_TreeCellsRenderer();
		fileTree.setCellRenderer(renderer);

		fileTreeCellEditor = new M_TreeCellsEditor(fileTree,
				renderer);
		fileTree.setCellEditor(fileTreeCellEditor);

		FileTreeSelectionListener selListner = new FileTreeSelectionListener();
		fileTree.addTreeSelectionListener(selListner);
		fileTreeCellEditor.addCellEditorListener(cellEditorListener);
		treeModel.addTreeModelListener(modelListener);
	}

	protected DefaultMutableTreeNode createTreeNode(File file) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
				createIconData(file));

		if (workspaceName.equals(GUIConstants.POLICY_WORKSPACE_NAME)
				&& IOUtils.hasPolicy(file)) {
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		} else if (workspaceName.equals(GUIConstants.ASSERTION_WORKSPACE_NAME)
				&& IOUtils.hasAssertion(file)) {
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		return node;
	}

	public boolean exists(String str) {
		File newFile = new File(parentDir, str);
		return newFile.exists();
	}

		
	abstract Action getActionDelete();
	
	abstract Action getActionNewPackage();

	protected void expand(){
		
		if (pathToExpand == null) {
			return;
		}

		if (fileTree.isExpanded(pathToExpand)) {
			fileTree.collapsePath(pathToExpand);
		} else {
			fileTree.expandPath(pathToExpand);
		}
	}
	
	protected Action getActionExpand() {
		expandAction = new AbstractAction() {

			private static final long serialVersionUID = -5974144368977218668L;

			public void actionPerformed(ActionEvent e) {
				expand();
			}
		};
		return expandAction;
	}

	abstract Action getActionNewFile();

	protected Action getActionRefresh() {
		Action refreshAction = new AbstractAction("Refresh            " +
				"    F5", null) {

			private static final long serialVersionUID = -435474568101434851L;

			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		};

		return refreshAction;
	}

	protected Action getActionRename() {
		Action renameAction = new AbstractAction("Rename                F2",
				Images.ICON_RENAME_Document_16) {

			private static final long serialVersionUID = 4475337812250680864L;

			public void actionPerformed(ActionEvent e) {
				rename(null);
			}			
		};
		return renameAction;
	}
	
	protected boolean isSelected(TreePath aPath){
		
		if (aPath == null) return false;
		
		TreePath[] allSelections = fileTree.getSelectionPaths();
		for (int i = 0; i < allSelections.length; i++){
			if (allSelections[i].equals(aPath)){
				return true;
			}
		}
		return false;
	}

	public JScrollPane getExplorer() {
		return scrollPaneWorkspace;
	}

	protected List getFileList(File dir) {
		List fileList;
		if (workspaceName.equals(GUIConstants.POLICY_WORKSPACE_NAME)) {
			fileList = IOUtils.listPolicies(dir);
		} else {
			fileList = IOUtils.listAssertions(dir);
		}
		return fileList;
	}

	protected String getInputFor(String purpose, String inputFileName) {
		String result = inputFileName;
		if (purpose.equals("New Assertion")) {
			if (result == null){
				result = MOptionPanes.getInput(GUIUtil.getEditor(fileTree), 
						"Please input a new assertion name", "");
			}
			
			while (result != null
					&& exists(result + GUIConstants.ASSERTION_EXTENSION)
					&& !selectedFile.getName().equals(result + GUIConstants
							.ASSERTION_EXTENSION)) {
				result = MOptionPanes.getReinput(GUIUtil.getEditor(fileTree), 
						Messages.FILE_EXISTS);
			}
			return result;
		}
		
		if (purpose.equals("New Policy")) {
			if (result == null){
			    result = MOptionPanes.getInput(GUIUtil.getEditor(fileTree), 
			    		"Please input a new policy name", "");
			}
			
			while (result != null
					&& exists(result + GUIConstants.POLICY_EXTENSION) 
			    	&& !selectedFile.getName().equals(result + GUIConstants
			    			.POLICY_EXTENSION)) {
				result = MOptionPanes.getReinput(GUIUtil.getEditor(fileTree),
						Messages.FILE_EXISTS);
			}
			return result;
		}
		
		if (result == null){
	    	result = MOptionPanes.getInput(GUIUtil.getEditor(fileTree),
	    			"Please input a new folder name", "");
		}
		
		while (result != null && exists(result) && !selectedFile.getName()
				.equals(result)) {
			result = MOptionPanes.getReinput(GUIUtil.getEditor(fileTree), 
					Messages.FILE_EXISTS);
		}
		return result;
	}

	/**
	 * Der selektierte Knoten
	 */
	public DefaultMutableTreeNode getSelectedNode() {
		return this.selectedNode;
	}

	/**
	 * Das workspaceDir.
	 */
	public String getWorkspaceDir() {
		return this.workspaceDir.toString();
	}

	private DefaultMutableTreeNode getWorkspaceRoot() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				createIconData(workspaceDir));
		List fileList = getFileList(workspaceDir);
	
		for (int i = 0; i < fileList.getItemCount(); i++) {
			DefaultMutableTreeNode node = createTreeNode(new File(fileList
					.getItem(i)));
			top.add(node);
		}	
		return top;
	}

	abstract void openFile(File file);
	
	abstract void rename (String inputFileName);
	
	abstract void delete();
	
    protected void removeSubSelections(){
		
		if (fileTree.getSelectionPaths() == null){
			return;
		}
		
		TreePath[] selectionPaths = fileTree.getSelectionPaths();	
		removedPaths = new Vector<TreePath>();
		for (int i = 0; i < selectionPaths.length; i++){
			TreePath aPath = selectionPaths[i];
			boolean isParentSelected = false;
			TreePath bufferPath = aPath.getParentPath();
			while ((bufferPath != null) && !isParentSelected){
				for (int j = 0; j < selectionPaths.length && !isParentSelected;
				          j++){
					if (bufferPath.equals(selectionPaths[j])){
						removedPaths.add(aPath);
						isParentSelected = true;
					}
				}
				bufferPath = bufferPath.getParentPath();
			}
		}
		
		for (int i = 0 ; i < removedPaths.size(); i++){
			TreePath aPath = removedPaths.get(i);
			fileTree.removeSelectionPath(aPath);
		}
		
	}
    
	private int countSubFiles(File aFile){
		if (!aFile.isDirectory()){
			return 1;
		} else {
			int counter = 0;
			File[] subFiles = aFile.listFiles();
			for (int i = 0; i < subFiles.length; i++){
				counter = counter + countSubFiles(subFiles[i]);
			}
			return counter;
		}
	}	
    
	protected int countSelectedFiles(){
		
		if (fileTree.getSelectionPaths() == null){
			return 0;
		}
		
		int counter = 0;
		TreePath[] selectionPaths = fileTree.getSelectionPaths();	
		for (int i = 0; i < selectionPaths.length; i++){
			TreePath aPath = selectionPaths[i];
			DefaultMutableTreeNode iTreeNode = (DefaultMutableTreeNode) 
			    aPath.getLastPathComponent();
            File iFile = fileTree.getFileNode(iTreeNode).getFile();
            counter = counter + countSubFiles (iFile);
		}
		return counter;
	}	

	protected void refresh() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (selectedFile == null) {
			return;
		}
		DefaultMutableTreeNode parent;
		if (selectedFile.isDirectory()) {
			parent = selectedNode;
			parentDir = selectedFile;
		} else {
			parent = (DefaultMutableTreeNode) selectedNode.getParent();
			parentDir = selectedFile.getParentFile();
		}

		TreePath parentPath = new TreePath(parent.getPath());
		parent.removeAllChildren();

		List fileList = getFileList(parentDir);

		for (int i = 0; i < fileList.getItemCount(); i++) {
			File aFile = new File(fileList.getItem(i));
			DefaultMutableTreeNode node = createTreeNode(aFile);
			parent.add(node);
		}
		
		treeModel.reload(parent);

		fileTree.expandPath(parentPath);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Entfernt die leere Datei
	 */
	public void removeEmptyFile() {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) newTreeNode
				.getParent();
		newTreeNode.removeFromParent();
		treeModel.nodeStructureChanged(parent);
		if (parent.getPath() != null) {
			fileTree.setSelectionPath(new TreePath(parent.getPath()));
		}
	}

	/**
	 * Bietet dem Benutzer die Moeglichkeit, das Arbeitsverziechnis des 
	 * Workspace zu aendern.
	 *
	 */
	public void switchWorkspace() {
		JFileChooser fileChooser = M_FileChooser
				.getWorkspaceChooser(workspaceDir.getPath());
		if (fileChooser.showOpenDialog(Frame.getFrames()[0]) != 
			       JFileChooser.APPROVE_OPTION) {
			return;
		}
		workspaceDir = fileChooser.getSelectedFile();
		treeModel.setRoot(getWorkspaceRoot());
		refresh();
	}

	/**
	 * Der selektierte Dateiknoten in Workspace
	 */
	public FileNode getSelectedFileNode (){
		return fileTree.getFileNode(selectedNode);
	}
	
	protected TreeModelListener modelListener = new TreeModelListener (){
    	public void treeNodesChanged(TreeModelEvent arg0) {
	    	String newFileName = fileTreeCellEditor.getCellEditorValue()
	    	    .toString();
    		rename (newFileName);
	    	fileTreeCellEditor.setEditImmediately(false);
	    }

	    public void treeNodesInserted(TreeModelEvent arg0) {
	    }

	    public void treeNodesRemoved(TreeModelEvent arg0) {
	    }

	    public void treeStructureChanged(TreeModelEvent arg0) {
	    }
	};
	
	protected CellEditorListener cellEditorListener = new CellEditorListener(){

		public void editingCanceled(ChangeEvent arg0) {
			fileTreeCellEditor.setEditImmediately(false);
		}

		public void editingStopped(ChangeEvent arg0) {			
		}
		
	};
	
	class FileTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			selectedPath = fileTree.getSelectionPath();
			selectedNode = fileTree.getSelectedTreeNode();
			pathToExpand = selectedPath;
			if (selectedNode != null) {
				selectedFile = fileTree.getFileNode(selectedNode).getFile();
			} else {
				selectedFile = null;
			}
		}
	}
	
}
