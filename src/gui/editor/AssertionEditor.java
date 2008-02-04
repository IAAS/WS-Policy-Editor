package gui.editor;

import gui.explorer.AssertionExplorer;
import gui.images.Images;
import gui.util.GUIConstants;
import gui.util.GUIUtil;
import gui.util.InputDialogPattern;
import gui.util.M_FileChooser;
import gui.util.M_TreeCellsEditor;
import gui.util.M_TreeCellsRenderer;

import java.awt.BorderLayout;
import java.awt.Cursor;
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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.policy.util.PolicyConstants;

import util.DocumentUtil;
import util.MOptionPanes;
import util.Messages;

/**
 * Diese Klasse implementiert einen Editor speziell fuer die Bearbeitung
 * von Policy Assertions. Der Assertion Editor kann durch den Menueeintrag 
 * "New Assertion" in dem Kontextmenue von dem Assertion Workspace geladen
 * werden. Der Assertion Editor ist an den Policy Editor gebunden. Bevor der
 * Assertion Editor geschlossen wird, ist der Policy Editor nicht zugaengig.
 * Das Hauptfenster des Assertion Editors ist aehnlich wie das Hauptfenster
 * des Policy Editors. Der einzige Unterschied liegt daran, dass im Assertion
 * Editor keine Workspaces fuer Polices und Policy Assertions.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */

public class AssertionEditor extends JDialog{

	private static final String APP_NAME = 
			GUIConstants.APPLICATION_NAME + " - Assertion Editor";

	private static final long serialVersionUID = -6598183348177774997L;

	private AssertionExplorer assertionExplorer;

	private AttrTable attrTable;

	private JButton btAddAttribute;

	private JButton btAddElement;

	private JButton btAddText;

	private JButton btAll;

	private JButton btCollapse;

	private JButton btCollapseAll;

	private JButton btDeleteAttribute;

	private JButton btDeleteElement;

	private JButton btEditAttribute;

	private JButton btEditElement;

	private JButton btExactlyOne;

	private JButton btExpand;

	private JButton btExpandAll;

	private JButton btPolicy;

	private JButton btSave;

	private JButton btSaveAs;

	private File currentFile;

	private boolean currentFileChanged = false;

	private Document document;

	private DomTree domTree;
	
	private Node editingDomNode;

	private M_TreeCellsEditor domTreeCellEditor;

	private JFileChooser fileChooser;

	private boolean isNewFile = false;

	private AssertionEditor owner;

	private Element root;

	private JMenuItem saveMenuItem;

	private AttrTableModel tableModel;

	private DefaultTreeModel treeModel;

	private int windowHeight;

	private int windowWidth;

	private JMenuItem saveAsMenuItem;

	private JMenuItem addPolicyMenuItem;

	private JMenuItem addExactlyoneMenuItem;

	private JMenuItem addAllMenuItem;

	private JMenuItem addAssertionMenuItem;

	private JMenuItem addTextValueMenuItem;

	private JMenuItem addAttributeMenuItem;

	private JMenuItem editAssertionMenuItem;

	private JMenuItem editAttributeMenuItem;

	private JMenuItem deleteElementMenuItem;

	private JMenuItem deleteAttributeMenuItem;

	private JMenuItem expandMenuItem;

	private JMenuItem expandAllMenuItem;

	private JMenuItem collapseMenuItem;

	private JMenuItem collapseAllMenuItem;

	private JButton btSetOptional;
	
	private JButton btSetIgnorable;

	private JButton btAddComment;

	private JMenuItem addCommentMenuItem;

	private JMenuItem viewSourceMenuItem;

	private JButton btViewSource;


