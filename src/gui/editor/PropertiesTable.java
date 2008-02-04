package gui.editor;

import gui.explorer.FileNode;

import java.awt.Frame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.w3c.policy.util.NSRegistry;
import org.w3c.policy.util.PolicyConstants;

import util.DocumentUtil;
import util.MOptionPanes;

/**
 *  Mit dieser Klasse werden die Properties von der selektierten Assertion in 
 *  AssertionWorkspace durch eine Tabellensicht angezeigt. Diese Tabelle besteht 
 *  aus zwei Spalten. Eine davon ist fuer die Namen von Property und die 
 *  andere ist fuer die Werte von Property. Wird ein Assetion selektiert, 
 *  sind die folgende fuenf Properties anzuzeigen: "Assertion Name", 
 *  "Prefix", "NamespaceURI", "Description" und "Path". Selektiert ein Paket, 
 *  wird "default Namespace" angezeigt.
 * 
 * @author      Windy
 * @version     1.0
 * @see         AttrTable
 */
public class PropertiesTable extends JTable{
	
	private static final long serialVersionUID = 2453962731061721114L;
	
	private static PropertiesTableModel tableModel = new PropertiesTableModel();

	public PropertiesTable() {
		super(tableModel);
		setCellSelectionEnabled(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * Sobald die selektierte Assertion bzw. das selektierte Paket gewechselt
	 * wird, wird diese Methode aufgerufen, mit der die Tabelle aktualisiert.
	 * 
	 * @param aNode   der neue selektierte Knoten, dessen Properties angezeigt
	 *                werden soll.
	 */
	public void displayProperties(FileNode aNode) {
		final FileNode node = aNode;
	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {			
				tableModel.setSourceNode(node);	
				tableChanged(new TableModelEvent(tableModel));
			}
		});
	}
	
	/**
	 * Setzt eine Zelle in Tabelle modifizierbar.
	 * 
	 * @param rowIndex      der Zeilenindex dieser Zelle
	 * @param columnIndex   der Spaltindex dieser Zelle
	 */
	public void setCellEditable(int rowIndex, int columnIndex){
		tableModel.setCellEditable(rowIndex, columnIndex);
	}

}

class PropertiesTableModel extends AbstractTableModel{
	
	private static final long serialVersionUID = -8933868064168789352L;
	
	private String[] columnNames =  {"Property", "Value"};
	
	private FileNode node;
	
	private File file;
	
	private Document document;
	
	private Element root;
	
	private int selectedRowIndex; 
	
	private int selectedColumnIndex;
	
	public PropertiesTableModel() {
		super();	
	}
	 
	public int getColumnCount() {
        return columnNames.length;
	}
	 
	public int getRowCount() {
		if (file == null){
			return 0;
		} else if (file.isDirectory()){
			return 1;
		} else {
			return 5;
		}
	}
	    
	public FileNode getSourceNode() {
		return node;
	}
	
	public String getColumnName(int columnIndex) {
	    return columnNames[columnIndex];
	}
	
	public void setSourceNode(FileNode aNode){

		if (aNode == null) {
			node = null;
			file = null;
			document = null;
			root = null;
		} else { 	
			node = aNode;
			file = node.getFile();
			if (file.isDirectory() || (file == null)) {
				document = null;
				root = null;
			} else {
			    document = DocumentFactory.parseXML(file);
			    root = DocumentUtil.getDocumentRoot(document);
			}
		}
		selectedRowIndex = -1;
		selectedColumnIndex = -1;
	}
	
	private String getDefaultNamespace(){
		try {
	        File defaultNSFile = new File(file.getAbsolutePath(),".DefaultNS");
	        if (defaultNSFile.exists()){
	    	    BufferedReader reader = new BufferedReader(new FileReader
	    	    		(defaultNSFile));
	        	String defaultPrefix = reader.readLine();
	        	String defaultNsURI = reader.readLine();
	        	reader.close();
	        	return defaultPrefix + " : " + defaultNsURI;
	        }
		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by reading defaultNS.");
		}
		return "";
	}
	
	private String getAssertionName(){
		
		if (root != null){
			String assertionName = root.getLocalName();
			if (assertionName == null){
				assertionName = "";
			}
			return assertionName;				
		}
	    return "";
		
	}
	
	private String getPrefix(){

		if (root != null){
			String prefix = root.getPrefix();
			if (prefix  == null){
				prefix  = "";
			}
			return prefix ;				
		}
	    return "";
		
	}
	private String getNamespaceURI(){
		if (root != null){
			String nsURI = root.getNamespaceURI();
			if (nsURI == null){
				nsURI = "";
			}
			return nsURI;				
		}
	    return "";
	}

