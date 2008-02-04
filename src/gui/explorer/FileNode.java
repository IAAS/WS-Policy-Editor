package gui.explorer;


import gui.images.Images;
import gui.util.GUIConstants;

import java.awt.Frame;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import util.IOUtils;


/**
 * Jeder Knoten in dem Baum in Workspace ist ein FileNode. In einem 
 * DomTreeNode ist ein Node eingekapselt. Ein Node repraesentiert eine Datei
 * bzw. ein Paket in dem Baum in Workspace
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class FileNode implements Transferable, Serializable {

	private static final long serialVersionUID = 7537217343188622880L;

	private File file;

	private String workspaceName;

	public static final DataFlavor NODE_FLAVOR = new DataFlavor(
			FileNode.class, "File Node");

	protected static final DataFlavor flavors[] = { NODE_FLAVOR };
	
	public FileNode(File file, String workspaceName) {
		this.file = file;
		this.workspaceName = workspaceName;
	}

	/**
	 * Die entsprechende Datei, die diese Node repraesentiert.
	 */
	public File getFile() {
		return file;
	}

	@Override
	/**
	 * Liefert den Inhalt des Knotens, die in dem Baum angezeigt werden.
	 */
	public String toString() {
		return file.getName().length() > 0 ? file.getName() : file.getPath();
	}

	private boolean isPolicyWorkspace() {
		return workspaceName.equals(GUIConstants.POLICY_WORKSPACE_NAME);
	}

	private boolean isAssertionWorkspace() {
		return workspaceName.equals(GUIConstants.ASSERTION_WORKSPACE_NAME);
	}

	private List getFileList(File dir) {
		List fileList;
		if (isPolicyWorkspace()) {
			fileList = IOUtils.listPolicies(dir);
		} else {
			fileList = IOUtils.listAssertions(dir);
		}
		return fileList;
	}

	private IconData createIconData(File aFile) {
		FileNode fileNode = new FileNode(aFile, workspaceName);
		IconData iconData = null;

		if (aFile.isDirectory()) {
			iconData = new IconData(Images.ICON_PACKAGE_16,
					Images.ICON_PACKAGE_EXPANDED_16, fileNode);
		} else {
			if (isPolicyWorkspace()) {
				iconData = new IconData(Images.ICON_POLICY_16, null, fileNode);
			} else if (isAssertionWorkspace()) {
				iconData = new IconData(Images.ICON_ASSERTION_16, null, 
						fileNode);
			}
		}

		return iconData;
	}

	private DefaultMutableTreeNode createTreeNode(File aFile) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
				createIconData(aFile));

		if (isPolicyWorkspace() && IOUtils.hasPolicy(aFile)) {
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		} else if (isAssertionWorkspace() && IOUtils.hasAssertion(aFile)) {
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		return node;
	}

	/**
	 * Erweitet ein Paket, zeigt alle darunter liegenden Datein an.
	 * 
	 * @param parent   der Elternknoten 
	 * @return         ob diese Operation komplett ausgefuehrt wurde.    
	 */
	public boolean expand(DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent
				.getFirstChild();

		if (flag == null) 
			return false;
		
		Object obj = flag.getUserObject();
		if (!(obj instanceof Boolean)) {
			return false;
		}

		parent.removeAllChildren(); 
		
		List fileList = getFileList(file);

		for (int i = 0; i < fileList.getItemCount(); i++) {
			File aFile = new File(fileList.getItem(i));
			DefaultMutableTreeNode node = createTreeNode(aFile);
			parent.add(node);
		}

		return true;
	}

	/**
	 * Ob unter diesem Knoten einen Knoten exisiert, der ein Dir repaesentiert.
	 */
	public boolean hasSubDirs() {
		File[] files = listFiles();
		if (files == null)
			return false;
		for (int k = 0; k < files.length; k++) {
			if (files[k].isDirectory())
				return true;
		}
		return false;
	}

	private File[] listFiles() {
		if (!file.isDirectory())
			return null;
		try {
			return file.listFiles();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(Frame.getFrames()[0],
					"Error reading directory " + file.getAbsolutePath()
							+ "by listing files.", "Error",
					JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}
	
	/* ******************************************************************** */
	/* --------------------Transferable interface methods ----------------- */
	/* ******************************************************************** */
	
    /**
     * Transferable interface methods 
     */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(NODE_FLAVOR)) {
			return this;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	/**
	 * Transferable interface methods 
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/**
	 * Transferable interface methods 
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(NODE_FLAVOR);
	}
}