	/**
	 * 
	 * @param frame    eine Oberkomponente, Policy Editor
	 * @param cause    eine Oberkomponente, Assertion Explorer
	 * @param aFile    die Datei, die oeffnet und ausgehangen wird
	 * @param isNew    ob die Datei neu angelegt ist
	 */
	public AssertionEditor(PolicyEditor frame, AssertionExplorer cause,
			File aFile, boolean isNew) {
		super(frame, APP_NAME, true);

		owner = this;
		assertionExplorer = cause;
		currentFile = aFile;
		isNewFile = isNew;

		setWindowWidth(800);
		setWindowHeight(450);

		setSize(getWindowWidth(), getWindowHeight());
		getContentPane().setLayout(new BorderLayout());

		createMenuBar();

		createTreeModel();

		createTree();

		createAttrTable();

		fileChooser = M_FileChooser.getAssertionFileChooser(assertionExplorer
				.getWorkspaceDir());

		createToolbar();

		createWindow();

		setWindowClosingOperation();

		/*
		 * Set application title and when the parameter isNew is true then
		 * create a new document, otherwise open the document "aFile"
		 */
		if (!isNewFile) {
			openDocument(aFile);
		}

		if (currentFile != null) {
			setAppTitle(currentFile.getName());
		}
		
		setButtons();
		
		this.addKeyListener(escKeyListener);
		this.setFocusable(true);

	}

	/* Add new Element Node to the Document. */
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
	
	private void addText() {
		domTree.addTextNode();
	}

	/**
	 * Lege eine neue Wurzel mit den spezifizierten Eigenschaften an.
	 * 
	 * @param assertionName  der Name der Assertion
	 * @param prefix         das Praefix der Assertion
	 * @param nsURI          der Namensraum der Assertion
	 * @param description    die Beschreibung der Assertion
	 */
	public void addRootElement(String assertionName, String prefix,
			String nsURI, String description) {
		
		String qName = prefix + ":" + assertionName;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		document = DocumentFactory.createNewDocument();
		root = DocumentUtil.createElementNS(document, nsURI, qName, true);

		root.normalize();
		document.appendChild(root);

		DomTreeNode top = new DomTreeNode(root);
		treeModel.setRoot(top);
		domTree.treeDidChange();
		domTree.expandTree();
		if (!description.isEmpty()){
			root.setAttribute(PolicyConstants.XMLNS_PREFIX + ":" +
					PolicyConstants.WSPE_PREFIX, PolicyConstants.
					WSPE_NAMESPACE_URI);
			root.setAttribute(PolicyConstants.WSPE_PREFIX + ":" + 
					"Description", description);
		}
		tableModel.fireTableStructureChanged();
		displayAttributes(root);
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setButtons();
		saveFile(false);
		domTree.setSelectionRow(0);
		domTree.requestFocus();
	}

	private boolean checkDocument() {
		int result = MOptionPanes.getComfirmResult(owner,
				Messages.NO_SAVE_EMPTY_DOC);
		if (result == JOptionPane.YES_OPTION) {
			assertionExplorer.removeEmptyFile();
			return true;
		}
		return false;
	}