	private String getDescription(){
		if (root != null) {
			String description = root.getAttribute(PolicyConstants.WSPE_PREFIX 
					+ ":" + "Description");
			if (description == null){
				return "";
			} else {
				return description;
			}
		}
		return "";
	}
	

	public Object getValueAt(int rowIndex, int columnIndex) {

		switch (columnIndex) {
		case 0:// Name_COLUMN
			if (file == null){
				return "";
			} else if (file.isDirectory()){
				return "default namespace";
			} else {
		        switch (rowIndex) {
		        case 0:
		         	return "Assertion Name";
		        case 1:
		        	return "Prefix";
		        case 2:
		         	return "NamespaceURI";
		        case 3:
		        	return "Description";
		        case 4:
		        	return "Path";
		        }
		    }
		case 1:// VALUE_COLUMN
			if (file == null){
				return "";
			} else if (file.isDirectory()){
				return getDefaultNamespace();
			} else {
		        switch (rowIndex) {
		        case 0:
		         	return getAssertionName();
		        case 1:
		        	return getPrefix();
		        case 2:
		         	return getNamespaceURI();
		        case 3:
		        	return getDescription();
		        case 4:
		        	return file.getAbsolutePath();
		        }
			}
		}
		return "";
	 }
	
	private void editRoot (String localName, String prefix, String nsURI){
		String qName = prefix + ":" + localName;
	    Element  newRoot = document.createElementNS(nsURI, qName);
	    
	    Attr attr;
	    /* Import all the attributes */
	    NamedNodeMap attrs = root.getAttributes();
	    if (attrs.getLength() != 0) {
		    for (int i = 0; i < attrs.getLength(); i++) {
				attr = (Attr) attrs.item(i);
				Attr newAttr = (Attr) document.importNode(attr, true);
				newRoot.getAttributes().setNamedItem(newAttr);
		    }
	    }
	    
		/* Add the children of the editing node to the new node */
		while (root.hasChildNodes()) {
			newRoot.appendChild(root.getFirstChild());
		}
		
		document.replaceChild(newRoot, root);
		
		try {
			DocumentFactory.write(document, file);
		} catch (Exception e) {
			e.printStackTrace();
			MOptionPanes.showError(null, "Error by saving the file.");
		}
		
		root = newRoot;
		
	}
	 
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	
		setCellEditable(-1, -1);
		
		if (aValue.toString().equals(getValueAt(rowIndex, columnIndex)
				.toString())) {
			return;
		}	
		
		if ((rowIndex == 0) && (columnIndex == 1)){
			if (root != null) {
				String localName = aValue.toString();
				editRoot (localName, root.getPrefix(), root.getNamespaceURI());
			}
			return;
		}
		
		if ((rowIndex == 1) && (columnIndex == 1)){
			if (root != null) {
				String oldPrefix = root.getPrefix();
				String prefix = aValue.toString();
				root.setPrefix(prefix);
				root.removeAttribute("xmlns:" + oldPrefix);
				try {
					DocumentFactory.write(document, file);
				} catch (Exception e) {
					e.printStackTrace();
					MOptionPanes.showError(null, "Error by saving the file.");
				}
				
		    	NSRegistry.check(prefix, root.getNamespaceURI());
			}
			return;
		}
		
		if ((rowIndex == 2) && (columnIndex == 1)){
			if (root != null) {
				String nsURI = aValue.toString();
				editRoot (root.getLocalName(), root.getPrefix(), nsURI);
		    	NSRegistry.check(root.getPrefix(), nsURI);
			}
			return;
		}
		
		if ((rowIndex == 3) && (columnIndex == 1)){
			if (root != null) {
				String description = aValue.toString();
				root.setAttribute(PolicyConstants.WSPE_PREFIX + ":" + 
						"Description", description);
				try {
					DocumentFactory.write(document, file);
				} catch (Exception e) {
					e.printStackTrace();
					MOptionPanes.showError(null, "Error by saving the file.");
				}
			}
			return;
		}
		
		return ;
	}
	
	public void setCellEditable (int rowIndex, int columnIndex){
		selectedRowIndex = rowIndex;
		selectedColumnIndex = columnIndex;
	}
	
	 public boolean isCellEditable(int rowIndex, int columnIndex) {
			if ((rowIndex == selectedRowIndex) && (columnIndex == 
				selectedColumnIndex)){
				return true;
			} else {
				return false;
			}
	}
}


