package gui.explorer;

import gui.editor.PolicyEditor;
import gui.images.Images;
import gui.util.GUIConstants;
import gui.util.GUIUtil;
import gui.util.M_FileChooser;
import gui.util.NewPolicyDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import util.Environment;
import util.FileOperation;
import util.IOUtils;
import util.MOptionPanes;

/**
 * Diese Klasse enthaelt die Methoden, die speziell fuer die Verwaltung von 
 * Policy Dateien entwickelt werden. 
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * 
 * @see         Explorer
 * @see         AssertionExplorer
 *
 */
public class PolicyExplorer extends Explorer{

	private static final long serialVersionUID = -4391721753523091512L;

	private PolicyEditor owner;

	private boolean isNewFile;
	
	private Action actionImport;
	
	private Action actionNewPolicy;
	
	private Action actionNormalize;
	
	private Action actionIntersect;
	
	private Action actionMerge;
	
	private JFileChooser fileChooser;
	
	private Vector<TreePath> addedPaths;

	public String workspacePath;
	

	public PolicyExplorer(PolicyEditor owner, String workspacePath) 
	{
		this.owner = owner;
		this.workspacePath = workspacePath;

		workspaceDir = new File(workspacePath);

		workspaceName = GUIConstants.POLICY_WORKSPACE_NAME;
		
		savePathFile();

		createTree();

		createPopupMenu();
		
		createFileChooser(workspacePath);

		fileTree.add(popupMenu);
		fileTree.addMouseListener(mouseListener);
		fileTree.addTreeSelectionListener(treeSelectionListener);
		fileTree.addKeyListener(hotkeysKeyListener);
		createWorkspacePane("Policy Workspace");

		getContentPane().add(scrollPaneWorkspace, BorderLayout.CENTER);
		
	}
	