	private void createAttrTable() {
		/* Create attribute table and add selection listener to this table */
		tableModel = new AttrTableModel();
		tableModel.addTableModelListener(tableModelListener);
		attrTable = new AttrTable(tableModel);
		ListSelectionListener lsListSelection = new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				enableAttrButtons();
			}
		};
		attrTable.getSelectionModel().addListSelectionListener(
				lsListSelection);
		attrTable.addKeyListener(attrTableKeyListener);
		MouseListener mouseListener = new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)
						&& e.getClickCount() == 2) {
					if (e.getSource() instanceof AttrTable) {
						attrTable.editAttribute();
					}
				}
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		};

		attrTable.addMouseListener(mouseListener);
	}

	private void createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu fileMenu = new JMenu();
		fileMenu.setText("File");
		menuBar.add(fileMenu);

		saveMenuItem = new JMenuItem();
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(false);
			}
		});
		saveMenuItem.setText("Save");
		saveMenuItem.setIcon(Images.ICON_SAVE_16);
		fileMenu.add(saveMenuItem);

		saveAsMenuItem = new JMenuItem();
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(true);
			}
		});
		saveAsMenuItem.setText("Save as...");
		saveAsMenuItem.setIcon(Images.ICON_SAVE_AS_16);
		fileMenu.add(saveAsMenuItem);

		fileMenu.addSeparator();

		viewSourceMenuItem = new JMenuItem();
		viewSourceMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				showSourceViewer();
			}
		});
		viewSourceMenuItem.setText("View Source");
		viewSourceMenuItem.setIcon(Images.ICON_VIEW_SOURCE_16);
		fileMenu.add(viewSourceMenuItem);

		fileMenu.addSeparator();

		final JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
				domTree.addOperatorPolicy();
			}
		});
		addPolicyMenuItem.setText("wsp:Policy");
		addPolicyMenuItem.setIcon(Images.ICON_POLICY_OPERATOR_16);
		addMenu.add(addPolicyMenuItem);

		addExactlyoneMenuItem = new JMenuItem();
		addExactlyoneMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.addOperatorExactlyOne();
			}
		});
		addExactlyoneMenuItem.setText("wsp:ExactlyOne");
		addExactlyoneMenuItem.setIcon(Images.ICON_XOR_16);
		addMenu.add(addExactlyoneMenuItem);

		addAllMenuItem = new JMenuItem();
		addAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.addOperatorAll();
			}
		});
		addAllMenuItem.setText("wsp:All");
		addAllMenuItem.setIcon(Images.ICON_ALL_16);
		addMenu.add(addAllMenuItem);

		addMenu.addSeparator();

		addAssertionMenuItem = new JMenuItem();
		addAssertionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.addAssertion();
			}
		});
		addAssertionMenuItem.setText("Assertion");
		addAssertionMenuItem.setIcon(Images.ICON_ADD_Element_16);
		addMenu.add(addAssertionMenuItem);

		addCommentMenuItem = new JMenuItem();
		addCommentMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.addComment();
			}
		});
		addCommentMenuItem.setText("Comment");
		addCommentMenuItem.setIcon(Images.ICON_COMMENT_16);
		addMenu.add(addCommentMenuItem);

		addTextValueMenuItem = new JMenuItem();
		addTextValueMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				domTree.addTextNode();
			}
		});
		addTextValueMenuItem.setText("Text Value");
		addTextValueMenuItem.setIcon(Images.ICON_ADD_TEXT_16);
		addMenu.add(addTextValueMenuItem);

		addMenu.addSeparator();

		addAttributeMenuItem = new JMenuItem();
		addAttributeMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				attrTable.addAttribute();
			}
		});
		addAttributeMenuItem.setText("Attribute");
		addAttributeMenuItem.setIcon(Images.ICON_ADD_ATTRIBUTE_16);
		addMenu.add(addAttributeMenuItem);

		final JMenu editMainMenu = new JMenu();
		editMainMenu.setText("Edit");
		editMenu.add(editMainMenu);

		editAssertionMenuItem = new JMenuItem();
		editAssertionMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				editNode();
			}
		});
		editAssertionMenuItem.setText("Tree Node");
		editAssertionMenuItem.setIcon(Images.ICON_EDIT_ELEMENT_16);
		editMainMenu.add(editAssertionMenuItem);

		editMainMenu.addSeparator();

		editAttributeMenuItem = new JMenuItem();
		editAttributeMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				attrTable.editAttribute();
			}
		});
		editAttributeMenuItem.setText("Attribute");
		editAttributeMenuItem.setIcon(Images.ICON_EDIT_ATTRIBUTE_16);
		editMainMenu.add(editAttributeMenuItem);

		final JMenu deleteMenu = new JMenu();
		deleteMenu.setText("Delete");
		editMenu.add(deleteMenu);

		deleteElementMenuItem = new JMenuItem();
		deleteElementMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.deleteNode();
			}
		});
		deleteElementMenuItem.setText("Delete the Selected Tree Node");
		deleteElementMenuItem.setIcon(Images.ICON_DELETE_ELEMENT_16);
		deleteMenu.add(deleteElementMenuItem);

		deleteMenu.addSeparator();

		deleteAttributeMenuItem = new JMenuItem();
		deleteAttributeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attrTable.deleteAttribute();
			}
		});
		deleteAttributeMenuItem.setText("Attribute");
		deleteAttributeMenuItem.setIcon(Images.ICON_DELETE_ATTRIBUTE_16);
		deleteMenu.add(deleteAttributeMenuItem);

		final JMenu expandMenu = new JMenu();
		expandMenu.setText("Expand");
		menuBar.add(expandMenu);

		expandMenuItem = new JMenuItem();
		expandMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandNode(domTree.getSelectedTreeNode());
			}
		});
		expandMenuItem.setText("Expand");
		expandMenuItem.setIcon(Images.ICON_EXPAND_16);
		expandMenu.add(expandMenuItem);

		expandAllMenuItem = new JMenuItem();
		expandAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandTree();
			}
		});
		expandAllMenuItem.setText("Expand All");
		expandAllMenuItem.setIcon(Images.ICON_EXPAND_ALL_16);
		expandMenu.add(expandAllMenuItem);

		final JMenu collapseMenu = new JMenu();
		collapseMenu.setText("Collapse");
		menuBar.add(collapseMenu);

		collapseMenuItem = new JMenuItem();
		collapseMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseNode(domTree.getSelectedTreeNode());
			}
		});
		collapseMenuItem.setText("Collapse");
		collapseMenuItem.setIcon(Images.ICON_COLLAPSE_16);
		collapseMenu.add(collapseMenuItem);

		collapseAllMenuItem = new JMenuItem();
		collapseAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseTree();
			}
		});
		collapseAllMenuItem.setText("Collapse All");
		collapseAllMenuItem.setIcon(Images.ICON_COLLAPSE_ALL_16);
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

	private JToolBar createToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		btSave = new JButton(Images.ICON_SAVE_24);
		btSave.setToolTipText("Save");
		ActionListener lsActionSave = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(false);
			}
		};
		btSave.addActionListener(lsActionSave);
		btSave.setEnabled(false);
		toolBar.add(btSave);

		btSaveAs = new JButton(Images.ICON_SAVE_AS_24);
		btSaveAs.setToolTipText("Save as...");
		ActionListener lsActionSaveAs = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(true);
			}
		};
		btSaveAs.addActionListener(lsActionSaveAs);
		btSaveAs.setEnabled(false);
		toolBar.add(btSaveAs);

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

		toolBar.addSeparator();

		btAddElement = new JButton(Images.ICON_ADD_Element_24);
		btAddElement.setToolTipText("Add a Subnode");
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
		btAddText.setToolTipText("Add the Text Value of an Assertion");
		ActionListener lsActionAddText = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText();
			}
		};
		btAddText.addActionListener(lsActionAddText);
		btAddText.setEnabled(false);
		toolBar.add(btAddText);

		btEditElement = new JButton(Images.ICON_EDIT_ELEMENT_24);
		btEditElement.setToolTipText("Edit");
		ActionListener lsActionEditNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editNode();
			}
		};
		btEditElement.addActionListener(lsActionEditNode);
		btEditElement.setEnabled(false);
		toolBar.add(btEditElement);

		btDeleteElement = new JButton(Images.ICON_DELETE_ELEMENT_24);
		btDeleteElement.setToolTipText("Delete");
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
		btAddAttribute.setToolTipText("Add New Attribute");
		ActionListener lsActionAddAttr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAttribute();
			}
		};
		btAddAttribute.addActionListener(lsActionAddAttr);
		btAddAttribute.setEnabled(false);
		toolBar.add(btAddAttribute);

		btEditAttribute = new JButton(Images.ICON_EDIT_ATTRIBUTE_24);
		btEditAttribute.setToolTipText("Edit Attribute");
		ActionListener lsActionEditAttr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editAttribute();
			}
		};
		btEditAttribute.addActionListener(lsActionEditAttr);
		btEditAttribute.setEnabled(false);
		toolBar.add(btEditAttribute);

		btDeleteAttribute = new JButton(Images.ICON_DELETE_ATTRIBUTE_24);
		btDeleteAttribute.setToolTipText("Delete Attribute");
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
		btExpandAll.setEnabled(true);
		toolBar.add(btExpandAll);

		btExpand = new JButton(Images.ICON_EXPAND_24);
		btExpand.setToolTipText("Expand the Selected Tree Node");
		ActionListener lsActionExpandNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.expandTree();
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
		btCollapseAll.setEnabled(true);
		toolBar.add(btCollapseAll);

		btCollapse = new JButton(Images.ICON_COLLAPSE_24);
		btCollapse.setToolTipText("Collapse the Selected Tree Node");
		ActionListener lsActionCollapseNode = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domTree.collapseNode(domTree.getSelectedTreeNode());
				enableExpandCollapse();
			}
		};
		btCollapse.addActionListener(lsActionCollapseNode);
		btCollapse.setEnabled(false);
		toolBar.add(btCollapse);

		return toolBar;
	}

	private void createTree() {
		/* Create dom tree and it's renderer and gui.editor */
		domTree = new DomTree(treeModel);
		M_TreeCellsRenderer renderer = new M_TreeCellsRenderer();
		domTree.setCellRenderer(renderer);
		domTreeCellEditor = new M_TreeCellsEditor(domTree, renderer);
		domTree.addKeyListener(domTreeKeyListener);
		domTree.addMouseListener(domTreeMouseListener);
		domTree.setCellEditor(domTreeCellEditor);
		domTreeCellEditor.addCellEditorListener(cellEditorListener);
		domTree.addTreeSelectionListener(treeSelectionListener);

	}

	private void createTreeModel() {
		/* Create tree model and add model listener to this model */
		treeModel = new DefaultTreeModel(null);
		treeModel.addTreeModelListener(domTreeModelListener);
	}

	private void createWindow() {
		int startPositionX = GUIUtil.getStartPositionX(windowWidth);
		int startPositionY = GUIUtil.getStartPositionY(windowHeight);

		setTitle(GUIConstants.APPLICATION_NAME);
		setBounds(startPositionX, startPositionY, windowWidth, windowHeight);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		/*
		 * Create scroll panes for tree and table and add these to panes to a
		 * split pane
		 */
		JScrollPane scrollPaneDomTree = new JScrollPane(domTree);
		JScrollPane scrollPaneAttributeTable = new JScrollPane(attrTable);
		JSplitPane policyEditor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollPaneDomTree, scrollPaneAttributeTable);
		policyEditor.setDividerLocation(getWindowWidth() / 3);
		policyEditor.setOneTouchExpandable(true);
		getContentPane().add(policyEditor, BorderLayout.CENTER);
	}

	private void deleteAttribute() {
		attrTable.deleteAttribute();
	}

	private void deleteNode() {
		domTree.deleteNode();
	}

	private void displayAttributes(Node aNode) {
		attrTable.displayAttributes(aNode);
	}

	private void editAttribute() {
		attrTable.editAttribute();
	}

	private void editNode() {
		if (domTree.getSelectedTreeNode().getParent() == null){
			editRoot();
		} else {
	    	domTree.editNode();
		}
	}

	private void enableAttrButtons() {
		boolean b1 = domTree == null ? false
				: domTree.getSelectedNode() instanceof Element;
		boolean b2 = attrTable.getSelectedRowCount() == 1;
		btAddAttribute.setEnabled(b1);
		btEditAttribute.setEnabled(b2);
		btDeleteAttribute.setEnabled(b2);

		addAttributeMenuItem.setEnabled(b1);
		editAttributeMenuItem.setEnabled(b2);
		deleteAttributeMenuItem.setEnabled(b2);
	}

	private void enableComment() {
		Node node = domTree == null ? null : domTree.getSelectedNode();

		/* Allow policy and assertions to have comments. */
		boolean b1 = node instanceof Element
				&& (node.getPrefix() == null || (node.getPrefix() != null && 
			    (!node.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX)
				|| node.getNodeName().equals(PolicyConstants.QNAME_POLICY))));

		btAddComment.setEnabled(b1);

		addCommentMenuItem.setEnabled(b1);
	}

	private void enableExpandCollapse() {
		boolean b1 = document != null
				&& DocumentUtil.getDocumentRoot(document).hasChildNodes();
		boolean b2 = domTree.getSelectedNode() instanceof Element
				&& domTree.getSelectedNode().hasChildNodes();
		boolean b3 = domTree.isExpanded(domTree.getSelectionPath());

		btExpandAll.setEnabled(b1);
		btExpand.setEnabled(b2 && !b3);
		btCollapseAll.setEnabled(b1);
		btCollapse.setEnabled(b2 && b3);

		expandAllMenuItem.setEnabled(b1);
		expandMenuItem.setEnabled(b2 && !b3);
		collapseAllMenuItem.setEnabled(b1);
		collapseMenuItem.setEnabled(b2 && !b3);
	}

	private void enableNodeButtons() {
		Node node = domTree.getSelectedNode();

		boolean b1;
		if (!(node instanceof Element)
				|| node.getNodeName().equals(
						PolicyConstants.QNAME_POLICY_REFERENCE)
				|| document == null) {
			b1 = false;
		} else {
			b1 = true;
		}

		boolean b2 = (node instanceof Element) || (document == null);

		boolean b3;
		if (node == null
				|| (node instanceof Element && node.getPrefix() != null && node
					.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX))){
			b3 = false;
		} else {
			b3 = true;
		}

		boolean b4 = node != null;

		btPolicy.setEnabled(b1);
		btExactlyOne.setEnabled(b1);
		btAll.setEnabled(b1);

		btAddElement.setEnabled(b2);
		btEditElement.setEnabled(b3);
		btDeleteElement.setEnabled(b4);

		addPolicyMenuItem.setEnabled(b1);
		addExactlyoneMenuItem.setEnabled(b1);
		addAllMenuItem.setEnabled(b1);

		addAssertionMenuItem.setEnabled(b2);
		editAssertionMenuItem.setEnabled(b3);
		deleteElementMenuItem.setEnabled(b4);
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
					.setToolTipText("Set the Selected Assertion Non-optional");
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
					.setToolTipText("Set the Selected Assertion"
							+ " Non-ignorable");
			} else {
				btSetIgnorable.setIcon(Images.ICON_ADD_IGNORABLE);
				btSetIgnorable
					.setToolTipText("Set the Selected Assertion Ignorable");
			}
		}

		btSetIgnorable.setEnabled(b1);
	}

	private void enableSaveButtons() {
		boolean b1 = document != null;

		btSave.setEnabled(currentFileChanged);
		btSaveAs.setEnabled(b1);

		saveMenuItem.setEnabled(currentFileChanged);
		saveAsMenuItem.setEnabled(b1);
	}

	private void enableSourceViewer() {
		boolean b1 = document != null && document.getDocumentElement() != null
				&& !isCurrentFileChanged();

		btViewSource.setEnabled(b1);

		viewSourceMenuItem.setEnabled(b1);
	}

	private void enableTextButton() {
		Node node = domTree.getSelectedNode();

		boolean b1 = (node instanceof Element && !(node.getPrefix() != null 
				&& node.getPrefix().equals(PolicyConstants.WS_POLICY_PREFIX)));

		btAddText.setEnabled(b1);

		addTextValueMenuItem.setEnabled(b1);
	}

	/**
	 * Gib das zu dieser Datei gehoerten Dokument zurueck.
	 * 
	 */
	public Document getDocument() {
		return document;
	}

	private String getDocumentName() {
		return currentFile == null ? "Untitled" : currentFile.getName();
	}

	private int getWindowHeight() {
		return windowHeight;
	}

	private int getWindowWidth() {
		return windowWidth;
	}

	private boolean isCurrentFileChanged() {
		return currentFileChanged;
	}

	private void openDocument(File aFile) {
		isNewFile = false;

		if (aFile == null) {
			MOptionPanes.showError(owner, Messages.NO_FILE_FOUND);
		}

		if (!aFile.isFile())
			return;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			document = DocumentFactory.parseXML(aFile);
			if (document != null) {
				root = DocumentUtil.getDocumentRoot(document);
				DefaultMutableTreeNode top = domTree
						.createTreeNode(new DomTreeNode(root));
				treeModel.setRoot(top);
			}

			domTree.treeDidChange();
			
			domTree.setSelectionRow(0);

			displayAttributes(root);

			currentFile = aFile;

			setAppTitle(getDocumentName());

			setCurrentFileChanged(false);

			domTree.expandTree();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			MOptionPanes.showError(owner, "Error by opening the file.");
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

	}

	private boolean promptToSave() {
		if (!isCurrentFileChanged() && !isNewFile)
			return true;

		if (document == null) {
			return checkDocument();
		}

		int result = JOptionPane.showConfirmDialog(owner, "Save changes to \""
				+ getDocumentName() + "\"?", APP_NAME,	
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		switch (result) {
		case JOptionPane.YES_OPTION:
			if (!saveFile(false))
				return false;
			return true;
		case JOptionPane.NO_OPTION:
			if (isNewFile) {
				assertionExplorer.removeEmptyFile();
			}
			return true;
		case JOptionPane.CANCEL_OPTION:
			return false;
		}

		return true;
	}

	private boolean saveFile(boolean saveAs) {
		if (document == null) {
			if (checkDocument()) {
				dispose();
			}
			return false;
		}

		if (saveAs || currentFile == null) {
			if (fileChooser.showSaveDialog(AssertionEditor.this) != 
				JFileChooser.APPROVE_OPTION) {
				return false;
			}
			File fileToSave = fileChooser.getSelectedFile();
			if (fileToSave == null) {
				return false;
			}
			currentFile = fileToSave;
			setTitle(APP_NAME + " [" + getDocumentName() + "]");
		}

		isNewFile = false;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			DocumentFactory.write(document, currentFile);

		} catch (Exception e) {
			e.printStackTrace();
			MOptionPanes.showError(owner, "Error by saving the file.");
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		setCurrentFileChanged(false);
		isNewFile = false;
		
		enableSourceViewer();
		
		return true;
	}

	private void setAppTitle(String docName) {
		setTitle(APP_NAME + " [" + docName + "]");
	}

	private void setAssertionOptional(boolean isOptional) {
		if (isOptional) {
			attrTable.addAttributeOptional();
			enableOptional();
			return;
		}

		attrTable.removeAttributeOptional();
		enableOptional();
	}

	private void setAssertionIgnorable(boolean isIgnorable) {
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
		enableComment();
		enableExpandCollapse();
		enableNodeButtons();
		enableOptional();
		enableIgnorable();
		enableSaveButtons();
		enableSourceViewer();
		enableTextButton();
	}

	private void setCurrentFileChanged(boolean changed) {
		currentFileChanged = changed;
		enableSaveButtons();
		domTree.requestFocus();
	}

	private void setWindowClosingOperation() {
		/* Specify how shoule the window react to the closing operation */
		WindowListener lsClosingWindow = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!promptToSave()) {
					return;
				}
				dispose();
			}
		};
		addWindowListener(lsClosingWindow);
	}

	private void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	private void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	private void showSourceViewer() {
		if (currentFile == null) {
			return;
		}

		JDialog.setDefaultLookAndFeelDecorated(true);
		SourceViewer viewer = new SourceViewer(this, currentFile);

		viewer.setVisible(true);
	}
	
	private void editRoot (){
			
		Element newElement;

		DomTreeNode treeNode = domTree.getSelectedTreeNode();

		Node editingNode = treeNode.getDOMNode();

		/* Get the old attribute name */
		String oldNSName;
		if (editingNode.getPrefix() == null) {
			oldNSName = PolicyConstants.XMLNS_PREFIX;
		} else {
			oldNSName = PolicyConstants.XMLNS_PREFIX + ":"
					+ editingNode.getPrefix();
		}

	    InputDialogPattern editNodeDialog = new InputDialogPattern(editingNode
	    		.getLocalName(),editingNode.getPrefix(), editingNode
	    		.getNamespaceURI(), false);
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

		String newNSAttributeName = PolicyConstants.XMLNS_PREFIX + ":"
		           + prefix;

		Document doc = document;

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

		((DefaultTreeModel) domTree.getModel()).nodeStructureChanged(treeNode);

		domTree.expandNode(treeNode);
		
		domTree.setSelectionRow(0);
		
		setCurrentFileChanged(true);
	}
	
	
	private MouseListener domTreeMouseListener = new MouseListener() {

		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2){
				TreePath path = domTree.getPathForLocation(e.getX(), e.getY());

				if (path == null) {
					return;
				}

				domTree.setSelectionPath(path);

				Node node = domTree.getSelectedNode();

				if (node == null
						|| (node instanceof Element && (node.getPrefix() ==
							null || node.getPrefix().equals(
										PolicyConstants.WS_POLICY_PREFIX)))) {
					MOptionPanes.showError(AssertionEditor.this,
							"You cannot edit a policy operator.");
					return;
				}

				editNode();
			}
			return;
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	};
	
	private KeyListener escKeyListener = new KeyListener(){

		public void keyPressed(KeyEvent e) {
			
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
				if (!promptToSave()) {
					return;
				}
				dispose();
			}
			
		}

		public void keyReleased(KeyEvent e) {			
		}

		public void keyTyped(KeyEvent e) {	
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
					MOptionPanes.showError(AssertionEditor.this,
						"Sorry, you cannot change this Node.");
			        return; 
				}
				
				if (domTree.getSelectedTreeNode().getParent() == null){
					editRoot();
					return;
				}
			     
				if (domTree.getSelectedNode().getNodeType() == 
					    Node.ELEMENT_NODE){
					domTree.editElement();
				} else {
					editingDomNode = domTree.getSelectedNode();
				    domTreeCellEditor.setEditImmediately(true);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				if (e.getModifiers() == KeyEvent.CTRL_MASK) {
					saveFile(false);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				if (!promptToSave()) {
					return;
				}
				dispose();
			}

			
		}

		public void keyReleased(KeyEvent arg0) {	
		}

		public void keyTyped(KeyEvent arg0) {	
		}
		
	};
	
	private TreeModelListener domTreeModelListener = new TreeModelListener(){
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

			treeModel.nodeStructureChanged(parentTreeNode);
			domTree.expandNode(parentTreeNode);

			if (path != null) {
				path = path.pathByAddingChild(newXMLNode);
				domTree.setSelectionPath(path);
				domTree.scrollPathToVisible(path);
			}
			setCurrentFileChanged(true);
		}

		public void treeNodesInserted(TreeModelEvent e) {
			setCurrentFileChanged(true);
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			setCurrentFileChanged(true);
		}

		public void treeStructureChanged(TreeModelEvent e) {
			setCurrentFileChanged(true);
		}
	
	};
	
	private CellEditorListener cellEditorListener = new CellEditorListener(){

		public void editingCanceled(ChangeEvent arg0) {
			domTreeCellEditor.setEditImmediately(false);		
		}

		public void editingStopped(ChangeEvent arg0) {
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
						MOptionPanes.showError(AssertionEditor.this,
								"Sorry, you cannot change the attribute" +
								" name \"URI\".");
			        	return;   
					}
					
					String oldName = attrTable.getValueAt(row, column)
					    .toString();

					/* Does not allow to change WS-Policy namespace */
					if (oldName.equals(PolicyConstants.WS_POLICY_NS_NAME)) {
						MOptionPanes.showError(AssertionEditor.this,
								Messages.NO_CHANGE_ON_POLICY_NS);
						return;
					}
					
				} else {
					
					/* Editing column is column "Value" */
					String oldValue = attrTable.getValueAt(row, column)
					    .toString();

					/* Does not allow to change WS-Policy namespace */
					if (oldValue.equals(PolicyConstants
							.WS_POLICY_NAMESPACE_URI)) {
						MOptionPanes.showError(AssertionEditor.this,
								Messages.NO_CHANGE_ON_POLICY_NS);
						return;
					}
		        }
		        tableModel.setCellEditable (row, column);
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
				if (!promptToSave()) {
					return;
				}
				dispose();
		    } else if (e.getKeyCode() == KeyEvent.VK_DELETE){
				deleteAttribute();
			} else if (e.getKeyCode() == KeyEvent.VK_INSERT){
				addAttribute();
			} else  if (e.getKeyCode() == KeyEvent.VK_S && e.getModifiers()
					== KeyEvent.CTRL_MASK){
				saveFile(false);
			}
		}

		public void keyReleased(KeyEvent arg0) {	
		}

		public void keyTyped(KeyEvent arg0) {	
		}
		
	};

	private TableModelListener tableModelListener = new TableModelListener(){

		public void tableChanged(TableModelEvent e) {
			setCurrentFileChanged(true);
		}
	};
	
	private TreeSelectionListener treeSelectionListener = new
	    TreeSelectionListener(){
		
		public void valueChanged(TreeSelectionEvent e) {
			attrTable.displayAttributes(domTree.getSelectedNode());
			editingDomNode = domTree.getSelectedNode();
			setButtons();
		}

	};
	
}