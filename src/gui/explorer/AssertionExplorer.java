package gui.explorer;

import gui.editor.AssertionEditor;
import gui.editor.PolicyEditor;
import gui.images.Images;
import gui.util.GUIConstants;
import gui.util.GUIUtil;
import gui.util.InputDialogPattern;
import gui.util.NewAssertionDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import util.Environment;
import util.FileOperation;
import util.IOUtils;
import util.MOptionPanes;


/**
 * Diese Klasse enthaelt die Methoden, die speziell fuer die Verwaltung von 
 * Assertion Dateien entwickelt werden. 
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * 
 * @see         Explorer
 * @see         PolicyExplorer
 *
 */
public class AssertionExplorer extends Explorer{

	private static final long serialVersionUID = -8743061504054804298L;

	private PolicyEditor owner;
	
	private Action actionNewAssertion;
	
	private Action actionSetDefaultNS;

	public String workspacePath;
    
	public AssertionExplorer(PolicyEditor owner, String workspacePath) {
		this.owner = owner;
		this.workspacePath = workspacePath;
		
		workspaceDir = new File(workspacePath);

		workspaceName = GUIConstants.ASSERTION_WORKSPACE_NAME;

		savePathFile();
		
		createTree();

		createPopupMenu();

		fileTree.add(popupMenu);
		fileTree.addMouseListener(mouseListener);
		fileTree.addKeyListener(hotkeysKeyListener);

		createWorkspacePane("Assertion Workspace");

		getContentPane().add(scrollPaneWorkspace, BorderLayout.CENTER);
	}
	
