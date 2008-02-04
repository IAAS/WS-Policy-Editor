package gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.w3c.policy.util.NSRegistry;
import org.w3c.policy.util.PolicyConstants;

import util.MOptionPanes;
import util.XMLUtil;

/**
 * Diese Klasse bietet dem Benuzer die Moeglichkeit an, beliebig viele
 * Namespace in System zu speichern und spaeter zu benutzen.
 * 
 * @author      Windy
 * @version     1.0
 */
public class NamespaceList extends JDialog{
	
	private static final long serialVersionUID = 7925216393656360059L;
	
	private JButton btOk;
	
	private JButton btCancel;
	
	private JButton btNew;
	
	private JButton btEdit;
	
	private JButton btDelete;
	
	private NamespaceTable namespaceTable;
	
	private String selectedPrefix;
	
	private String selectedNsURI;
	
	private boolean namespaceListChanged = false;
	
	private String[][] oldRegistry;
	
	private int oldCounter;


	/**
	 * Create the frame
	 */
	public NamespaceList(){
		super();
		setTitle("Namespace List");
		setModal(true);
		setDefaultLookAndFeelDecorated(true);
		getContentPane().setLayout(null);
		setBounds(100, 100, 435, 347);
		
		oldRegistry = NSRegistry.getNSRegistry();
		oldCounter = NSRegistry.countNamespace();
		
		createComponent();
		
		addListener();
		
		namespaceTable.changeSelection(0, 0, false, false);
		refreshButtons();
		namespaceTable.changeSelection(0, 0, false, false);
	}

	private void createComponent(){
		btNew = new JButton();
		btNew.setText("New");
		btNew.setBounds(10, 10, 93, 23);
		btNew.addActionListener(actionListener);
		getContentPane().add(btNew);

		btEdit = new JButton();
		btEdit.setText("Edit");
		btEdit.setBounds(121, 10, 93, 23);
		btEdit.setEnabled(false);
		btEdit.addActionListener(actionListener);
		getContentPane().add(btEdit);

		btDelete = new JButton();
		btDelete.setText("Delete");
		btDelete.setBounds(232, 10, 93, 23);
		btDelete.setEnabled(false);
		btDelete.addActionListener(actionListener);
		getContentPane().add(btDelete);
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(9, 48, 395, 203);
		getContentPane().add(scrollPane);

		namespaceTable = new NamespaceTable();
		scrollPane.setViewportView(namespaceTable);
		
		btOk = new JButton("Ok");
		btOk.setBounds(241, 269, 73, 23);
		btOk.addActionListener(actionListener);
		getContentPane().add(btOk);
		
		btCancel = new JButton("Cancel");
		btCancel.setBounds(331, 269, 73, 23);
		btCancel.addActionListener(actionListener);
		getContentPane().add(btCancel);
		
	}

	private void addListener(){
		namespaceTable.addMouseListener(tableMouseListener);
		namespaceTable.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
					selectedPrefix = null;
		        	selectedNsURI = null;
		        	
		        	if (namespaceListChanged){
		        		if (JOptionPane.showConfirmDialog(null,
								"the namespace-list is changed.\n " +
								"Do you want to save new namespace-list ? ",
								"Comformation", JOptionPane.YES_NO_OPTION) 
								!= JOptionPane.YES_OPTION) {	

					        NSRegistry.refreshFile(oldRegistry, oldCounter);
		        		}
					}
			    	dispose();
				}
				
				if (e.getKeyCode() == KeyEvent.VK_ENTER){
					int rowIndex = namespaceTable.getSelectedRow();
					selectedPrefix = (String) namespaceTable.getValueAt
					    (rowIndex, 0);
					selectedNsURI = (String) namespaceTable.getValueAt
					    (rowIndex, 1);
			    	dispose();
				}
				
				if (e.getKeyCode() == KeyEvent.VK_DELETE){
					if (!btDelete.isEnabled()){
						MOptionPanes.showError(null, "You cannot delete this namespace.");
					} else {
					    delete();
					}
				}