	/**
	 * Legt ein neues Verzeichnis in dem Workspace an.
	 * @param wsName
	 */
	protected void createWorkspacePane(String wsName) {
		scrollPaneWorkspace = new JScrollPane();
		scrollPaneWorkspace.setViewportView(fileTree);

		final JButton btAssertionWS = new JButton();
		btAssertionWS.setText(wsName);
		btAssertionWS.setToolTipText("Click to change the " + wsName);
		ActionListener lsChangeWorkspace = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchWorkspace();
				workspacePath = getWorkspaceDir();
				savePathFile();
			}
		};
		btAssertionWS.addActionListener(lsChangeWorkspace);

		scrollPaneWorkspace.setColumnHeaderView(btAssertionWS);
	}

	
	/**
	 * Schreibt den neuen Pfad des WorkSpace in einer Datei auf.
	 *
	 */
	public void savePathFile()
	{
		try 
		{
			Environment.checkWorkspacePathFile();
			FileOperation pWrite = new FileOperation();
			pWrite.writeFile (Environment.getPolicyWorkspacePathFile(),
					workspacePath);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}
	
	private void createPopupMenu() 
	{		
		popupMenu = new JPopupMenu();

		actionImport = getActionImport();
		popupMenu.add(actionImport);
		
		popupMenu.addSeparator();
        
		expandAction = getActionExpand();
		popupMenu.add(expandAction);

		refreshAction = getActionRefresh();
		popupMenu.add(refreshAction);
		
		popupMenu.addSeparator();

		newPackageAction = getActionNewPackage();
		popupMenu.add(newPackageAction);

		actionNewPolicy = getActionNewFile();
		popupMenu.add(actionNewPolicy);

		deleteAction = getActionDelete();
		popupMenu.add(deleteAction);

		renameAction = getActionRename();
		popupMenu.add(renameAction);
		
		popupMenu.addSeparator();
		
		actionNormalize = getActionNormalize();
		popupMenu.add(actionNormalize);
		
		actionIntersect = getActionIntersect();
		popupMenu.add(actionIntersect);
		
		actionMerge = getActionMerge();
		popupMenu.add(actionMerge);

	}
	
	
	/**
	 * Zaehlt, wie viele Policies in PolicyWorkspace selektiert werden.
	 */
	public int countSelectedPolicies()
	{
		return countSelectedFiles();
	}
	
	private void expandNode(DefaultMutableTreeNode parent) 
	{
		
		if (parent.getChildCount() == 0)
		{
			return;
		}
		
		DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent
		    .getFirstChild();

		if (flag == null) 
		{
			return;
		}
        
		Object obj = flag.getUserObject();
		if (!(obj instanceof Boolean)) 
		{
			return;
		}

		parent.removeAllChildren(); 

		File file = fileTree.getFileNode(parent).getFile();
		List fileList = getFileList(file);

		for (int i = 0; i < fileList.getItemCount(); i++) 
		{
			File aFile = new File(fileList.getItem(i));
			DefaultMutableTreeNode node = createTreeNode(aFile);
			parent.add(node);
			expandNode(node);
		}
        
	}
	
	/**
	 * Erweitert den ganzen Baum.
	 *
	 */
	public void expandTree() 
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) 
		    fileTree.getModel().getRoot();
        
		for (int i = 0; i < root.getChildCount(); i++) 
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
			    root.getChildAt(i);
			expandNode(node);
		}
	}


	private void createFileChooser(String currentPath) 
	{
		fileChooser = M_FileChooser.getPolicyFileChooser(currentPath);
	}
	
	/**
	 * Legt ein neues Paket an.
	 */
	public void createNewPackage() 
	{
		if (selectedFile == null) 
		{
			return;
		}

		String newDirName = MOptionPanes.getInput(GUIUtil.getEditor(fileTree),
			"Please input the package name.", "");

		if (newDirName == null) 
		{
			return;
		}

		DefaultMutableTreeNode parent;
		
		if (selectedFile.isDirectory()) 
		{
			parent = selectedNode;
			parentDir = selectedFile;
		} 
		else 
		{
			parent = (DefaultMutableTreeNode) selectedNode.getParent();
			parentDir = selectedFile.getParentFile();
		}

		TreePath parentPath = new TreePath(parent.getPath());
		File newDir = new File(parentDir, newDirName);
		
		try 
		{
			DefaultMutableTreeNode child;
			int sum = treeModel.getChildCount(parent);			
			int index = 0;
						
			while ((index < sum))
			{

				child = (DefaultMutableTreeNode)treeModel.getChild
				    (parent, index);
				String childName = child.toString();
				
				if ((childName.endsWith(".xml")) ||
					(childName.endsWith(".asrt"))) 
				{      
					break;
				} 
				else if ((newDirName.compareToIgnoreCase(childName) < 0)) 
				{
					break;			
				}
				index++;
			}
			
			if (newDir.mkdirs()) 
			{
				DefaultMutableTreeNode aNewTreeNode = new 
				    DefaultMutableTreeNode(createIconData(newDir));
				parent.insert(aNewTreeNode, index);
				treeModel.nodeStructureChanged(parent);				
				selectedPath = parentPath.pathByAddingChild(aNewTreeNode);
				fileTree.setSelectionPath(selectedPath);
				fileTree.scrollPathToVisible(selectedPath);
			
			} 
			else 
			{
				MOptionPanes.showError(GUIUtil.getEditor(fileTree),
					"The package exists already.");
			}
		} 
		catch (SecurityException ex) 
		{
			ex.printStackTrace();
			MOptionPanes.showError(GUIUtil.getEditor(fileTree),
				"Security Exception by creating a new package.");
		}
	}
	
	/**
	 * Legt eine neue Policydatei mit den gegebenen Properties.
	 * 
	 * @param file           die neue Datei
	 * @param policyName      der Name der Policy
	 * @param Id              das Id der Policy
	 * @param description     die Beschreibung der Policy
	 */
	public void createNewFile(File file, String policyName, String Id, 
			 String description) 
	{
		owner.newDocument(file, policyName, Id, description);
		setNewFile(true);
	}

	
	/**
	 * Bei dieser Methode wird zuerst der Inputdialog geladen, wenn der 
	 * Benutzer alle benoeigte Informationen eingibt,  dann wird eine neue
	 * Poliey angelegt und danach im neuen DokumentPane geoeffnet.
	 *
	 */
	public void createNewPolicy() 
	{
		if (selectedFile == null) 
		{
			MOptionPanes.showError(owner,
				"Please selecte a package in the \"Policy Workspace\"");
			return;
		}

		Runnable expandSelectedNode = new Runnable() 
		{
			public void run() 
			{
				if (selectedFile.isDirectory()) 
				{
					DefaultMutableTreeNode treeNode = selectedNode;
					fileTree.expandPath(new TreePath(treeNode.getPath()));
				}
			}
		};
		SwingUtilities.invokeLater(expandSelectedNode);

		Runnable createNewPolicy = new Runnable() 
		{
			public void run() 
			{				
				DefaultMutableTreeNode parent;
				String parentPath;
				if (selectedFile.isDirectory()) 
				{
					parent = selectedNode;
					parentDir = selectedFile;
					parentPath = selectedFile.getPath();
				} 
				else 
				{
					parent = (DefaultMutableTreeNode) selectedNode.getParent();
					parentDir = fileTree.getFileNode(parent).getFile();
					parentPath = fileTree.getFileNode(parent).getFile()
						.getPath();
				}
				
				NewPolicyDialog newPolicyDialog = new NewPolicyDialog(PolicyExplorer.this);
				newPolicyDialog.setLocationRelativeTo(null);
				newPolicyDialog.setVisible(true);
				
				String newFileName = newPolicyDialog.getFileName();

				if (newFileName == null) 
				{
					return;
				}
				
				String policyName = newPolicyDialog.getPolicyName();
				String Id = newPolicyDialog.getID();
				String description = newPolicyDialog.getDescription();

				String newFilePath = parentPath
					+ System.getProperty("file.separator") + newFileName
					+ GUIConstants.POLICY_EXTENSION;

				File newFile = new File(newFilePath);

				DefaultMutableTreeNode child;	
				
				int sum = treeModel.getChildCount(parent);						
				int index = 0;
				
				while ((index < sum))
				{
					child = (DefaultMutableTreeNode)treeModel.getChild
					    (parent, index);
					String childName = child.toString();
					if ( (childName.endsWith(".xml"))  && 
						(newFileName.compareToIgnoreCase(childName) <= 0)) 
					{
						break;
					}
					index++;
				}

				newTreeNode = new DefaultMutableTreeNode(
					createIconData(newFile));
				parent.insert(newTreeNode, index);
				treeModel.nodeStructureChanged(parent);
				TreePath newTreePath = new TreePath(newTreeNode.getPath());
				fileTree.setSelectionPath(newTreePath);
				fileTree.scrollPathToVisible(newTreePath);

				createNewFile(newFile, policyName, Id, description);
			}
		};

		SwingUtilities.invokeLater(createNewPolicy);
	}
	
	private void copyFile(String oldPath, String newPath) 
	{ 
		try 
		{
			int bytesum = 0;     
			int byteread = 0;     
			File oldfile = new File(oldPath);     
			if (oldfile.exists()) 
			{
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];     
				while ((byteread = inStream.read(buffer)) !=  -1) 
				{  
					bytesum +=  byteread;       
					fs.write(buffer, 0, byteread);                    
				}     
				inStream.close();                 
			}     
		}     
		catch (Exception e) 
		{     
			MOptionPanes.showError(this, "Error by coping the file.");  
			e.printStackTrace();     
		}     

	}     

	public void importPolicy()
	{
		
		if (selectedFile == null) 
		{
			MOptionPanes.showError(owner,
				"Please selecte a package in the \"Policy Workspace\"");
			return;
		}
		
		if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}
		
		File fileToImport = fileChooser.getSelectedFile();

		String oldPath = fileToImport.getPath();
		
		if (fileToImport == null)
		{
			return;
		}
		
		if (!fileToImport.getName().endsWith(GUIConstants.POLICY_EXTENSION))
		{
			MOptionPanes.showError(this, "You must select a XML-file.");
			return;
		}
		
		DefaultMutableTreeNode parent;
		String parentPath;
		
		if (selectedFile == null)
		{
			fileTree.setSelectionRow(0);
		}
		
		if (selectedFile.isDirectory()) 
		{
			parent = selectedNode;
			parentPath = selectedFile.getPath();
		} 
		else 
		{
			parent = (DefaultMutableTreeNode) selectedNode.getParent();
			parentPath = fileTree.getFileNode(parent).getFile()
				.getPath();
		}
		
		String newPath = parentPath + "\\" + fileToImport.getName();
		File newFile = new File(newPath);
		
		if (newFile.exists())
		{
			MOptionPanes.showError(this, newPath + "is already exists.");
			return;
		}
		
		try 
		{
			newFile.createNewFile();
		}     
		catch (Exception e) 
		{     
			MOptionPanes.showError(this, "Error by creating the file.");  
			e.printStackTrace();     
		}  
		    
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		copyFile(oldPath, newPath);
		refresh();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
	    
	}
	
	protected Action getActionImport()
	{
		Action importAction = new AbstractAction("Import") 
		{
			private static final long serialVersionUID = 2086102311838235866L;

			public void actionPerformed(ActionEvent e) 
			{
				importPolicy();
			}
			
		};

		return importAction;
	}

	@Override
	protected Action getActionNewPackage() 
	{
		Action newPackageAction = new AbstractAction("New Package",
			Images.ICON_PACKAGE_16) 
		{

			private static final long serialVersionUID = -4584155625161387736L;

			public void actionPerformed(ActionEvent e) 
			{
				createNewPackage();
			}
		};

		return newPackageAction;
	}

	
	protected void delete()
	{
		fileTree.repaint();
		TreePath[] selectionPaths = fileTree.getSelectionPaths();
		fileTree.setSelectionPath(null);
		
		for (int i = 0; i < selectionPaths.length; i++)
		{
			TreePath path = selectionPaths[i];
		
			if (path == null) 
			{
				return;
			}
		
			if (path.getParentPath() == null) 
			{
				JOptionPane.showMessageDialog(Frame.getFrames()[0],
					"You cannot delete Policy Workspace.", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			DefaultMutableTreeNode iTreeNode = (DefaultMutableTreeNode) path
				.getLastPathComponent();
			FileNode iFileNode = fileTree.getFileNode(iTreeNode);
			if (iFileNode == null) 
			{
				return;
			}

			File file = iFileNode.getFile();

			if (file != null) 
			{
				if (file.isDirectory()) 
				{
					if (JOptionPane.showConfirmDialog(owner,
						"Do you want to delete \nPackage \""
						+ file.getName() + "\" ?",
						"Comformation", JOptionPane.YES_NO_OPTION) == 
							JOptionPane.YES_OPTION) 
					{
			    		
						setCursor(Cursor.getPredefinedCursor
								(Cursor.WAIT_CURSOR));
						owner.closeOpenedFiles(file);
						DefaultMutableTreeNode parent = 
							(DefaultMutableTreeNode) iTreeNode
							.getParent();
						if (IOUtils.deleteFiles(file)) 
						{
							iTreeNode.removeFromParent();
						}
						treeModel.nodeStructureChanged(parent);
						setCursor(Cursor.getPredefinedCursor
								(Cursor.DEFAULT_CURSOR));
					}
				} 
				else 
				{
					if (JOptionPane.showConfirmDialog(owner,
						"Do you want to delete \npolicy \""
						+ file.getName() + "\" ?",
						"Comformation", JOptionPane.YES_NO_OPTION) 
						    == JOptionPane.YES_OPTION) 
					{
			    		
						setCursor(Cursor.getPredefinedCursor
								(Cursor.WAIT_CURSOR));
						owner.closeOpenedFiles(file);
						DefaultMutableTreeNode parent = 
							(DefaultMutableTreeNode) iTreeNode
							.getParent();
						if (IOUtils.deleteFiles(file)) 
						{
							iTreeNode.removeFromParent();
						}
						treeModel.nodeStructureChanged(parent);
						setCursor(Cursor.getPredefinedCursor
								(Cursor.DEFAULT_CURSOR));
					}
				}
				fileTree.setSelectionRow(0);
			}
		}		
	}
	
	@Override
	protected Action getActionDelete() 
	{
		Action iAction = new AbstractAction("Delete            " +
				"        Delete",
			Images.ICON_DELETE_Document_16) 
		{
			private static final long serialVersionUID = -6282765029538618469L;

			public void actionPerformed(ActionEvent e) 
			{
				delete();
			}
		};
		return iAction;
	}


	@Override
	protected Action getActionNewFile() 
	{
		Action iAction = new AbstractAction("New Policy         " +
				"  Insert", Images.ICON_POLICY_16) 
		{
			private static final long serialVersionUID = -7211749312468286881L;

			public void actionPerformed(ActionEvent e) 
			{
				createNewPolicy();
			}
		};
		return iAction;
	}

	
	private Action getActionNormalize() 
	{
		Action normalizeAction = new AbstractAction("Normalize         " +
				"    Alt+N") 
		{

			private static final long serialVersionUID = 3116582981864970202L;

			public void actionPerformed(ActionEvent e) 
			{
				normalize();
			}
		};

		return normalizeAction;
	}
	
	private Action getActionIntersect() 
	{
		Action intersectAction = new AbstractAction("Intersect           " +
				"    Alt+I") 
		{

			private static final long serialVersionUID = 0L;

			public void actionPerformed(ActionEvent e) 
			{
				intersect();
			}
		};

		return intersectAction;
	}

	private Action getActionMerge() 
	{
		Action mergeAction = new AbstractAction("Merge             " +
				"       Alt+M") 
		{

			private static final long serialVersionUID = 0L;

			public void actionPerformed(ActionEvent e) 
			{
				merge();
			}
		};

		return mergeAction;
	}
	
	@Override
		protected void openFile(File file) 
		{
			owner.openDocument(file);
		}
	
	
	
	
	private void arrangeSubFiles(TreePath aPath )
	{
		
		if (!fileTree.isExpanded(aPath)) 
		{
			fileTree.expandPath(aPath);
		}
		
		DefaultMutableTreeNode iTreeNode = (DefaultMutableTreeNode) 
		    aPath.getLastPathComponent();
		int numberOfChildren = iTreeNode.getChildCount();
		DefaultMutableTreeNode subNode;
		for (int i = 0 ; i < numberOfChildren ; i ++)
		{
			subNode = (DefaultMutableTreeNode) iTreeNode.getChildAt(i);
			Object obj = subNode.getUserObject();
			if (!(obj instanceof Boolean)) 
			{
				if (fileTree.getFileNode(subNode) != null)
				{
					File iFile = fileTree.getFileNode(subNode).getFile();
					TreePath subPath = aPath.pathByAddingChild(subNode);
				
					if (iFile.isDirectory()) 
					{
						arrangeSubFiles(subPath);
					} 
					else 
					{
						addedPaths.add(subPath);
					}
				}
			}
		}
	}	
	
	
	private void arrangeSelectedPolicies()
	{
		
		if (fileTree.getSelectionPaths() == null)
		{
			return;
		} 
		
		TreePath[] selectionPaths = fileTree.getSelectionPaths();
		
		removedPaths = new Vector<TreePath>();
			addedPaths = new Vector<TreePath>();
		
				for (int i = 0; i < selectionPaths.length; i++)
				{
					TreePath aPath = selectionPaths[i];
					DefaultMutableTreeNode iTreeNode = (DefaultMutableTreeNode)
					    aPath.getLastPathComponent();
					File iFile = fileTree.getFileNode(iTreeNode).getFile();
					if (iFile.isDirectory()) 
					{
						removedPaths.add(aPath);
						arrangeSubFiles(aPath);
					}
				}		
		
		for (int i = 0; i < removedPaths.size(); i++)
		{
			TreePath aPath = removedPaths.get(i);
			fileTree.removeSelectionPath(aPath);
		}
		
		for (int i = 0; i < addedPaths.size(); i++)
		{
			TreePath aPath = addedPaths.get(i);
			fileTree.addSelectionPath(aPath);
		}
	}

	/**
	 * Alle in PolicyWorkspace selektierten Policies
	 */
	public Vector<File> getSelectedPolicies() {
		
	    arrangeSelectedPolicies();
		
	    TreePath[] paths = fileTree.getSelectionPaths();

	    Vector<File> selectedPolicies = new Vector<File>();

	    if (paths == null || paths.length == 0) 
        {
        	return null;
        }

	    for (int i = 0; i < paths.length; i++) 
        {
         	DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) paths[i]
            	.getLastPathComponent();
        	FileNode fileNode = fileTree.getFileNode(treeNode);
	        File file = fileNode.getFile();
			
	        if (file.isFile()) 
            {
             	selectedPolicies.add(file);
            }			
         }

     	return selectedPolicies;
     }

	
	@Override
	protected void rename(String inputFileName)
	{
		
		if (selectedFile == null) 
		{
			return;
		}

		String parentPath;
		String newFilePath;
		File newFile;

		parentDir = selectedFile.getParentFile();
		parentPath = parentDir.getPath();
		
		if (selectedFile.getPath().equals(workspacePath))
		{
			JOptionPane.showMessageDialog(Frame.getFrames()[0],
				"You cannot rename the Policy Workspace.", 
				"Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (selectedFile.isDirectory()) 
		{
			newFileName = getInputFor("New Folder", inputFileName);
			if (newFileName == null) 
			{
				return;
			}
			newFilePath = parentPath
				+ System.getProperty("file.separator")
				+ newFileName;
		} 
		else 
		{
			newFileName = getInputFor("New Policy", inputFileName);
			if (newFileName == null) 
			{
				return;
			}
			newFilePath = parentPath
				+ System.getProperty("file.separator")
				+ newFileName + GUIConstants.POLICY_EXTENSION;
		}

		if (newFileName == null) 
		{
			return;
		}

		newFile = new File(newFilePath);
		selectedFile.renameTo(newFile);

		selectedNode.setUserObject(createIconData(newFile));
		treeModel.nodeStructureChanged(selectedNode);
		fileTree.setSelectionPath(selectedPath);
	}

	private void normalize()
	{
		owner.normalize();
		
	}
	
	private void intersect()
	{
		owner.intersect();
		
	}
	
	private void merge()
	{
		owner.merge();	
	}
	
	private void enablePopupMenuItems()
	{
		if (selectedFile == null) 
		{
			actionImport.setEnabled(false);
			expandAction.setEnabled(false);
			refreshAction.setEnabled(false);
			newPackageAction.setEnabled(false);
			actionNewPolicy.setEnabled(false);
			deleteAction.setEnabled(false);
			renameAction.setEnabled(false);
			actionNormalize.setEnabled(false);
			actionIntersect.setEnabled(false);
			actionMerge.setEnabled(false);
		} 
		else 
		{     	
			if (fileTree.getSelectionPaths().length == 1)
			{
				actionImport.setEnabled(true);
				expandAction.setEnabled(true);
				refreshAction.setEnabled(true);
				newPackageAction.setEnabled(true);
				actionNewPolicy.setEnabled(true);
				deleteAction.setEnabled(true);
				renameAction.setEnabled(true);
			} 
			else 
			{
				actionImport.setEnabled(false);
				expandAction.setEnabled(false);
				refreshAction.setEnabled(false);
				newPackageAction.setEnabled(false);
				actionNewPolicy.setEnabled(false);
				deleteAction.setEnabled(true);
				renameAction.setEnabled(false);
			}
        	
			if (countSelectedPolicies() == 0)
			{
				actionNormalize.setEnabled(false);
				actionIntersect.setEnabled(false);
				actionMerge.setEnabled(false);
			} 
			else if (countSelectedPolicies() == 1)
			{
				actionNormalize.setEnabled(true);
				actionIntersect.setEnabled(false);
				actionMerge.setEnabled(false);
			} 
			else 
			{
				actionNormalize.setEnabled(true);
				actionIntersect.setEnabled(true);
				actionMerge.setEnabled(true);
			}
		}
	}
	
	/**
	 * Ob die eine neue Datei ist.
	 * 
	 * @see   #setNewFile(boolean)
	 */
	public boolean isNewFile() 
	{
		return this.isNewFile;
	}

	/**
	 * Stellt den Zustand der Datei auf "neu" oder nicht "neu".
	 * 
	 * @see    #isNewFile
	 */
	public void setNewFile(boolean isNewFile) 
	{
		this.isNewFile = isNewFile;
	}

	private MouseListener mouseListener = new MouseListener(){
		public void mouseClicked(MouseEvent e) 
		{
			if (e.getClickCount() == 2) 
			{
				int x = e.getX();
				int y = e.getY();
				TreePath path = fileTree.getClosestPathForLocation(x, y);

				if (path == null) 
				{
					return;
				}

				fileTree.setSelectionPath(path);
				DefaultMutableTreeNode iTreeNode = fileTree.getSelectedTreeNode();
				File iFile = fileTree.getFileNode(iTreeNode).getFile();
				if (iFile.isDirectory()) 
				{
					return;
				}
				openFile(iFile);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) 
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) 
		{
			if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) 
			{
				int x = e.getX();
				int y = e.getY();

				TreePath path = fileTree.getClosestPathForLocation(x, y);

				if (path == null) 
				{
					return;
				}

				int offmask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent
				    .BUTTON1_DOWN_MASK;
				int onmask = MouseEvent.CTRL_DOWN_MASK;
				if ((e.getModifiersEx() & (onmask | offmask)) != onmask) 
				{
					if (!isSelected(path))
					{
						fileTree.setSelectionPath(path);
					}
				}
				
				fileTree.scrollPathToVisible(path);
				
				if (fileTree.isExpanded(path)) 
				{
					expandAction.putValue(Action.NAME, "Collapse");
				} 
				else 
				{
					expandAction.putValue(Action.NAME, "Expand");
				}

				removeSubSelections();
				enablePopupMenuItems();
				popupMenu.show(fileTree, x, y);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) 
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) 
		{
		}
	};
	
	private TreeSelectionListener treeSelectionListener = new 
	    TreeSelectionListener(){
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeSelectionListener#valueChanged
		 * (javax.swing.event.TreeSelectionEvent)
		 */
		public void valueChanged(TreeSelectionEvent e) 
		{
			owner.enableProcessButtons();
		}
	};


	private KeyListener hotkeysKeyListener = new KeyListener(){
		public void keyPressed(KeyEvent e) 
		{
			
			if (getSelectedNode() == null)
			{
				return;
			}
			
			switch (e.getKeyCode()) 
			{
				case KeyEvent.VK_F2:
					fileTreeCellEditor.setEditImmediately(true);
					break;
				case KeyEvent.VK_F5:
					refresh();
					break;
				case KeyEvent.VK_INSERT:
					createNewPolicy();
					break;
				case KeyEvent.VK_DELETE:
					delete();
					break;
				case KeyEvent.VK_ENTER:
					if (selectedFile.isDirectory())
					{
						pathToExpand = selectedPath;
						expand();
					} 
					else 
					{
						openFile(selectedFile);
					}
					break;
				case KeyEvent.VK_N:
					if (e.getModifiers() == KeyEvent.ALT_MASK)
					{
						owner.normalize();
					}
					break;
				case KeyEvent.VK_I:
					if (e.getModifiers() == KeyEvent.ALT_MASK)
					{
						boolean b1 = (countSelectedPolicies() >= 2);
						if (b1) 
						{
							owner.intersect();
						} 
						else 
						{
							MOptionPanes.showError(null, "you muss select " +
									"more than 2 policy.");
							return;
						}	
					}
					break;
				case KeyEvent.VK_M:
					if (e.getModifiers() == KeyEvent.ALT_MASK)
					{
						boolean b1 = (countSelectedPolicies()  >= 2);
						if (b1) 
						{
							owner.merge();
						} 
						else 
						{
							MOptionPanes.showError(null, "you muss select" +
									" more than 2 policy.");
							return;
						}	
					}
					break;
			}


		}

		public void keyReleased(KeyEvent e) 
		{		
		}

		public void keyTyped(KeyEvent e) 
		{
		}
	};
	
}