	/**
	 * Legt ein neues Verzeichnis in Workspace an.
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
	 * Speichert den neuen Pfad des WorkSpace in System.
	 *
	 */
	public void savePathFile()
	{			
		try 
		{	
			Environment.checkWorkspacePathFile();
			FileOperation pWrite = new FileOperation();
			pWrite.writeFile (Environment.getAssertionWorkspacePathFile(), 
					workspacePath);		
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
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

		InputDialogPattern newPackageDialog = new InputDialogPattern (true);
		newPackageDialog.setLabelTitel("Package Name : ", "Default Prefix : ", 
				"Default NamespaceURI : ");
		newPackageDialog.setInputFieldEditable(true);
		newPackageDialog.setLocationRelativeTo(null);
		newPackageDialog.setVisible(true);
		      	
		String defaultPrefix = newPackageDialog.getPrefix();
		String defaultNsURI = newPackageDialog.getNsURI();
		
		if ((defaultPrefix == null) || (defaultNsURI == null))
		{
			return;
		} 
		
		String newDirName = newPackageDialog.getValue();

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

				child = (DefaultMutableTreeNode)treeModel.getChild(parent, 
						index);
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
				
				if (!defaultPrefix.isEmpty() || !defaultNsURI.isEmpty())
				{
					try 
					{
						File defaultNSFile = new File(newDir.getAbsolutePath(),
								".DefaultNS");
					  
						if (!defaultNSFile.exists()) 
						{
							defaultNSFile.createNewFile();
						}

						BufferedWriter writer = new BufferedWriter(new 
								FileWriter(defaultNSFile));
						writer.write(defaultPrefix);
						writer.newLine();
						writer.write(defaultNsURI);
						writer.newLine();
						writer.close();
					  	
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
						MOptionPanes.showError(Frame.getFrames()[0],
							"I/O error by writing defaultNS.");
					}
				}
				
				DefaultMutableTreeNode aNewTreeNode = new DefaultMutableTreeNode(
					createIconData(newDir));
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
	 * Bei dieser Methode wird zuerst der Inputdialog geladen, wenn der 
	 * Benutzer alle benoeigten Informationen eingibt,  dann wird ein neues
	 * Assertion angelegt und Assertion Editor wird geladen.
	 *
	 */
	public void createNewAssertion() 
	{
		Runnable expandSelectedNode = new Runnable() 
		{
			public void run() 
			{
				if (selectedFile == null) 
				{
					return;
				}
				if (selectedFile.isDirectory()) 
				{
					DefaultMutableTreeNode treeNode = selectedNode;
					fileTree.expandPath(new TreePath(treeNode.getPath()));
				}
			}
		};
		SwingUtilities.invokeLater(expandSelectedNode);

		Runnable createNewAssertion = new Runnable() 
		{
			public void run() 
			{
				if (selectedFile == null) 
				{
					MOptionPanes
						.showError(owner,
						"Please selecte a package in the \"Assertion " +
						"Workspace\"");
					return;
				}

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
				String defaultPrefix = "";
				String defaultNsURI = "";
				try 
				{
					File defaultNSFile = new File(parentPath,".DefaultNS");
					if (defaultNSFile.exists())
					{
						BufferedReader reader = new BufferedReader(new 
								FileReader(defaultNSFile));
						defaultPrefix = reader.readLine();
						defaultNsURI = reader.readLine();
						reader.close();	
					}
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					MOptionPanes.showError(Frame.getFrames()[0],
						"I/O error by reading defaultNS.");
				}
    		    
				NewAssertionDialog newAssertionDialog = new NewAssertionDialog
				    (AssertionExplorer.this, defaultPrefix, defaultNsURI);
				newAssertionDialog.setLocationRelativeTo(null);
				newAssertionDialog.setVisible(true);
			   
				String prefix = newAssertionDialog.getPrefix();
				String nsURI = newAssertionDialog.getNsURI();

				if ((prefix == null) || (nsURI == null))
				{
					return;
				}
     			
				String assertionName = newAssertionDialog.getAssertionName();
				String description = newAssertionDialog.getDescription();
       
				newFileName = newAssertionDialog.getFileName();
     			
				String newFilePath = parentPath
					+ System.getProperty("file.separator") + newFileName
					+ GUIConstants.ASSERTION_EXTENSION;

				File newFile = new File(newFilePath);
				
				DefaultMutableTreeNode child;	
				
				int sum = treeModel.getChildCount(parent);						
				int index = 0;
				
				while ((index < sum))
				{
					child = (DefaultMutableTreeNode)treeModel.getChild(parent, 
							index);
					String childName = child.toString();
					if (childName.endsWith(".asrt")) 
					{  
						if (newFileName.compareToIgnoreCase(childName) <= 0) 
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

				createNewFile(newFile, assertionName, prefix, nsURI, 
						description);
			}
		};

		SwingUtilities.invokeLater(createNewAssertion);
	}

	/**
	 * Legt eine neue Assertion Datei mit den angegebenen Properties
	 * 
	 * @param aFile           die neue Datei
	 * @param assertionName   der Name von Assertion
	 * @param prefix          das Praefix von Assertion
	 * @param nsURI           das NamespaceURI von Assertion
	 * @param description     die Beschreibung von Assertion
	 */
	public void createNewFile(File aFile, String assertionName, String prefix,
		String nsURI, String description) 
	{
		JDialog.setDefaultLookAndFeelDecorated(true);
		AssertionEditor frame = new AssertionEditor(owner, this, aFile, true);
		frame.addRootElement(assertionName, prefix, nsURI, description);
		frame.setVisible(true);
	}

	/**
	 * Bietet dem Benutzer die Moeglichkeit an, den Defaultnamespace eines 
	 * Pakets zu aendern.
	 */
	public void setDefaultNS()
	{
		if (selectedFile == null)
		{
			return;
		}
		
		if (selectedFile.isDirectory())
		{
			try 
			{
				String oldDefaultPrefix = "";
				String oldDefaultNsURI = "";
				File defaultNSFile = new File(selectedFile.getAbsolutePath(),
						".DefaultNS");
				if (defaultNSFile.exists())
				{
					BufferedReader reader = new BufferedReader(new FileReader
							(defaultNSFile));
					oldDefaultPrefix = reader.readLine();
					oldDefaultNsURI = reader.readLine();
					reader.close();	
				};
			    
				InputDialogPattern defaultNSDialog = new InputDialogPattern
					(selectedFile.getName(),oldDefaultPrefix, oldDefaultNsURI,
							true);
				defaultNSDialog.setLabelTitel("Package Name : ", 
						"Default Prefix : ", "Default NamespaceURI : ");
				defaultNSDialog.setInputFieldEditable(false);
				defaultNSDialog.setLocationRelativeTo(null);
				defaultNSDialog.setVisible(true);
			    
				String defaultPrefix = defaultNSDialog.getPrefix();
				String defaultNsURI = defaultNSDialog.getNsURI();
			    
				if ((defaultPrefix == null) || (defaultNsURI == null))
				{
					return;
				}
			    
				if (!defaultNSFile.exists()) 
				{
					defaultNSFile.createNewFile();
				}
			    
				BufferedWriter writer = new BufferedWriter(new 
						FileWriter(defaultNSFile));
				writer.write(defaultPrefix);
				writer.newLine();
				writer.write(defaultNsURI);
				writer.newLine();
				writer.close();
				
				owner.refreshPropertiesTable();
				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by writing defaultNS.");
			}
		} 
		else 
		{
			return;
		}
	}
	
	private void createPopupMenu() 
	{
		popupMenu = new JPopupMenu();

		expandAction = getActionExpand();
		popupMenu.add(expandAction);

		refreshAction = getActionRefresh();
		popupMenu.add(refreshAction);

		popupMenu.addSeparator();

		newPackageAction = getActionNewPackage();
		popupMenu.add(newPackageAction);

		actionNewAssertion = getActionNewFile();
		popupMenu.add(actionNewAssertion);

		deleteAction = getActionDelete();
		popupMenu.add(deleteAction);

		renameAction = getActionRename();
		popupMenu.add(renameAction);
		
		popupMenu.addSeparator();
		
		actionSetDefaultNS = getActionSetDefaultNS();
		popupMenu.add(actionSetDefaultNS);

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
				owner.refreshPropertiesTable();
			}
		};

		return newPackageAction;
	}

	
	@Override
	/**
	 * Entfernt alle in Workspace selektierten Dateien.
	 */
	protected void delete()
	{
		fileTree.repaint();
		TreePath[] selectionPaths = fileTree.getSelectionPaths();
		fileTree.setSelectionPath(null);
		owner.refreshPropertiesTable();
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
					"You cannot delete Assertion Workspace.", "Error",
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
		    		
						setCursor(Cursor.getPredefinedCursor(Cursor
								.WAIT_CURSOR));
						owner.closeOpenedFiles(file);
						DefaultMutableTreeNode parent = 
							(DefaultMutableTreeNode) iTreeNode.getParent();
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
						"Do you want to delete \nAssertion \""
						+ file.getName() + "\" ?",
						"Comformation", JOptionPane.YES_NO_OPTION) == 
							JOptionPane.YES_OPTION) 
					{
		    		
						setCursor(Cursor.getPredefinedCursor
								(Cursor.WAIT_CURSOR));
						owner.closeOpenedFiles(file);
						DefaultMutableTreeNode parent = 
							(DefaultMutableTreeNode) iTreeNode.getParent();
						if (IOUtils.deleteFiles(file)) 
						{
							iTreeNode.removeFromParent();
						}
						treeModel.nodeStructureChanged(parent);
						setCursor(Cursor.getPredefinedCursor(Cursor
								.DEFAULT_CURSOR));
					}
				}
			}
		}		
	}
		
	
	@Override
	protected Action getActionDelete() 
	{
		Action iAction = new AbstractAction("Delete              " +
				"      Delete",
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
		Action iAction = new AbstractAction("New Assertion    Insert",
			Images.ICON_ASSERTION_16) 
		{
			private static final long serialVersionUID = -7211749312468286881L;

			public void actionPerformed(ActionEvent e) 
			{
				createNewAssertion();
			
			}
		};
		return iAction;
	}

	
	private Action getActionSetDefaultNS() 
	{
		actionSetDefaultNS = new AbstractAction("Set default Namespace") 
		{

			private static final long serialVersionUID = -9124369080538824035L;

			public void actionPerformed(ActionEvent e) 
			{
				setDefaultNS();
			}
		};
		return actionSetDefaultNS;
	}

	@Override
	void openFile(File file) 
	{
		JDialog.setDefaultLookAndFeelDecorated(true);
		AssertionEditor frame = new AssertionEditor(owner, this, file, false);
		frame.setVisible(true);
	}


	@Override
	/**
	 * Benennt eine Datei um.
	 */
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
				"You cannot rename " +
				"the Assertion Workspace.", "Warning",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
	
		if (selectedFile.isDirectory()) 
		{	
		
			newFileName = getInputFor("New Folder", inputFileName);	
			newFilePath = parentPath
				+ System.getProperty("file.separator")
				+ newFileName;
		} 
		else 
		{		
			newFileName = getInputFor("New Assertion", inputFileName);
			newFilePath = parentPath
				+ System.getProperty("file.separator")
				+ newFileName + GUIConstants.ASSERTION_EXTENSION;
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
		owner.refreshPropertiesTable();
	}

	private void enablePopupMenuItems()
	{
		if (selectedFile == null) 
		{
			expandAction.setEnabled(false);
			refreshAction.setEnabled(false);
			newPackageAction.setEnabled(false);
			actionNewAssertion.setEnabled(false);
			deleteAction.setEnabled(false);
			renameAction.setEnabled(false);
			actionSetDefaultNS.setEnabled(false);
		} 
		else 
		{  
			if (fileTree.getSelectionPaths().length == 1)
			{
				expandAction.setEnabled(true);
				refreshAction.setEnabled(true);
				newPackageAction.setEnabled(true);
				actionNewAssertion.setEnabled(true);
				deleteAction.setEnabled(true);
				renameAction.setEnabled(true);
				if (selectedFile.isDirectory())
				{
					actionSetDefaultNS.setEnabled(true);
				} 
				else 
				{
					actionSetDefaultNS.setEnabled(false);
				}
			} 
			else 
			{
				expandAction.setEnabled(false);
				refreshAction.setEnabled(false);
				newPackageAction.setEnabled(false);
				actionNewAssertion.setEnabled(false);
				deleteAction.setEnabled(true);
				renameAction.setEnabled(false);
				actionSetDefaultNS.setEnabled(false);
			}
		}
		
	}
	

	private MouseListener mouseListener = new MouseListener()
	{
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
				DefaultMutableTreeNode iTreeNode = fileTree
				    .getSelectedTreeNode();
				File iFile = fileTree.getFileNode(iTreeNode).getFile();
				if (iFile.isDirectory()) 
				{
					return;
				}
				openFile(iFile);
			}
			owner.refreshPropertiesTable();
		}

		public void mousePressed(MouseEvent e) 
		{
		}

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
				
				int offmask = MouseEvent.SHIFT_DOWN_MASK | 
				                    MouseEvent.BUTTON1_DOWN_MASK;		
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
			owner.refreshPropertiesTable();
		}

		public void mouseEntered(MouseEvent e) 
		{
		}

		public void mouseExited(MouseEvent e) 
		{
		}
		
	};
	
	private KeyListener hotkeysKeyListener = new KeyListener()
	{
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
					createNewAssertion();
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

