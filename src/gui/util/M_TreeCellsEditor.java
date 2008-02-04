package gui.util;

import gui.editor.DomTree;
import gui.explorer.FileTree;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
 
/**
 * Verwaltet alle TreeCellEditor
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         M_TreeCellsRenderer
 */
public class M_TreeCellsEditor extends DefaultTreeCellEditor {
	
    private boolean editable = false;
    
    /**
     * Legt ein neues TreeCellsEditor fuer einen DomTree an
     * 
     * @param tree       der DomTree
     * @param renderer   das gegebene TreeCellsRenderer
     */
	public M_TreeCellsEditor(DomTree tree, M_TreeCellsRenderer renderer) {
		super(tree, renderer);
	} 

    /**
     * Legt ein neues TreeCellsEditor fuer einen FileTree an
     * 
     * @param tree       der FileTree
     * @param renderer   das gegebene TreeCellsRenderer
     */
	public M_TreeCellsEditor(FileTree tree, M_TreeCellsRenderer renderer) {
		super(tree, renderer);
	} 

	/**
	 * Legt einen neuen TreeCellRenderer anhand der gegebenen Parameter an.
	 */
	@Override
	public Component getTreeCellEditorComponent(JTree aTree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
		
		String newValue;
		if (value.toString().endsWith(".asrt")){
			newValue = value.toString().replace(".asrt", "");
		} else if (value.toString().endsWith(".xml")){
			newValue = value.toString().replace(".xml", "");
		} else {
			newValue = value.toString();
		}
		
		return super.getTreeCellEditorComponent(aTree, newValue , isSelected,
				expanded, leaf, row);
	}

	/**
	 * ob eine Uhr, die die Laenge der Modifizierung misst, gestartet werden
	 * soll
	 * 
	 * @return   false
	 */
	@Override
	protected boolean shouldStartEditingTimer(EventObject event) {
		return false;
	}

	/**
	 * tut nichts hier.
	 */
	@Override
	protected void startEditingTimer() {
	}

	/**
	 * Stellt die TreeCell modifizierbar bzw. nicht modifizierbar
	 */
	public void setEditImmediately (boolean b1){
		editable = b1;
	}
	
	/**
	 * Ob die TreeCell modifizierbar ist
	 */
	@Override
	protected boolean canEditImmediately(EventObject event) {
		if (editable){
		    return true;
		} else {
			return false;
		}
	}

}
