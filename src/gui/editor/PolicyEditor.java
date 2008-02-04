package gui.editor;

import gui.explorer.AssertionExplorer;
import gui.explorer.PolicyExplorer;
import gui.images.Images;
import gui.util.GUIConstants;
import gui.util.GUIUtil;
import gui.util.M_FileChooser;
import gui.util.M_TreeCellsEditor;
import gui.util.M_TreeCellsRenderer;
import gui.util.RefURIInputDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import org.w3c.policy.Policy;
import org.w3c.policy.util.NSRegistry;
import org.w3c.policy.util.PolicyConstants;
import org.w3c.policy.util.PolicyReader;
import org.w3c.policy.util.PolicyUtil;
import org.w3c.policy.util.PolicyWriter;

import util.DocumentUtil;
import util.IOUtils;
import util.MOptionPanes;
import util.Messages;

/**
 * Bietet die Klasse and Schnittstelle von PolicyEditor, 
 * der den Benutzern bei dem Erstellen und dem Bearbeiten von Policies mit 
 * einer grapgischen Benutzeroberflaeche unterstuetzt. 
 * 
 * Der Editor besteht aus :
 * 
 *     Komponente                Positionen
 * 1. MenuBar und ToolBar        ganz oben
 * 2. PolicyWorkspace            unter MenuBar und ToolBar, ganz links
 * 3. DokumentPane               unter MenuBar und ToolBar, in der Mitte
 * 4. AssertionWorkspace         unter MenuBar und ToolBar, ganz rechts
 * 5. AttrTable                  unter DokumentPane
 * 6. PropertiesTable            unter AssertionExplorer
 * 
 * die genauere Funktionalitaet koennen Sie in Dokumentation finden.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */

public class PolicyEditor extends JFrame{

	private static final long serialVersionUID = -4690735589394904520L;

	private JMenuItem addAllMenuItem;

	private JMenuItem addAssertionMenuItem;

	private JMenuItem addAttributeMenuItem;

	private JMenuItem addExactlyoneMenuItem;

	private JMenuItem addPolicyMenuItem;

	private JMenuItem addPolicyReferenceMenuItem;

	private JMenuItem addTextValueMenuItem;

	private AssertionExplorer assertionExplorer;

	private JMenuItem assertionMenuItem;

	private JMenuItem assertionPackageMenuItem;

	private String assertionWorkspacePath;

	private AttrTable attrTable;

	private JButton btAddAttribute;

	private JButton btAddElement;

	private JButton btAddText;

	private JButton btAll;

	private JButton btClose;

	private JButton btCloseAll;

	private JButton btCollapse;

	private JButton btCollapseAll;

	private JButton btDeleteAttribute;

	private JButton btDeleteElement;

	private JButton btEditAttribute;

	private JButton btEditElement;

	private JButton btExactlyOne;

	private JButton btExpand;

	private JButton btExpandAll;

	private JButton btImportPolicy;
	
	private JButton btIntersect;

	private JButton btMerge;

	private JButton btNewAssertion;

	private JButton btNewPolicy;

	private JButton btNormalize;

	private JButton btPolicy;

	private JButton btPolicyReference;

	private JButton btSave;

	private JButton btSaveAs;

	private JMenuItem closeAllMenuItem;
	
	private JMenuItem closeOtherMenuItem;

	private JMenuItem closeMenuItem;

	private JMenuItem collapseAllMenuItem;

	private JMenuItem collapseMenuItem;

	private File currentFile;

	private JMenuItem deleteAttributeMenuItem;

	private JMenuItem deleteElementMenuItem;

	private Document document;

	private DocumentPane documentPane;

	private DomTree domTree;

	private JMenuItem editAssertionMenuItem;

	private JMenuItem editAttributeMenuItem;

	private JMenuItem expandAllMenuItem;

	private JMenuItem expandMenuItem;

	private JFileChooser fileChooser;

	private JMenuItem intersectMenuItem;

	private JMenuItem mergeMenuItem;

	private JMenuItem normalizeMenuItem;
	
	private Node editingDomNode;
	/*
	 * Use to determine whether to delete the new created but empty policy file
	 * in policy explorer
	 */
	private PolicyExplorer policyExplorer;

	private JMenuItem policyMenuItem;

	private JMenuItem policyPackageMenuItem;

	private String policyReferenceURI;

	private String policyWorkspacePath;
	
	private PropertiesTable propertiesTable;

	private JMenuItem saveAsMenuItem;

	private JMenuItem saveAllMenuItem;

	private JMenuItem saveMenuItem;

	private JMenuItem viewSourceMenuItem;

	/**
	 * static enables direct access from other classes 
	 * currently used by DomTree.java()
	 */
	public static M_TabbedPane tabbedPane;
	
	private M_TreeCellsEditor domTreeCellEditor;

	private AttrTableModel tableModel;

	private JToolBar toolBar;

	private JButton btSetOptional;
	
	private JButton btSetIgnorable;

	private JButton btAddComment;

	private JMenuItem addCommentMenuItem;

	private JButton btViewSource;	
	
	
/**
 * die Pfaden von zwei Verzeichnissen, die als Wurzelverzeichnis in den
 * jeweiligen Workspaces dienen, werden spezifiziert
 * 
 */
	public PolicyEditor(String policyWorkspacePath,
			String assertionWorkspacePath) {

		super();		
		this.policyWorkspacePath = policyWorkspacePath;
		this.assertionWorkspacePath = assertionWorkspacePath;
        
		if (!NSRegistry.fileExists()){
			NSRegistry.createFile();
		}
		
		if (NSRegistry.countNamespace() < 4){
			NSRegistry.createFile();
		}
		
		getContentPane().setLayout(new BorderLayout());

		createMenuBar();

		createToolBar();

		createAttrTable();
		
		createPropertiesTable();

		createWindow();

		createFileChooser(policyWorkspacePath);
		
		policyExplorer.expandTree();
		policyExplorer.fileTree.setSelectionRow(0);
		assertionExplorer.fileTree.setSelectionRow(0);
	}

	private void addAssertion() {
		domTree.addAssertion();
	}

	private void addAttribute() {
		attrTable.addAttribute();
	}

	private void addComment() {
		domTree.addComment();
	}

	private void addOperatorAll() {
		domTree.addOperatorAll();
	}

	private void addOperatorExactlyOne() {
		domTree.addOperatorExactlyOne();
	}

	private void addOperatorPolicy() {
		domTree.addOperatorPolicy();
	}

	/**
	 * Bietet dem Benutzer die Moeglichkeit, eine Policy in dem Dateisystem
	 * zu referenzieren. Sie wird in ein Fenster geladen, in dem der Benutzer
	 * eine gewuenschte Policy eingeben kann. Der Pfad der ausgewaehlten 
	 * Policy wird alte Wert des Attributs "URI" des Konstrukts 
	 * "wsp:PolicyReference" hinzugefuegt.
	 *
	 */
	public void addPolicyReference() {
		showURIInputDialog(null);

		if (getPolicyReferenceURI() == null ||
				getPolicyReferenceURI().isEmpty()) {
			return;
		}

		domTree.addPolicyReference(getPolicyReferenceURI());
	}

	private void addText() {
		domTree.addTextNode();
	}