			    if (e.getKeyCode() == KeyEvent.VK_INSERT) {
					add();
				}
				
			}
			
			@Override
			public void keyReleased(KeyEvent e){
				refreshButtons();
			}
			
		});
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				selectedPrefix = null;
	        	selectedNsURI = null;
	        	
	        	if (namespaceListChanged){
	        		if (JOptionPane.showConfirmDialog(null,
							"the namespace-list is changed.\n " +
							"Do you want to save new namespace-list ? ",
							"Comformation", JOptionPane.YES_NO_OPTION) 
							!= JOptionPane.YES_OPTION) {	

				        NSRegistry.refreshFile(oldRegistry, oldCounter);
	        		}
				}
			}

		});
	}
	
	/**
	 * das vom Benutzer ausgewaehlte Praefix
	 */
	public String getSelectedPrefix (){
		return this.selectedPrefix;
	}
	
	/**
	 * das vom Benutzer ausgewaehlte NamespaceURI
	 */
	public String getSelectedNsURI (){
		return this.selectedNsURI;
	}
	
	private void refreshButtons(){
		
		int rowIndex = namespaceTable.getSelectedRow();
		selectedPrefix = (String) namespaceTable.getValueAt(rowIndex, 0);
		selectedNsURI = (String) namespaceTable.getValueAt(rowIndex, 1);
		
		if (selectedPrefix.equals(PolicyConstants.WS_POLICY_PREFIX) 
				&& selectedNsURI.equals(PolicyConstants.WS_POLICY_NAMESPACE_URI)){
			btEdit.setEnabled(false);
			btDelete.setEnabled(false);
			return;
		}
		
		if (selectedPrefix.equals(PolicyConstants.WSPE_PREFIX) 
				&& selectedNsURI.equals(PolicyConstants.WSPE_NAMESPACE_URI)){
			btEdit.setEnabled(false);
			btDelete.setEnabled(false);
			return;
		}
		
		if (selectedPrefix.equals(PolicyConstants.XML_PREFIX) 
				&& selectedNsURI.equals(PolicyConstants.XML_NAMESPACE_URI)){
			btEdit.setEnabled(false);
			btDelete.setEnabled(false);
			return;
		}
		
		if (selectedPrefix.equals(PolicyConstants.XMLNS_PREFIX) 
				&& selectedNsURI.equals(PolicyConstants.XMLNS_NAMESPACE_URI)){
			btEdit.setEnabled(false);
			btDelete.setEnabled(false);
			return;
		}
		
		btEdit.setEnabled(true);
		btDelete.setEnabled(true);
		
		namespaceTable.setFocusable(true);
		
	}
	
	
	private void add(){
    	String newPrefix = MOptionPanes.getInput(this, "Please enter new prefix:", "");
    	
    	while (!(newPrefix == null) && (!XMLUtil.isLegalNCName(newPrefix))){
    		newPrefix = MOptionPanes.getReinput(this, "illegal prefix! " +
    				"Please enter again.");
    	}
    	
    	if (newPrefix == null) {
    		return;
    	}
    	
    	String newNamespace = MOptionPanes.getInput(this, "Please enter new" +
    			" namespace", "");
    	if (newNamespace == null){
    		return;
    	}
    	
    	namespaceListChanged = true;
    	namespaceTable.addNamespace(newPrefix, newNamespace);
    
    	refreshButtons();
		
	}
	
	private void edit(){
		int rowIndex = namespaceTable.getSelectedRow();
    	
    	String newPrefix = MOptionPanes.getInput(this, "Please enter new " +
    			"prefix:", selectedPrefix);
    	
    	if (newPrefix == null) {
    		return;
    	}
    	
    	String newNamespace = MOptionPanes.getInput(this, "Please enter new" +
    			" namespace", selectedNsURI);
    	
    	if (newNamespace == null){
    		return;
    	}
    	
    	namespaceListChanged = true;
    	namespaceTable.editNamespace(rowIndex, newPrefix, newNamespace);

    	refreshButtons();
	}
	
	private void delete(){
		int rowIndex = namespaceTable.getSelectedRow();
    
    	int ComfirmResult;
    	ComfirmResult = MOptionPanes.getComfirmResult(this,
    			"Are you sure you want to delete " + selectedPrefix + " : " + 
    			selectedNsURI + " ?");
    	if (ComfirmResult == JOptionPane.NO_OPTION) {
    		return;
    	}
    	
    	namespaceListChanged = true;
    	namespaceTable.deleteNamespace(rowIndex);
    	
    	refreshButtons();
    	
	}
	
	private ActionListener actionListener = new ActionListener(){
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == btOk) {
			    dispose();
			    
		    } else if (e.getSource() == btCancel){
				selectedPrefix = null;
	        	selectedNsURI = null;
	        	
	        	if (namespaceListChanged){
	        		if (JOptionPane.showConfirmDialog(null,
							"the namespace-list is changed.\n " +
							"Do you want to save new namespace-list ? ",
							"Comformation", JOptionPane.YES_NO_OPTION) 
							!= JOptionPane.YES_OPTION) {	

				        NSRegistry.refreshFile(oldRegistry, oldCounter);
	        		}
				}
			    dispose();
			    
		    } else if (e.getSource() == btNew) {
		    	add();
		    } else if (e.getSource() == btEdit) {
		        edit();  	
		    } else if (e.getSource() == btDelete) {
		    	delete();
		    }	
		}
	};

	private MouseListener tableMouseListener = new MouseListener(){
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) 
					&& e.getClickCount() == 2) {
				int rowIndex = namespaceTable.getSelectedRow();
				if (rowIndex == -1){
					return;
				}
				selectedPrefix = (String) namespaceTable.getValueAt
				    (rowIndex, 0);
				selectedNsURI = (String) namespaceTable.getValueAt
				    (rowIndex, 1);
			    dispose();
			}
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
			refreshButtons();
		}
	};
		

	
}