	private boolean checkSelectedPolicies(Vector<File> selectedPolicies) {
		if (selectedPolicies == null || selectedPolicies.size() == 0) {
			MOptionPanes.showError(this, "No policy is selected.");
			return false;
		} else if (selectedPolicies.size() < 2) {
			MOptionPanes
					.showError(this, "You must select at least 2 policies.");
			return false;
		}

		int size = selectedPolicies.size();

		for (int i = 0; i < size; i++) {
			if (tabbedPane.isFileOpened(selectedPolicies.get(i))) {
				if (isCurrentFileChanged()) {
					if (promptToSave()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Entfernt die aktuelle selektierte DocumentPane von der TabbedPane. 
	 * Falls die Aenderungen der Policy in einer DocumentPane noch nicht
	 * gespeichert wurden, werden dem Benutzer die Optionen fuer "Save":  
	 * "Ja", "Nein" und "Abbrechen" angeboten.
	 */
	public void closeTab() {
		if (isCurrentFileChanged()) {
			if (promptToSave()) {
				return;
			}
		}
		tabbedPane.closeTab();
		refresh(tabbedPane.getSelectedDocumentPane());
		setButtons();
	}
	
	/**
	 * Entfernt alle DocumentPanes von der TabbedPane. Wenn die Aenderungen
	 * der Policy in einer DocumentPane noch nicht gespeichert wurden, werden
	 * dem Benutzer die Optionen fuer "Save":  "Ja", "Nein" oder "Abbrechen" 
	 * angeboten.
	 */
	public void closeAllTabs() {
		int count = tabbedPane.getTabCount();
		
		while (count > 0) {
			tabbedPane.setSelectedIndex(0);
			if (tabbedPane.getDocumentPaneAt(0).isFileChanged()) {				
				if (promptToSave()) {
					return;
				}
			}
			tabbedPane.closeTab();
			count--;
			refresh(tabbedPane.getSelectedDocumentPane());
			setButtons();
		}
		refresh(tabbedPane.getSelectedDocumentPane());
		setButtons();
	}

	/**
	 * Entfernt alle andere DocumentPanes ausser dem aktuell selektierten von
	 * der TabbedPane. Wenn die Aenderungen der Policy in einer DocumentPane
	 * noch nicht gespeichert wurden, werden dem Benutzer die Optionen "Ja",
	 * "Nein" oder "Abbrechen" angeboten.
	 */
	public void closeOtherTabs() {
		
		int current = tabbedPane.getTabCount() - 1;
		int original = tabbedPane.getSelectedIndex();
		
		while (current > 0) {
			if (current == original) {
				current--;
				if (tabbedPane.getDocumentPaneAt(current).isFileChanged()) {
					tabbedPane.setSelectedIndex(current);
					if (promptToSave()) {
						return;
					}					
				}
				tabbedPane.setSelectedIndex(current);
				tabbedPane.closeTab();
				original--;		
			}
			else {
				if (tabbedPane.getDocumentPaneAt(current).isFileChanged()) {
					tabbedPane.setSelectedIndex(current);
					if (promptToSave()) {
					return;
					}					
				}
				tabbedPane.setSelectedIndex(current);
				tabbedPane.closeTab();
				current--;
			}						
			refresh(tabbedPane.getSelectedDocumentPane());
			setButtons();
		}		
		refresh(tabbedPane.getSelectedDocumentPane());
		setButtons();
	}	

	/**
	 * Schliesst eine selektierte Datei
	 */
	public void closeOpenedFiles(File file) {
		if (file.isDirectory()) {
			List files = IOUtils.listPolicies(file);

			if (files == null) {
				return;
			}

			Vector<Integer> indices = new Vector<Integer>();

			for (int i = 0; i < files.getItemCount(); i++) {
				String filePath = files.getItem(i);
				int count = tabbedPane.getTabCount();

				for (int j = 0; j < count; j++) {
					if (tabbedPane.getToolTipTextAt(j).equals(filePath)) {
						indices.add(j);
						currentFile = null;
					}
				}
			}

			if (indices.size() == 0) {
				return;
			}

			Collections.sort(indices, Collections.reverseOrder());

			for (int i = 0; i < indices.size(); i++) {
				tabbedPane.remove(indices.get(i));
			}
		} else {
			if (tabbedPane.isFileOpened(file)) {
				closeTab();
			}
		}
		refresh(tabbedPane.getSelectedDocumentPane());
	}


	private void createAttrTable() {
		/* Create attribute table and add selection listener to this table */
		tableModel = new AttrTableModel();
		attrTable = new AttrTable(tableModel);

		attrTable.getSelectionModel().addListSelectionListener(
				attrTableSelectionListener);

		/* Add mouse listener */

		attrTable.addMouseListener(attrTableMouseListener);
		
		/* Add key listener */
		
		attrTable.addKeyListener(attrTableKeyListener);

		/* Add table model listener */

		tableModel.addTableModelListener(attrTableModelListener);
	}
	
	private void createPropertiesTable(){
		propertiesTable = new PropertiesTable();
		
		/* Add key listener */
		
		propertiesTable.addKeyListener(propertiesTableKeyListener);	
		propertiesTable.addMouseListener(propertiesTableMouseListener);
		
	}

	/**
	 * 
	 */
	private void createFileChooser(String currentPath) {
		fileChooser = M_FileChooser.getPolicyFileChooser(currentPath);
	}

	private void createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu fileMenu = new JMenu();
		fileMenu.setText("File");
		menuBar.add(fileMenu);

		final JMenu newMenu = new JMenu();
		newMenu.setText("New");
		fileMenu.add(newMenu);

		policyMenuItem = new JMenuItem();
		policyMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				policyExplorer.createNewPolicy();
			}
		});
		policyMenuItem.setText("Policy");
		policyMenuItem.setIcon(Images.ICON_POLICY_16);
		newMenu.add(policyMenuItem);

		policyPackageMenuItem = new JMenuItem();
		policyPackageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				policyExplorer.createNewPackage();
			}
		});
		policyPackageMenuItem.setText("Policy Package");
		policyPackageMenuItem.setIcon(Images.ICON_PACKAGE_16);
		newMenu.add(policyPackageMenuItem);

		newMenu.addSeparator();

		assertionMenuItem = new JMenuItem();
		assertionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assertionExplorer.createNewAssertion();
			}
		});
		assertionMenuItem.setText("Assertion");
		assertionMenuItem.setIcon(Images.ICON_ASSERTION_16);
		newMenu.add(assertionMenuItem);

		assertionPackageMenuItem = new JMenuItem();
		assertionPackageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assertionExplorer.createNewPackage();
			}
		});
		assertionPackageMenuItem.setText("Assertion Package");
		assertionPackageMenuItem.setIcon(Images.ICON_PACKAGE_16);
		newMenu.add(assertionPackageMenuItem);

		fileMenu.addSeparator();

		JMenuItem openMenuItem = new JMenuItem();
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDocument(null);
			}
		});
		openMenuItem.setText("Open");
		openMenuItem.setIcon(Images.ICON_OPEN_16);
		fileMenu.add(openMenuItem);

		fileMenu.addSeparator();
		
		JMenuItem importMenuItem = new JMenuItem();
		importMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				policyExplorer.importPolicy();
			}
		});
		importMenuItem.setText("Import");
		importMenuItem.setToolTipText("Copy a Policy File to Policy " +
				"Workspace and then open it.");
		importMenuItem.setIcon(Images.ICON_IMPORT_16);
		fileMenu.add(importMenuItem);

		fileMenu.addSeparator();

		saveMenuItem = new JMenuItem();
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		saveMenuItem.setText("Save                    Ctrl+S");
		saveMenuItem.setIcon(Images.ICON_SAVE_16);
		saveMenuItem.setEnabled(false);
		fileMenu.add(saveMenuItem);
		

		saveAllMenuItem = new JMenuItem();
		saveAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAll();
			}
		});
		saveAllMenuItem.setText("Save All              Ctrl+Shift+S");
		saveAllMenuItem.setIcon(Images.ICON_SAVE_16);
		saveAllMenuItem.setEnabled(false);
		fileMenu.add(saveAllMenuItem);

		saveAsMenuItem = new JMenuItem();
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		saveAsMenuItem.setText("Save as...");
		saveAsMenuItem.setIcon(Images.ICON_SAVE_AS_16);
		saveAsMenuItem.setEnabled(false);
		fileMenu.add(saveAsMenuItem);

		fileMenu.addSeparator();

		closeMenuItem = new JMenuItem();
		closeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeTab();
			}
		});
		closeMenuItem.setText("Close                  Ctrl+W ");
		closeMenuItem.setIcon(Images.ICON_CLOSE_16);
		closeMenuItem.setEnabled(false);
		fileMenu.add(closeMenuItem);
		
		closeOtherMenuItem = new JMenuItem();
		closeOtherMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeOtherTabs();
			}
		});
		closeOtherMenuItem.setText("Close other Tabs");
		closeOtherMenuItem.setIcon(Images.ICON_CLOSE_16);
		closeOtherMenuItem.setEnabled(false);
		fileMenu.add(closeOtherMenuItem);

		closeAllMenuItem = new JMenuItem();
		closeAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAllTabs();
			}
		});
		closeAllMenuItem.setText("Close All            Ctrl+Shift+W");
		closeAllMenuItem.setIcon(Images.ICON_CLOSE_ALL_16);
		closeAllMenuItem.setEnabled(false);
		fileMenu.add(closeAllMenuItem);

		fileMenu.addSeparator();

		viewSourceMenuItem = new JMenuItem();
		viewSourceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showSourceViewer();
			}
		});
		viewSourceMenuItem.setText("View Source");
		viewSourceMenuItem.setIcon(Images.ICON_VIEW_SOURCE_16);
		viewSourceMenuItem.setEnabled(false);
		fileMenu.add(viewSourceMenuItem);

		fileMenu.addSeparator();

		final JMenuItem switchPolicyWorkspaceMenuItem = new JMenuItem();
		switchPolicyWorkspaceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				policyExplorer.switchWorkspace();
				policyExplorer.workspacePath = policyExplorer.getWorkspaceDir();
				policyExplorer.savePathFile();
			}
		});
		switchPolicyWorkspaceMenuItem.setText("Switch Policy Workspace");
		fileMenu.add(switchPolicyWorkspaceMenuItem);

		final JMenuItem switchAssertionWorkspaceMenuItem = new JMenuItem();
		switchAssertionWorkspaceMenuItem
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						assertionExplorer.switchWorkspace();
						assertionExplorer.workspacePath = assertionExplorer
						    .getWorkspaceDir();
						assertionExplorer.savePathFile();
					}
				});
		switchAssertionWorkspaceMenuItem.setText("Switch Assertion Workspace");
		fileMenu.add(switchAssertionWorkspaceMenuItem);

		fileMenu.addSeparator();

		final JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int count = tabbedPane.getTabCount();

				for (int i = 0; i < count; i++) {
					if (tabbedPane.getDocumentPaneAt(i).isFileChanged()) {
						tabbedPane.setSelectedIndex(i);
						if (promptToSave()) {
							return;
						}
					}
				}
				dispose();
			}
		});
		exitMenuItem.setText("Exit");
		fileMenu.add(exitMenuItem);

		final JMenu editMenu = new JMenu();
		editMenu.setText("Edit");
		menuBar.add(editMenu);

		final JMenu addMenu = new JMenu();
		addMenu.setText("Add Child");
		editMenu.add(addMenu);

		addPolicyMenuItem = new JMenuItem();
		addPolicyMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOperatorPolicy();
			}
		});
		addPolicyMenuItem.setText("wsp:Policy");
		addPolicyMenuItem.setIcon(Images.ICON_POLICY_OPERATOR_16);
		addPolicyMenuItem.setEnabled(false);
		addMenu.add(addPolicyMenuItem);

		addExactlyoneMenuItem = new JMenuItem();
		addExactlyoneMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOperatorExactlyOne();
			}
		});
		addExactlyoneMenuItem.setText("wsp:ExactlyOne");
		addExactlyoneMenuItem.setIcon(Images.ICON_XOR_16);
		addExactlyoneMenuItem.setEnabled(false);
		addMenu.add(addExactlyoneMenuItem);

		addAllMenuItem = new JMenuItem();
		addAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOperatorAll();
			}
		});
		addAllMenuItem.setText("wsp:All");
		addAllMenuItem.setIcon(Images.ICON_ALL_16);
		addAllMenuItem.setEnabled(false);
		addMenu.add(addAllMenuItem);

		addMenu.addSeparator();

		addPolicyReferenceMenuItem = new JMenuItem();
		addPolicyReferenceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPolicyReference();
			}
		});
		addPolicyReferenceMenuItem.setText("Policy Reference");
		addPolicyReferenceMenuItem.setIcon(Images.ICON_POLICY_REFERENCE_16);
		addPolicyReferenceMenuItem.setEnabled(false);
		addMenu.add(addPolicyReferenceMenuItem);

		addMenu.addSeparator();

		addAssertionMenuItem = new JMenuItem();
		addAssertionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAssertion();
			}
		});
		addAssertionMenuItem.setText("Assertion");
		addAssertionMenuItem.setIcon(Images.ICON_ADD_Element_16);
		addAssertionMenuItem.setEnabled(false);
		addMenu.add(addAssertionMenuItem);

		addCommentMenuItem = new JMenuItem();
		addCommentMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.addComment();
			}
		});
		addCommentMenuItem.setText("Comment");
		addCommentMenuItem.setIcon(Images.ICON_COMMENT_16);
		addCommentMenuItem.setEnabled(false);
		addMenu.add(addCommentMenuItem);

		addTextValueMenuItem = new JMenuItem();
		addTextValueMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText();
			}
		});
		addTextValueMenuItem.setText("Text Value");
		addTextValueMenuItem.setIcon(Images.ICON_ADD_TEXT_16);
		addTextValueMenuItem.setEnabled(false);
		addMenu.add(addTextValueMenuItem);

		addMenu.addSeparator();

		addAttributeMenuItem = new JMenuItem();
		addAttributeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAttribute();
			}
		});
		addAttributeMenuItem.setText("Attribute");
		addAttributeMenuItem.setIcon(Images.ICON_ADD_ATTRIBUTE_16);
		addAttributeMenuItem.setEnabled(false);
		addMenu.add(addAttributeMenuItem);

		final JMenu editMainMenu = new JMenu();
		editMainMenu.setText("Edit");
		editMenu.add(editMainMenu);

		editAssertionMenuItem = new JMenuItem();
		editAssertionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editNode();
			}
		});
		editAssertionMenuItem.setText("Tree Node");
		editAssertionMenuItem.setIcon(Images.ICON_EDIT_ELEMENT_16);
		editAssertionMenuItem.setEnabled(false);
		editMainMenu.add(editAssertionMenuItem);

		editMainMenu.addSeparator();

		editAttributeMenuItem = new JMenuItem();
		editAttributeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editAttribute();
			}
		});
		editAttributeMenuItem.setText("Attribute");
		editAttributeMenuItem.setIcon(Images.ICON_EDIT_ATTRIBUTE_16);
		editAttributeMenuItem.setEnabled(false);
		editMainMenu.add(editAttributeMenuItem);

		final JMenu deleteMenu = new JMenu();
		deleteMenu.setText("Delete");
		editMenu.add(deleteMenu);

		deleteElementMenuItem = new JMenuItem();
		deleteElementMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteNode();
			}
		});
		deleteElementMenuItem.setText("Delete Tree Node");
		deleteElementMenuItem.setIcon(Images.ICON_DELETE_ELEMENT_16);
		deleteElementMenuItem.setEnabled(false);
		deleteMenu.add(deleteElementMenuItem);

		deleteMenu.addSeparator();

		deleteAttributeMenuItem = new JMenuItem();
		deleteAttributeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteAttribute();
			}
		});
		deleteAttributeMenuItem.setText("Attribute");
		deleteAttributeMenuItem.setIcon(Images.ICON_DELETE_ATTRIBUTE_16);
		deleteAttributeMenuItem.setEnabled(false);
		deleteMenu.add(deleteAttributeMenuItem);

		final JMenu processingMenu = new JMenu();
		processingMenu.setText("Processing");
		menuBar.add(processingMenu);

		normalizeMenuItem = new JMenuItem();
		normalizeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				normalize();
			}
		});
		normalizeMenuItem.setText("Normalize     Alt+N");
		normalizeMenuItem.setIcon(Images.ICON_NORMALIZE_16);
		normalizeMenuItem.setEnabled(false);
		processingMenu.add(normalizeMenuItem);

		intersectMenuItem = new JMenuItem();
		intersectMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				intersect();
			}
		});
		intersectMenuItem.setText("Intersect       Alt+I");
		intersectMenuItem.setIcon(Images.ICON_INTERSECT_16);
		intersectMenuItem.setEnabled(false);
		processingMenu.add(intersectMenuItem);

		mergeMenuItem = new JMenuItem();
		mergeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				merge();
			}
		});
		mergeMenuItem.setText("Merge            Alt+M");
		mergeMenuItem.setIcon(Images.ICON_MERGE_16);
		mergeMenuItem.setEnabled(false);
		processingMenu.add(mergeMenuItem);

		final JMenu expandMenu = new JMenu();
		expandMenu.setText("Expand");
		menuBar.add(expandMenu);

		expandMenuItem = new JMenuItem();
		expandMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandNode(domTree.getSelectedTreeNode());
				enableExpandCollapse();
			}
		});
		expandMenuItem.setText("Expand");
		expandMenuItem.setIcon(Images.ICON_EXPAND_16);
		expandMenuItem.setEnabled(false);
		expandMenu.add(expandMenuItem);

		expandAllMenuItem = new JMenuItem();
		expandAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandTree();
				enableExpandCollapse();
			}
		});
		expandAllMenuItem.setText("Expand All");
		expandAllMenuItem.setIcon(Images.ICON_EXPAND_ALL_16);
		expandAllMenuItem.setEnabled(false);
		expandMenu.add(expandAllMenuItem);

		final JMenu collapseMenu = new JMenu();
		collapseMenu.setText("Collapse");
		menuBar.add(collapseMenu);

		collapseMenuItem = new JMenuItem();
		collapseMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseNode(domTree.getSelectedTreeNode());
				enableExpandCollapse();
			}
		});
		collapseMenuItem.setText("Collapse");
		collapseMenuItem.setIcon(Images.ICON_COLLAPSE_16);
		collapseMenuItem.setEnabled(false);
		collapseMenu.add(collapseMenuItem);

		collapseAllMenuItem = new JMenuItem();
		collapseAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseTree();
				enableExpandCollapse();
			}
		});
		collapseAllMenuItem.setText("Collapse All");
		collapseAllMenuItem.setIcon(Images.ICON_COLLAPSE_ALL_16);
		collapseAllMenuItem.setEnabled(false);
		collapseMenu.add(collapseAllMenuItem);

		final JMenu helpMenu = new JMenu();
		helpMenu.setText("Help");
		menuBar.add(helpMenu);

		final JMenuItem aboutMenuItem = new JMenuItem();
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		aboutMenuItem.setText("About");
		helpMenu.add(aboutMenuItem);
	}

	private void createToolBar() {
		toolBar = new JToolBar();
		toolBar.setBorder(new LineBorder(Color.black, 0, false));
		getContentPane().add(toolBar, BorderLayout.NORTH);

		btNewPolicy = new JButton(Images.ICON_POLICY_24);
		btNewPolicy.setToolTipText("Create a New Policy");
		ActionListener lsActionNewPolicy = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				policyExplorer.createNewPolicy();
			}
		};
		btNewPolicy.addActionListener(lsActionNewPolicy);
		toolBar.add(btNewPolicy);
		
		btNewAssertion = new JButton(Images.ICON_ASSERTION_24);
		btNewAssertion.setToolTipText("Create a New Assertion");
		ActionListener lsActionNewAssertion = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assertionExplorer.createNewAssertion();
			}
		};
		btNewAssertion.addActionListener(lsActionNewAssertion);
		toolBar.add(btNewAssertion);

		JButton btOpen = new JButton(Images.ICON_OPEN_24);
		btOpen.setToolTipText("Open a Policy File");
		ActionListener lsActionOpen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDocument(null);
			}
		};
		btOpen.addActionListener(lsActionOpen);
		toolBar.add(btOpen);

		btImportPolicy = new JButton(Images.ICON_IMPORT_24);
		btImportPolicy.setToolTipText("Import a Policy File");
		ActionListener lsActionImportPolicy = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				policyExplorer.importPolicy();
			}
		};
		btImportPolicy.addActionListener(lsActionImportPolicy);
		toolBar.add(btImportPolicy);

		toolBar.addSeparator();
		
		btNormalize = new JButton(Images.ICON_NORMALIZE_24);
		btNormalize.setToolTipText("Normalize the Policy");
		ActionListener lsActionNormalize = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				normalize();
			}
		};
		btNormalize.addActionListener(lsActionNormalize);
		btNormalize.setEnabled(false);
		toolBar.add(btNormalize);

		btIntersect = new JButton(Images.ICON_INTERSECT_24);
		btIntersect.setToolTipText("Intersect Policies  (with at least 2 " +
				"policies) ");
		ActionListener lsActionIntersect = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				intersect();
			}
		};
		btIntersect.addActionListener(lsActionIntersect);
		btIntersect.setEnabled(false);
		toolBar.add(btIntersect);

		btMerge = new JButton(Images.ICON_MERGE_24);
		btMerge.setToolTipText("Merge Policies  (with at least 2 policies)");
		ActionListener lsActionMerge = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				merge();
			}
		};
		btMerge.addActionListener(lsActionMerge);
		btMerge.setEnabled(false);
		toolBar.add(btMerge);
		
		toolBar.addSeparator();
		
		btViewSource = new JButton(Images.ICON_VIEW_SOURCE_24);
		btViewSource.setToolTipText("View Source");
		ActionListener lsActionViewSource = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSourceViewer();
			}
		};
		btViewSource.addActionListener(lsActionViewSource);
		btViewSource.setEnabled(false);
		toolBar.add(btViewSource);
		
		btSave = new JButton(Images.ICON_SAVE_24);
		btSave.setToolTipText("Save");
		ActionListener lsActionSave = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		};
		btSave.addActionListener(lsActionSave);
		btSave.setEnabled(false);
		toolBar.add(btSave);

		btSaveAs = new JButton(Images.ICON_SAVE_AS_24);
		btSaveAs.setToolTipText("Save as...");
		ActionListener lsActionSaveAs = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		};
		btSaveAs.addActionListener(lsActionSaveAs);
		btSaveAs.setEnabled(false);
		toolBar.add(btSaveAs);
		
		
		btClose = new JButton(Images.ICON_CLOSE_24);
		btClose.setToolTipText("Close the Selected Tab");
		ActionListener lsActionClose = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeTab();
			}
		};
		btClose.addActionListener(lsActionClose);
		btClose.setEnabled(false);
		toolBar.add(btClose);

		btCloseAll = new JButton(Images.ICON_CLOSE_ALL_24);
		btCloseAll.setToolTipText("Close All Tabs");
		ActionListener lsActionCloseAll = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAllTabs();
			}
		};
		btCloseAll.addActionListener(lsActionCloseAll);
		btCloseAll.setEnabled(false);
		toolBar.add(btCloseAll);

		toolBar.addSeparator();

		btPolicy = new JButton(Images.ICON_POLICY_OPERATOR_24);
		btPolicy.setToolTipText("Add Operator \"wsp:Policy\"");
		ActionListener lsActionPolicy = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOperatorPolicy();
			}
		};
		btPolicy.addActionListener(lsActionPolicy);
		btPolicy.setEnabled(false);
		toolBar.add(btPolicy);

		btExactlyOne = new JButton(Images.ICON_XOR_24);
		btExactlyOne.setToolTipText("Add Operator \"wsp:ExactlyOne\"");
		ActionListener lsActionExactlyOne = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOperatorExactlyOne();
			}
		};
		btExactlyOne.addActionListener(lsActionExactlyOne);
		btExactlyOne.setEnabled(false);
		toolBar.add(btExactlyOne);

		btAll = new JButton(Images.ICON_ALL_24);
		btAll.setToolTipText("Add Operator \"wsp:All\"");
		ActionListener lsActionAll = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOperatorAll();
			}
		};
		btAll.addActionListener(lsActionAll);
		btAll.setEnabled(false);
		toolBar.add(btAll);

		btPolicyReference = new JButton(Images.ICON_POLICY_REFERENCE_24);
		btPolicyReference.setToolTipText("Add Policy Reference");
		ActionListener lsActionPolicyReference = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPolicyReference();
			}
		};
		btPolicyReference.addActionListener(lsActionPolicyReference);
		btPolicyReference.setEnabled(false);
		toolBar.add(btPolicyReference);

		toolBar.addSeparator();

		btAddElement = new JButton(Images.ICON_ADD_Element_24);
		btAddElement.setToolTipText("Add an Assertion");
		ActionListener lsActionAddNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAssertion();
			}
		};
		btAddElement.addActionListener(lsActionAddNode);
		btAddElement.setEnabled(false);
		toolBar.add(btAddElement);

		btSetOptional = new JButton(Images.ICON_ADD_OPTIONAL);
		btSetOptional.setToolTipText("Set the Selected Assertion Optional");
		ActionListener lsActionSetOptional = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DomTreeNode treeNode = domTree.getSelectedTreeNode();
				if (treeNode.isOptional()) {
					setAssertionOptional(false);
					return;
				}
				setAssertionOptional(true);
			}
		};
		btSetOptional.addActionListener(lsActionSetOptional);
		btSetOptional.setEnabled(false);
		toolBar.add(btSetOptional);
		
		btSetIgnorable = new JButton(Images.ICON_ADD_IGNORABLE);
		btSetIgnorable.setToolTipText("Set the Selected Assertion Ignorable");
		ActionListener lsActionSetIgnorable = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DomTreeNode treeNode = domTree.getSelectedTreeNode();
				if (treeNode.isIgnorable()) {
					setAssertionIgnorable(false);
					return;
				}
				setAssertionIgnorable(true);
			}
		};
		btSetIgnorable.addActionListener(lsActionSetIgnorable);
		btSetIgnorable.setEnabled(false);
		toolBar.add(btSetIgnorable);

		btAddComment = new JButton(Images.ICON_COMMENT_24);
		btAddComment.setToolTipText("Add Comment");
		ActionListener lsActionAddComment = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addComment();
			}
		};
		btAddComment.addActionListener(lsActionAddComment);
		btAddComment.setEnabled(false);
		toolBar.add(btAddComment);

		btAddText = new JButton(Images.ICON_ADD_TEXT_24);
		btAddText.setToolTipText("Add Text Value");
		ActionListener lsActionAddText = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText();
			}
		};
		btAddText.addActionListener(lsActionAddText);
		btAddText.setEnabled(false);
		toolBar.add(btAddText);

		btEditElement = new JButton(Images.ICON_EDIT_ELEMENT_24);
		btEditElement.setToolTipText("Edit the Selected Node");
		ActionListener lsActionEditNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editNode();
			}
		};
		btEditElement.addActionListener(lsActionEditNode);
		btEditElement.setEnabled(false);
		toolBar.add(btEditElement);

		btDeleteElement = new JButton(Images.ICON_DELETE_ELEMENT_24);
		btDeleteElement.setToolTipText("Delete the Selected Node");
		ActionListener lsActionDeleteNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteNode();
			}
		};
		btDeleteElement.addActionListener(lsActionDeleteNode);
		btDeleteElement.setEnabled(false);
		toolBar.add(btDeleteElement);

		toolBar.addSeparator();

		btAddAttribute = new JButton(Images.ICON_ADD_ATTRIBUTE_24);
		btAddAttribute.setToolTipText("Add a New Attribute");
		ActionListener lsActionAddAttr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAttribute();
			}
		};
		btAddAttribute.addActionListener(lsActionAddAttr);
		btAddAttribute.setEnabled(false);
		toolBar.add(btAddAttribute);

		btEditAttribute = new JButton(Images.ICON_EDIT_ATTRIBUTE_24);
		btEditAttribute.setToolTipText("Edit the Selected Attribute");
		ActionListener lsActionEditAttr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editAttribute();
			}
		};
		btEditAttribute.addActionListener(lsActionEditAttr);
		btEditAttribute.setEnabled(false);
		toolBar.add(btEditAttribute);

		btDeleteAttribute = new JButton(Images.ICON_DELETE_ATTRIBUTE_24);
		btDeleteAttribute.setToolTipText("Delete the Selected Attribute");
		ActionListener lsActionDeleteAttr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteAttribute();
			}
		};
		btDeleteAttribute.addActionListener(lsActionDeleteAttr);
		btDeleteAttribute.setEnabled(false);
		toolBar.add(btDeleteAttribute);

		toolBar.addSeparator();

		btExpandAll = new JButton(Images.ICON_EXPAND_ALL_24);
		btExpandAll.setToolTipText("Expand the Whole Tree");
		ActionListener lsActionExpandTree = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandTree();
				enableExpandCollapse();
			}
		};
		btExpandAll.addActionListener(lsActionExpandTree);
		btExpandAll.setEnabled(false);
		toolBar.add(btExpandAll);

		btExpand = new JButton(Images.ICON_EXPAND_24);
		btExpand.setToolTipText("Expand the Selected Node");
		ActionListener lsActionExpandNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandNode(domTree.getSelectedTreeNode());
				enableExpandCollapse();
			}
		};
		btExpand.addActionListener(lsActionExpandNode);
		btExpand.setEnabled(false);
		toolBar.add(btExpand);

		btCollapseAll = new JButton(Images.ICON_COLLAPSE_ALL_24);
		btCollapseAll.setToolTipText("Collapse the Whole Tree");
		ActionListener lsActionCollapseTree = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseTree();
				enableExpandCollapse();
			}
		};
		btCollapseAll.addActionListener(lsActionCollapseTree);
		btCollapseAll.setEnabled(false);
		toolBar.add(btCollapseAll);

		btCollapse = new JButton(Images.ICON_COLLAPSE_24);
		btCollapse.setToolTipText("Collapse the Selected Node");
		ActionListener lsActionCollapseNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseNode(domTree.getSelectedTreeNode());
				enableExpandCollapse();
			}
		};
		btCollapse.addActionListener(lsActionCollapseNode);
		btCollapse.setEnabled(false);
		toolBar.add(btCollapse);
	}
	
	private void createWindow() {
		int windowHeight = GUIUtil.getDefaultWindowHeight();
		int windowWidth = GUIUtil.getDefaultWindowWidth();
		int startPositionX = GUIUtil.getStartPositionX(windowWidth);
		int startPositionY = GUIUtil.getStartPositionY(windowHeight);

		super.setTitle(GUIConstants.APPLICATION_NAME);
		setBounds(startPositionX, startPositionY, windowWidth, windowHeight);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		/* Create main split pane */
		final JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setDividerSize(3);
		mainSplitPane.setDividerLocation(windowWidth / 6);
		getContentPane().add(mainSplitPane);

		/* Create scroll pane for policy workspaceDir */
		policyExplorer = new PolicyExplorer(this, policyWorkspacePath);
		mainSplitPane.setLeftComponent(policyExplorer.getExplorer());

		/*
		 * Create a sub split pane in the right split pane of mainSplitPane
		 * to keep assertion workspaceDir, attribute table and policy DOM trees
		 */
		final JSplitPane subSplitPane = new JSplitPane();
		subSplitPane.setOneTouchExpandable(true);
		subSplitPane.setDividerSize(3);
		subSplitPane.setDividerLocation(windowWidth / 2);
		mainSplitPane.setRightComponent(subSplitPane);
		
		/*
		 * Create a vertical split pane in the left split pane of subSplitPane
		 * to keep attribute table and policy DOM trees
		 */
		final JSplitPane midSplitPane = new JSplitPane();
		midSplitPane.setOneTouchExpandable(true);
		midSplitPane.setDividerSize(3);
		midSplitPane.setDividerLocation(windowHeight / 3 * 2);
		midSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		subSplitPane.setLeftComponent(midSplitPane);

		/* Create scroll pane for attribute table */
		final JScrollPane scrollPaneAttrTable = new JScrollPane();
		midSplitPane.setRightComponent(scrollPaneAttrTable);

		/* Create attribute table */
		scrollPaneAttrTable.setViewportView(attrTable);

		/*
		 * Create a tabbed pane in the upper part of midSplitPane to hold
		 * policy DOM trees
		 */
		tabbedPane = new M_TabbedPane(this);
		midSplitPane.setLeftComponent(tabbedPane);

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = e.getSource();
				if (obj instanceof M_TabbedPane) {
					DocumentPane docPane = ((M_TabbedPane) obj)
							.getSelectedDocumentPane();
					refresh(docPane);
				}
			}
		};

		tabbedPane.addChangeListener(changeListener);
		
		/*
		 * Create a vertical split pane in the right split pane of subSplitPane
		 * to keep attribute table and assertion workspaceDir
		 */
		
		final JSplitPane rightSplitPane = new JSplitPane();
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setDividerSize(3);
		rightSplitPane.setDividerLocation(windowHeight / 3 * 2);
		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		subSplitPane.setRightComponent(rightSplitPane);

		/* Create scroll pane for assertion workspaceDir */
		assertionExplorer = new AssertionExplorer(this, assertionWorkspacePath);
        rightSplitPane.setLeftComponent(assertionExplorer.getExplorer());

		WindowListener lsClosingWindow = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int count = tabbedPane.getTabCount();

				for (int i = 0; i < count; i++) {
					if (tabbedPane.getDocumentPaneAt(i).isFileChanged()) {
						tabbedPane.setSelectedIndex(i);
						if (promptToSave()) {
							return;
						}
					}
				}
				System.exit(0);
			}
		};
		addWindowListener(lsClosingWindow);
		
		/* Create scroll pane for properties table */
		final JScrollPane scrollPanePropertiesTable = new JScrollPane();
		rightSplitPane.setRightComponent(scrollPanePropertiesTable);

		/* Create properties table */
		scrollPanePropertiesTable.setViewportView(propertiesTable);
		assertionExplorer.fileTree.addTreeSelectionListener
		    (fileTreeSelectionListener);

	}

	private void deleteAttribute() {
		attrTable.deleteAttribute();
	}

	private void deleteNode() {
		domTree.deleteNode();
	}

	private void editAttribute() {
		attrTable.editAttribute();
	}

	private void editNode() {
		domTree.editNode();
		attrTable.displayAttributes(domTree.getSelectedNode());
	}

	private void enableAttrButtons() {
		Node node = domTree == null ? null : domTree.getSelectedNode();
		
		boolean b1;
		if (!(node instanceof Element) || node.getNodeName().equals
				(PolicyConstants.QNAME_XOR) || node.getNodeName().equals 
				(PolicyConstants.QNAME_AND)) {
			b1 = false;
		} else {
			b1 = true;
		}

		boolean b2 = attrTable.getSelectedRowCount() == 1;

		btAddAttribute.setEnabled(b1);
		btEditAttribute.setEnabled(b2);
		btDeleteAttribute.setEnabled(b2);

		addAttributeMenuItem.setEnabled(b1);
		editAttributeMenuItem.setEnabled(b2);
		deleteAttributeMenuItem.setEnabled(b2);
	}

	private void enableCloseButtons() {
		boolean b1 = tabbedPane.getTabCount() > 0;
		boolean b2 = tabbedPane.getTabCount() > 1;

		btClose.setEnabled(b1);
		btCloseAll.setEnabled(b2);

		closeMenuItem.setEnabled(b1);
		closeOtherMenuItem.setEnabled(b2);
		closeAllMenuItem.setEnabled(b2);
	}

	private void enableComment() {
		Node node = domTree == null ? null : domTree.getSelectedNode();

		/* Allow policy and assertions to have comments. */
		boolean b1 = node instanceof Element
				&& (node.getPrefix() == null || (node.getPrefix() != null && 
						(!node.getPrefix().equals(PolicyConstants
								.WS_POLICY_PREFIX) || node.getNodeName()
								.equals(PolicyConstants.QNAME_POLICY))));

		btAddComment.setEnabled(b1);

		addCommentMenuItem.setEnabled(b1);
	}

	/**
	 * Setzt alle "Expand" und "Collapse" Items in MenuBar sowie Knoepfe in
	 * Toolbar auf dem richtigen Zustand
	 */
	public void enableExpandCollapse() {
		boolean b1 = document != null
				&& DocumentUtil.getDocumentRoot(document).hasChildNodes();
		boolean b2 = domTree == null ? false
				: domTree.getSelectedNode() instanceof Element
						&& domTree.getSelectedNode().hasChildNodes();
		boolean b3 = domTree == null ? false : domTree.isExpanded(domTree
				.getSelectionPath());

		btExpandAll.setEnabled(b1);
		btExpand.setEnabled(b2 && !b3);
		btCollapseAll.setEnabled(b1);
		btCollapse.setEnabled(b2 && b3);

		expandAllMenuItem.setEnabled(b1);
		expandMenuItem.setEnabled(b2 && !b3);
		collapseAllMenuItem.setEnabled(b1);
		collapseMenuItem.setEnabled(b2 && b3);
	}

	private void enableNodeButtons() {
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (!(node instanceof Element)
				|| node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)) {
			b1 = false;
		} else {
			b1 = true;
		}

		boolean b2;
		if (node == null
				|| (node instanceof Element && (node.getPrefix() == null || 
						node.getPrefix().equals(PolicyConstants
								.WS_POLICY_PREFIX)))) {
			b2 = false;
		} else {
			b2 = true;
		}

		boolean b3 = node != null;

		btPolicy.setEnabled(b1);
		btExactlyOne.setEnabled(b1);
		btAll.setEnabled(b1);
		btAddElement.setEnabled(b1);

		btEditElement.setEnabled(b2);

		btDeleteElement.setEnabled(b3);

		addPolicyMenuItem.setEnabled(b1);
		addExactlyoneMenuItem.setEnabled(b1);
		addAllMenuItem.setEnabled(b1);
		addAssertionMenuItem.setEnabled(b1);

		editAssertionMenuItem.setEnabled(b2);

		deleteElementMenuItem.setEnabled(b3);
	}

	private void enableOptional() {
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (node == null) {
			b1 = false;
		} else {
			b1 = node instanceof Element
					&& (node.getPrefix() == null || ((node.getPrefix() != null)
							&& !node.getPrefix().equals(
									PolicyConstants.WS_POLICY_PREFIX)));
		}

		if (b1) {
			if (domTree.getSelectedTreeNode().isOptional()) {
				btSetOptional.setIcon(Images.ICON_REMOVE_OPTIONAL);
				btSetOptional
						.setToolTipText("Set the Selected Assertion Non-" +
								"optional");
			} else {
				btSetOptional.setIcon(Images.ICON_ADD_OPTIONAL);
				btSetOptional
						.setToolTipText("Set the Selected Assertion Optional");
			}
		}

		btSetOptional.setEnabled(b1);
	}
	private void enableIgnorable() {
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1;
		if (node == null) {
			b1 = false;
		} else {
			b1 = node instanceof Element
					&& (node.getPrefix() == null || ((node.getPrefix() != null)
							&& !node.getPrefix().equals(
									PolicyConstants.WS_POLICY_PREFIX)));
		}

		if (b1) {
			if (domTree.getSelectedTreeNode().isIgnorable()) {
				btSetIgnorable.setIcon(Images.ICON_REMOVE_IGNORABLE);
				btSetIgnorable
						.setToolTipText("Set the Selected Assertion Non-" +
								"ignorable");
			} else {
				btSetIgnorable.setIcon(Images.ICON_ADD_IGNORABLE);
				btSetIgnorable
						.setToolTipText("Set the Selected Assertion " +
								"Ignorable");
			}
		}

		btSetIgnorable.setEnabled(b1);
	}
	
	
	private void enablePolicyReferenceButton() {
		Node node = domTree == null ? null : domTree.getSelectedNode();
		boolean b1;
		if (node == null) {
			b1 = false;
		} else {
			b1 = node instanceof Element
					&& (node.getNodeName().equals(PolicyConstants.QNAME_POLICY)
							|| node.getNodeName().equals(
									PolicyConstants.QNAME_XOR) || node
							.getNodeName().equals(PolicyConstants.QNAME_AND));
		}
		btPolicyReference.setEnabled(b1);

		addPolicyReferenceMenuItem.setEnabled(b1);
	}


	/**
	 * Setzt alle "Normalize" "Intersect" und "Merge" Items in MenuBar sowie
	 * Knoepfe in Toolbar auf dem richtigen Zustand
	 */
	public void enableProcessButtons() {
		
		boolean b1 = (policyExplorer.countSelectedPolicies() >= 1);
		boolean b2 = (policyExplorer.countSelectedPolicies() >= 2);

		btNormalize.setEnabled(b1);
		btIntersect.setEnabled(b2);
		btMerge.setEnabled(b2);

		normalizeMenuItem.setEnabled(b1);
		intersectMenuItem.setEnabled(b2);
		mergeMenuItem.setEnabled(b2);
	}

	private void enableSaveButtons() {
		boolean b1 = tabbedPane.getSelectedDocumentPane() != null;

		btSave.setEnabled(isCurrentFileChanged());
		btSaveAs.setEnabled(b1);

		saveMenuItem.setEnabled(isCurrentFileChanged());
		saveAllMenuItem.setEnabled(isCurrentFileChanged());
		saveAsMenuItem.setEnabled(b1);
	}

	private void enableSourceViewer() {
		boolean b1 = tabbedPane.getSelectedDocumentPane() != null
		&& !tabbedPane.getSelectedDocumentPane().isFileChanged();
		
		btViewSource.setEnabled(b1);
		
		viewSourceMenuItem.setEnabled(b1);
	}
	
	
	private void enableTextButton() {
		Node node = domTree == null ? null : domTree.getSelectedNode();

		boolean b1 = (node instanceof Element && (node.getPrefix() == null 
				|| !(node.getPrefix().equals
						(PolicyConstants.WS_POLICY_PREFIX))));

		btAddText.setEnabled(b1);

		addTextValueMenuItem.setEnabled(b1);
	}

	private String getDocumentName() {
		return currentFile == null ? "Untitled" : currentFile.getName();
	}

	/**Liefert policyReferenceURI zurueck
	 * 
	 * @return String policyReferenceURI 
	 */
	public String getPolicyReferenceURI() {
		return policyReferenceURI;
	}

	/**
	 * Diese Methode wird erst aufrufbar, wenn mehr als eine Policy Datei in
	 * dem Policy Workspace selektiert werden. Die geschnitte Policy wird in einer
	 * neuen DocumentPane angezeigt.
	 *
	 *@see   #normalize()
	 *@see   #merge()
	 */
	public void intersect() {

		Vector<File> selectedPolicies = policyExplorer.getSelectedPolicies();

		if (!checkSelectedPolicies(selectedPolicies)) {
			return;
		}
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		int size = selectedPolicies.size();
		Policy result;

		File file1 = selectedPolicies.get(0);
		File file2 = selectedPolicies.get(1);
		result = PolicyUtil.intersect(file1, file2);

		if (size > 2) {
			for (int i = 2; i < size; i++) {
				result = PolicyUtil.intersect(result, selectedPolicies.get(i));
			}
		}

		try {
			File tempFile = File.createTempFile("Intersection", null);
			PolicyWriter writer = new PolicyWriter(tempFile);
			writer.writePolicy(result);
			openDocument(tempFile);

			tempFile.deleteOnExit();
		} catch (IOException e) {
			MOptionPanes.showError(this, e.getMessage());
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Ermittelt, ob die Policy in der aktuell selektierten DocumentPane 
	 * schon geaendert aber noch nicht gespeichert wurde.
	 */
	public boolean isCurrentFileChanged() {
		return tabbedPane.getSelectedDocumentPane() == null ? false
				: tabbedPane.getSelectedDocumentPane().isFileChanged();
	}

	/**
	 * Diese Methode wird erst aufrufbar, wenn mehr als eine Policy Datei im
	 * Policy Workspace selektiert werden. Die Verschmelzungspolicy wird in einer
	 * neuen DocumentPane angezeigt.
	 * 
	 * @see   #normalize()
	 * @see   #merge()
	 */
	public void merge() {

		Vector<File> selectedPolicies = policyExplorer.getSelectedPolicies();

		if (!checkSelectedPolicies(selectedPolicies)) {
			return;
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		int size = selectedPolicies.size();
		Policy result;

		File file1 = selectedPolicies.get(0);
		File file2 = selectedPolicies.get(1);
		result = PolicyUtil.merge(file1, file2);

		if (size > 2) {
			for (int i = 2; i < size; i++) {
				result = PolicyUtil.merge(result, selectedPolicies.get(i));
			}
		}

		try {
			File tempFile = File.createTempFile("Merge", null);
			PolicyWriter writer = new PolicyWriter(tempFile);
			writer.writePolicy(result);
			openDocument(tempFile);

			tempFile.deleteOnExit();
		} catch (IOException e) {
			MOptionPanes.showError(this, e.getMessage());
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Mit dieser Methode laesst sich eine neue Policy mit gegebenen Attribute
	 * erstellen.
	 * 
	 * @param file          die Datei, in der die neue Policy gespeichert wird.
	 * @param policyName    der Policyname-Attribut mit der Form "wsp:Name"
	 * @param Id            das Id-Attribut mit der Form "wsu:Id"
	 * @param description   die Beschreibung mit der Form "wspe:Description"
	 */
	public void newDocument(File file, String policyName, String Id, String 
			description) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		DocumentPane docPane = new DocumentPane(file, this, attrTable);
		docPane.newDocument(policyName, Id, description);
		ImageIcon icon = new ImageIcon("images/middle.jpg");   
		tabbedPane.addTab(file.getName(), icon, docPane, file.getAbsolutePath());
		domTree.setSelectionRow(0);
		currentFile = file;
		enableCloseButtons();

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Bietet einen Zugang zu der Normalisierungsmethode. Die Policy in der
	 * selektierten DocumentPane wird mit dem PolicyReader geparst und
	 * anschliessend normalisiert. Die normalisierte Policy wird in einer
	 * neuen DocumentPane angezeigt.
	 * 
	 * @see   #intersect()
	 * @see   #merge()
	 *
	 */
	public void normalize() {
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		Vector<File> selectedPolicies = policyExplorer.getSelectedPolicies();

		int size = selectedPolicies.size();
		for (int i = 0; i < size; i++){
			File file = selectedPolicies.get(i);

			if (file == null) {
				MOptionPanes.showError(this, "Cannot find the policy.");
				return;
			}

			Policy policy = PolicyReader.parsePolicy(file);

			Policy normalized = (Policy) policy.normalize();

			try {
				File tempFile = File.createTempFile(file.getName(), null);
				PolicyWriter writer = new PolicyWriter(tempFile);
				writer.writePolicy(normalized);
				openDocument(tempFile);

				tempFile.deleteOnExit();
			} catch (IOException e) {
				MOptionPanes.showError(this, e.getMessage());
			}
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Es wird geprueft, ob die Policy in der TabbedPane schon geoeffnet ist.
	 * Ist es der Fall, wird die Methode abgebrochem; ansonsten wird die
	 * Policy der DocumentFactory uebergegeben, der die Policy anschliessend
	 * parst. Nach dem Parsen wird die Policy in einem neuen DocumentPane 
	 * angezeigt. Der Titel der DocumentPane entspricht dem Dateinamen der 
	 * Policy.
	 * 
	 * @param file   die Datei, in der die Policy gespeichert wird.
	 */
	public void openDocument(File file) {
		if (file == null) {
			if (fileChooser.showOpenDialog(this) != 
				    JFileChooser.APPROVE_OPTION)
				return;

			file = fileChooser.getSelectedFile();
		}

		if (tabbedPane.isFileOpened(file)) {
			return;
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		document = DocumentFactory.parseXML(file);
		if (document != null) {
			DocumentPane docPane = new DocumentPane(file, this, attrTable);
			docPane.openDocument(document);
			ImageIcon icon = new ImageIcon("images/middle.jpg");   
			tabbedPane.addTab(file.getName(), icon, docPane, file.getAbsolutePath());
			domTree.setSelectionRow(0);
			enableCloseButtons();
		}
		domTree.expandTree();
		
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Liest von der Benutzer ein, ob er alle Aenderungen der Policy in 
	 * DocumentPanes wirklich speichern will.
	 *  
	 * @return   die Wahl des Benutzers
	 */
	public boolean promptToSave() {
		if (!isCurrentFileChanged())
			return false;

		int result = JOptionPane.showConfirmDialog(this, "Save changes to \""
				+ getDocumentName() + "\"?", GUIConstants.APPLICATION_NAME,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		switch (result) {

		case JOptionPane.YES_OPTION:
			if (save())
				return false;
			return true;

		case JOptionPane.NO_OPTION:
			setCurrentFileChanged(false);
			return false;

		case JOptionPane.CANCEL_OPTION:
			return true;
		}

		return true;
	}
	
	/**
	 * Liest von der Benutzer ein, ob er alle DocumentPanes von der
	 * TabbedPane wirklich entfernen will.
	 * 
	 * @return   die Wahl des Benutzers
	 * @see      #promptToCloseOthers
	 */
	public boolean promptToCloseAll() {

		int result = JOptionPane.showConfirmDialog(this,
				"Do you want to close all tabs?", 
				GUIConstants.APPLICATION_NAME,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		switch (result) {

		case JOptionPane.YES_OPTION:
			return true;

		case JOptionPane.NO_OPTION:
			return false;

		case JOptionPane.CANCEL_OPTION:
			return false;
		}
		return false;
	}
	
	/**
	 * Liest von der Benutzer ein, ob er alle andere DocumentPanes ausser
	 * der aktuell selektierten von der TabbedPane wirklich entfernen will.
	 * 
	 * @return   die Wahl des Benutzers
	 * @see      #promptToCloseAll
	 */
	public boolean promptToCloseOthers() {
		
		int result = JOptionPane.showConfirmDialog(this, 
				"Do you want to close other tabs?", 
				GUIConstants.APPLICATION_NAME,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		switch (result) {
		case JOptionPane.YES_OPTION:
			return true;

		case JOptionPane.NO_OPTION:
			return false;

		case JOptionPane.CANCEL_OPTION:
			return false;
		}

		return false;
	}
	
	private void refresh(DocumentPane docPane) {
		/* No document pane is selected. */
		if (docPane == null) {
			documentPane = null;
			document = null;
			domTree = null;
			currentFile = null;
			return;
		}

		if (domTree != null){
			domTree.removeMouseListener(domTreeMouseListener);
			domTree.removeTreeSelectionListener(domTreeSelectionListener);
			domTree.removeKeyListener(domTreeKeyListener);
		}
		
		if (documentPane != null){
			documentPane.getTreeModel().removeTreeModelListener(domTreeModelListener);
		}
		
		documentPane = docPane;
		document = documentPane.getDocument();
		domTree = documentPane.getDomTree();
		currentFile = documentPane.getFile();

		domTree.addMouseListener(domTreeMouseListener);
		domTree.addTreeSelectionListener(domTreeSelectionListener);
		domTree.addKeyListener(domTreeKeyListener);
		documentPane.getTreeModel().addTreeModelListener(domTreeModelListener);
		
		M_TreeCellsRenderer renderer = new M_TreeCellsRenderer();
		if (domTreeCellEditor != null){
			domTreeCellEditor.removeCellEditorListener(cellEditorListener);
		}
		domTreeCellEditor = new M_TreeCellsEditor(domTree, renderer);
		domTree.setCellEditor(domTreeCellEditor);
		domTreeCellEditor.addCellEditorListener(cellEditorListener);

		attrTable.displayAttributes(domTree.getSelectedNode());

		setButtons();
	}

	private void beSaved(int curIdx) {
		if (curIdx == -1) {
			curIdx = PolicyEditor.tabbedPane.getSelectedIndex();
		}	
		String curTitle = PolicyEditor.tabbedPane.getTitleAt(curIdx);
		if (curTitle.charAt(0) == '*') {
			PolicyEditor.tabbedPane.setTitleAt(curIdx, curTitle.substring(1));
		}
	}
	
	/**
	 * Realisiert die Speicherfunktion. Sie ruft die Methode "write" in der
	 * DocumentFactroy auf, mit der die Policy in das DateiSystem gespeichert
	 * wird.
	 * 
	 * @return  ob diese Operation komplett ausgefuehrt wird.
	 * @see     #saveAll()
	 * @see     #saveAs()
	 */
	public boolean save() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			DocumentFactory.write(documentPane.getDocument(), currentFile);

		} catch (Exception e) {
			e.printStackTrace();
			MOptionPanes.showError(this, "Error by saving the file.");
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		setCurrentFileChanged(false);	
		beSaved(-1);
		
		return true;
	}
	
	/**
	 * Speichert alle Policies ins Dateisystem, die in DocumentPane geoeffnet werden.
	 * 
	 * @return  ob diese Operation komplett und richtig ausgefuehrt wird.
	 * @see     #save()
	 * @see     #saveAs()
	 */
	public boolean saveAll() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		try {
			int count = tabbedPane.getTabCount();
			
			for (int i = 0; i < count; i++) {
				DocumentFactory.write(tabbedPane.getDocumentPaneAt(i)
						.getDocument(), tabbedPane.getDocumentPaneAt(i)
						.getFile());
				tabbedPane.getDocumentPaneAt(i).setFileChanged(false);	
				beSaved(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MOptionPanes.showError(this, "Error by saving the file.");
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
		setCurrentFileChanged(false);
		beSaved(-1);

		return true;
	}

	/**
	 * Bietet dem Benutzer die Moeglichkeit an, die Policy in einer anderen
	 * Datei zu speichern. Dafuer wird ein Speicherdialog geladen, mit dem
	 * der Benutzer die Zieldatei mit Maus selektieren oder ueber die Tastatur
	 * eingeben kann.
	 * 
	 * @return  ob diese Operation komplett und richtig ausgefuehrt wird.
	 * @see     #save
	 * @see     #saveAll
	 */
	public boolean saveAs() {
		if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		File fileToSave = fileChooser.getSelectedFile();

		if (fileToSave == null) {
			return false;
		}

		boolean closeThisTab;
		File fileToOpen;
		if (fileToSave == currentFile) {
			closeThisTab = false;
			fileToOpen = fileToSave;
		} else {
			closeThisTab = true;
			if (!fileToSave.getName().endsWith(GUIConstants.POLICY_EXTENSION)) {
				String newFilePath = fileToSave.getPath()
						+ GUIConstants.POLICY_EXTENSION;
				fileToOpen = new File(newFilePath);
			} else {
				fileToOpen = fileToSave;
			}
		}

		currentFile = fileToOpen;

		boolean saveResult = save();

		/* If save successful */
		if (saveResult) {
			if (closeThisTab) {

				tabbedPane.closeTab();
				openDocument(fileToOpen);

			}

			return true;
		}

		return false;
	}

	/**
	 * Setzt eine Assertion "Optional" oder "Non-Optional"
	 * 
	 */
	public void setAssertionOptional(boolean isOptional) {
		if (isOptional) {
			attrTable.addAttributeOptional();
			enableOptional();
			return;
		}

		attrTable.removeAttributeOptional();
		enableOptional();
	}
	
	/**
	 * Setzt eine Assertion "Ignorable" oder "Non-Ignorable"
	 * 
	 */
	public void setAssertionIgnorable(boolean isIgnorable) {
		if (isIgnorable) {
			attrTable.addAttributeIgnorable();
			enableIgnorable();
			return;
		}

		attrTable.removeAttributeIgnorable();
		enableIgnorable();
	}

	private void setButtons() {
		enableAttrButtons();
		enableCloseButtons();
		enableComment();
		enableExpandCollapse();
		enableNodeButtons();
		enableOptional();
		enableIgnorable();
		enablePolicyReferenceButton();
		enableProcessButtons();
		enableSaveButtons();
		enableSourceViewer();
		enableTextButton();
	}
	
	private void beingChanged() {
	    int curIdx = PolicyEditor.tabbedPane.getSelectedIndex();
		String curTitle = PolicyEditor.tabbedPane.getTitleAt(curIdx);
		if (curTitle.charAt(0) != '*') {
			PolicyEditor.tabbedPane.setTitleAt(curIdx, "* " + curTitle);
		}
	}
	
	/**
	 * Setzt den Zustand der aktuellen in DocumentPane angezeigten Policydatei
	 * auf "changed" oder nicht "changed".
	 */
	public void setCurrentFileChanged(boolean currentFileChanged) {
		if (tabbedPane.getSelectedDocumentPane() == null) {
			MOptionPanes.showError(this, "No policy has been opened.");
			return;
		}
		tabbedPane.getSelectedDocumentPane().setFileChanged(currentFileChanged);
		setButtons();
		domTree.requestFocus();
		beingChanged();
	}

	/**
	 * Weist PolicyReference das gegebene URI zu 
	 */
	public void setPolicyReferenceURI(String URI) {
		policyReferenceURI = URI;
	}

	/**
	 * Bietet dem Benutzer die Moeglichkeit an, den Quellecode einer Policy
	 * anzeigen zu lassen.
	 */
	public void showSourceViewer() {
		if (currentFile == null) {
			return;
		}

		JDialog.setDefaultLookAndFeelDecorated(true);
		SourceViewer viewer = new SourceViewer(this, currentFile);

		viewer.setVisible(true);
	}

	private void showURIInputDialog(String altURI) {
		JDialog.setDefaultLookAndFeelDecorated(true);
		RefURIInputDialog dialog = new RefURIInputDialog(this,altURI);
		dialog.setVisible(true);
	}

	/**
	 * Aktualisiert die PropertiesTable unter dem AssertionWorkspace
	 *
	 */
	public void refreshPropertiesTable(){
		propertiesTable.displayProperties(assertionExplorer
				.getSelectedFileNode());
	}

	
	public static void main(String args[]) {
		try {
			JDialog.setDefaultLookAndFeelDecorated(true);
			PolicyEditor frame = new PolicyEditor("D:\\Desktop\\Policies\\",
					"D:\\Desktop\\Assertions\\");
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TableModelListener attrTableModelListener = 
		    new TableModelListener() {
		public void tableChanged(TableModelEvent e) {
			setCurrentFileChanged(true);				
		}
	};

	private MouseListener attrTableMouseListener = new MouseListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
				if (e.getSource() instanceof AttrTable) {
					int row = attrTable.getSelectedRow();
					int column = attrTable.getSelectedColumn();
					String columnName = attrTable.getColumnName(column);

					if ((row != -1) && (column != -1)) {
						AttrTableModel model = (AttrTableModel) attrTable
								.getModel();
						Node sourceNode = model.getSourceNode();
						if (sourceNode.getNodeName().equals(
								PolicyConstants.QNAME_POLICY_REFERENCE)) {
							if (columnName == AttrTableModel.columnNames[0]) {
								MOptionPanes
										.showError(PolicyEditor.this,
												"Sorry, you cannot change the " +
												"attribute name \"URI\".");
								return;
							}

							showURIInputDialog(attrTable.getValueAt(row, column)
									.toString());

							if (getPolicyReferenceURI() == null ||
									getPolicyReferenceURI().isEmpty()) {
								return;
							}

							attrTable.setPolicyReferenceURI(
									getPolicyReferenceURI());
						}
					}
				}
				attrTable.editAttribute();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}
	};
	
	private MouseListener propertiesTableMouseListener = new MouseListener() {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
				if (e.getSource() instanceof PropertiesTable) {
					int row = propertiesTable.getSelectedRow();
					int column = propertiesTable.getSelectedColumn();
					
					if ((row == -1) || (column == -1)){
						return;
					}

					/* Editing column is column "Properties" */
					if (column == 0) {
						
						/* Does not allow to change properties name */
						MOptionPanes.showError(PolicyEditor.this,
							"Sorry, you cannot change the property name.");
				        return; 
				        
				    /* Editing column is column "Value" */
					} else {
						
						/* Does not allow to change path*/
						if (row == 4){
							MOptionPanes.showError(PolicyEditor.this,
							    "Sorry, you cannot change the path.");
				            return;
						}

						propertiesTable.setCellEditable(row, column);
						
			        }
				}
				
			}
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseEntered
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseExited
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mousePressed
		 * (java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseReleased
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}
	};

	private KeyListener attrTableKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F2){
				int row = attrTable.getSelectedRow();
				int	column = attrTable.getSelectedColumn();
				
				if ((row == -1) || (column == -1)){
					return;
				}
				
				String columnName = attrTable.getColumnName(column);

				/* Editing column is column "Attribute" */
				if (columnName == AttrTableModel.columnNames[0]) {
					
					if (tableModel.getSourceNode().getNodeName().equals(
							PolicyConstants.QNAME_POLICY_REFERENCE)) {
						MOptionPanes.showError(PolicyEditor.this,
								"Sorry, you cannot change the attribute " +
								"name \"URI\".");
			        	return;   
					}
					
					String oldName = attrTable.getValueAt(row, column)
					    .toString();

					/* Does not allow to change WS-Policy namespace */
					if (oldName.equals(PolicyConstants.WS_POLICY_NS_NAME)) {
						MOptionPanes.showError(PolicyEditor.this,
								Messages.NO_CHANGE_ON_POLICY_NS);
						return;
					}
					
				} else {
					
					if (tableModel.getSourceNode().getNodeName().equals(
							PolicyConstants.QNAME_POLICY_REFERENCE)) {
						
						showURIInputDialog(attrTable.getValueAt(row, column)
								.toString());

						if (getPolicyReferenceURI() == null ||
								getPolicyReferenceURI().isEmpty()) {
							return;
						}

						attrTable.setPolicyReferenceURI(getPolicyReferenceURI());	
						attrTable.editAttribute();
						return;
					}
					
					/* Editing column is column "Value" */
					String oldValue = attrTable.getValueAt(row, column).toString();

					/* Does not allow to change WS-Policy namespace */
					if (oldValue.equals(PolicyConstants.WS_POLICY_NAMESPACE_URI)) {
						MOptionPanes.showError(PolicyEditor.this,
								Messages.NO_CHANGE_ON_POLICY_NS);
						return;
					}
		        }
		        tableModel.setCellEditable (row, column);
			} else if (e.getKeyCode() == KeyEvent.VK_DELETE){
				deleteAttribute();
			} else if (e.getKeyCode() == KeyEvent.VK_INSERT){
				addAttribute();
			} else  if (e.getKeyCode() == KeyEvent.VK_S && e.getModifiers() == 
				    KeyEvent.CTRL_MASK){
				save();
			}
		}

		public void keyReleased(KeyEvent arg0) {	
		}

		public void keyTyped(KeyEvent arg0) {	
		}
		
	};
	
	private KeyListener propertiesTableKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F2){
				int row = propertiesTable.getSelectedRow();
				int	column = propertiesTable.getSelectedColumn();
				
				if ((row == -1) || (column == -1)){
					return;
				}

				/* Editing column is column "Properties" */
				if (column == 0) {
					
					/* Does not allow to change properties name */
					MOptionPanes.showError(PolicyEditor.this,
						"Sorry, you cannot change the property name.");
			        return; 
			        
			    /* Editing column is column "Value" */
				} else {
					
					/* Does not allow to change path*/
					if (row == 4){
						MOptionPanes.showError(PolicyEditor.this,
						    "Sorry, you cannot change the path.");
			            return;
					}

					propertiesTable.setCellEditable(row, column);
					
		        }
			} 
		}

		public void keyReleased(KeyEvent arg0) {	
		}

		public void keyTyped(KeyEvent arg0) {	
		}
		
	};
	
	private KeyListener domTreeKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F2){
				if (domTree.getSelectedNode() == null){
					return;
				}

				if (!btEditElement.isEnabled()) {
					
					/* Does not allow to change properties name */
					MOptionPanes.showError(PolicyEditor.this,
						"Sorry, you cannot change this Node.");
			        return; 
				}
			     
				if (domTree.getSelectedNode().getNodeType() ==  
					    Node.ELEMENT_NODE){
					domTree.editElement();
				} else {
					editingDomNode = domTree.getSelectedNode();
				    domTreeCellEditor.setEditImmediately(true);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_S){
				if (e.getModifiers() == KeyEvent.CTRL_MASK){
					save();
				} else if (e.getModifiers() == KeyEvent.CTRL_MASK + 
						KeyEvent.SHIFT_MASK) {
					saveAll();
				}
			}  else if (e.getKeyCode() == KeyEvent.VK_W){
				if (e.getModifiers() == KeyEvent.CTRL_MASK){
					closeTab();
				} else if (e.getModifiers() == KeyEvent.CTRL_MASK + 
						KeyEvent.SHIFT_MASK){
					closeAllTabs();
				} 
			} else  if (e.getKeyCode() == KeyEvent.VK_DELETE){
				if (domTree.getSelectedNode() != null){
					deleteNode();
				}	
			}
		}

		public void keyReleased(KeyEvent arg0) {	
		}

		public void keyTyped(KeyEvent arg0) {	
		}
		
	};
	
	private ListSelectionListener attrTableSelectionListener = new 
	      ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			enableAttrButtons();
		}
	};
	
	private TreeModelListener domTreeModelListener = new TreeModelListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeModelListener#treeNodesChanged
		 * (javax.swing.event.TreeModelEvent)
		 */
		public void treeNodesChanged(TreeModelEvent e) {
	    	domTreeCellEditor.setEditImmediately(false);
	    	
	    	boolean isComment;
	    	if (editingDomNode.getNodeType() == Node.COMMENT_NODE) {
	    		isComment = true;
	    	} else {
	    		isComment = false;
	    	}
	    	
	    	String newNodeValue = domTreeCellEditor.getCellEditorValue()
	    	    .toString();

	    	TreePath path = domTree.getSelectionPath().getParentPath();
			Node parentNode = editingDomNode.getParentNode();
			DomTreeNode parentTreeNode = (DomTreeNode) domTree
			    .getSelectedTreeNode().getParent();
	
			parentNode.removeChild(editingDomNode);
			domTree.getSelectedTreeNode().removeFromParent();
			
			Document doc = parentNode.getOwnerDocument();
			DomTreeNode newXMLNode;
			if (isComment){
				Comment comment = doc.createComment(newNodeValue);
		    	newXMLNode = new DomTreeNode(comment);
			} else {
				Text text = doc.createTextNode(newNodeValue);
                newXMLNode = new DomTreeNode(text);
			}
			parentTreeNode.addNode(newXMLNode);

			documentPane.getTreeModel().nodeStructureChanged(parentTreeNode);
			domTree.expandNode(parentTreeNode);

			if (path != null) {
				path = path.pathByAddingChild(newXMLNode);
				domTree.setSelectionPath(path);
				domTree.scrollPathToVisible(path);
			}
			setCurrentFileChanged(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeModelListener#treeNodesInserted
		 * (javax.swing.event.TreeModelEvent)
		 */
		public void treeNodesInserted(TreeModelEvent e) {
			setCurrentFileChanged(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeModelListener#treeNodesRemoved
		 * (javax.swing.event.TreeModelEvent)
		 */
		public void treeNodesRemoved(TreeModelEvent e) {
			setCurrentFileChanged(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeModelListener#treeStructureChanged
		 * (javax.swing.event.TreeModelEvent)
		 */
		public void treeStructureChanged(TreeModelEvent e) {
			setCurrentFileChanged(true);
		}
	
	};
	
	protected CellEditorListener cellEditorListener = new CellEditorListener(){

		public void editingCanceled(ChangeEvent arg0) {
			domTreeCellEditor.setEditImmediately(false);		
		}

		public void editingStopped(ChangeEvent arg0) {
		
		}
		
	};

	private MouseListener domTreeMouseListener = new MouseListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseClicked
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
				TreePath path = domTree.getPathForLocation(e.getX(), e.getY());

				if (path == null) {
					return;
				}

				domTree.setSelectionPath(path);

				Node node = domTree.getSelectedNode();

				if (node == null){
					MOptionPanes.showError(PolicyEditor.this, "null node.");
					return;
				}
				
				if (node instanceof Element && (node.getPrefix() == null)){
					MOptionPanes.showError(PolicyEditor.this, "null prefix.");
					return;
				}
							
				if (node.getNodeName().equals(PolicyConstants
						.QNAME_POLICY_REFERENCE)){
					
					showURIInputDialog(attrTable.getValueAt(0, 1).toString());

					if (getPolicyReferenceURI() == null ||
							getPolicyReferenceURI().isEmpty()) {
						return;
					}

					attrTable.setPolicyReferenceURI(getPolicyReferenceURI());
					attrTable.changeSelection(0, 1, false, false);
					attrTable.editAttribute();
					
		    		return;
				}
				
			    if (node instanceof Element && (node.getPrefix().equals
			    		(PolicyConstants.WS_POLICY_PREFIX))){
		    		MOptionPanes.showError(PolicyEditor.this,
						   "You cannot edit a policy operator.");
					return;
				}

				editNode();
			}
			return;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseEntered
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseExited
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mousePressed
		 * (java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseReleased
		 * (java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}
	};

	private TreeSelectionListener domTreeSelectionListener = new 
	    TreeSelectionListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeSelectionListener#valueChanged
		 * (javax.swing.event.TreeSelectionEvent)
		 */
		public void valueChanged(TreeSelectionEvent e) {
			Node node = domTree.getSelectedNode();
			attrTable.displayAttributes(node);
			editingDomNode = domTree.getSelectedNode();
			setButtons();
		}
	};
	
	private TreeSelectionListener fileTreeSelectionListener = new 
	    TreeSelectionListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.TreeSelectionListener#valueChanged
		 * (javax.swing.event.TreeSelectionEvent)
		 */
		public void valueChanged(TreeSelectionEvent e) {
		    refreshPropertiesTable();
		}
	};
	
}