class NamespaceTable extends JTable {
	
	
	private static final long serialVersionUID = 6693592069632107373L;
	
	private static NamespaceTableModel tableModel = new NamespaceTableModel();
	
	public NamespaceTable() {
	    super(tableModel);
	    setCellSelectionEnabled(true);
	    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    setRowSelectionAllowed(true);
	    setColumnSelectionAllowed(false);
    }
	
	public void addNamespace(String newPrefix, String newNamespace){
		NSRegistry.add(newPrefix, newNamespace);
		final int rowIndex = NSRegistry.getIndexOf(newPrefix, newNamespace);
		tableModel.fireTableStructureChanged();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableChanged(new TableModelEvent(tableModel));
				changeSelection(rowIndex, rowIndex, false, false);
			}
		});
		
	}
		
	public void editNamespace(int rowIndex, String newPrefix, 
			String newNamespace){
		NSRegistry.edit(rowIndex, newPrefix, newNamespace);
		tableModel.fireTableStructureChanged();	
		final int newIndex = NSRegistry.getIndexOf(newPrefix, newNamespace);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableChanged(new TableModelEvent(tableModel));
				changeSelection(newIndex, newIndex, false, false);
			}
		});
	}
	
	public void deleteNamespace(int rowIndex){
		NSRegistry.delete(rowIndex);
		tableModel.fireTableStructureChanged();	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableChanged(new TableModelEvent(tableModel));
				changeSelection(0, 0, false, false);
			}
		});		
	}

}

class NamespaceTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 8170746438853682154L;
	
	private String[] columnNames =  {"Prefix", "Namespace"};
	
	public NamespaceTableModel() {
		super();
	}
	
	public int getColumnCount() {
        return columnNames.length;
	}
	 
	public int getRowCount() {
		return NSRegistry.countNamespace();
	}
	
	public String getColumnName(int columnIndex) {
	    return columnNames[columnIndex];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= getRowCount() || 
		    columnIndex < 0 || columnIndex >= getColumnCount()) {
		    return "";
		} else {
		    return NSRegistry.getNSRegistry()[rowIndex][columnIndex];
		}
	}
}