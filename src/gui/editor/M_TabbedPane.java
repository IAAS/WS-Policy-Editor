package gui.editor;

import gui.editor.PolicyEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.Icon;

import org.w3c.policy.Policy;
import org.w3c.policy.util.PolicyReader;
import org.w3c.policy.util.PolicyUtil;
import org.w3c.policy.util.PolicyWriter;


/**
 * Bietet die Klasse and Schnittstelle mit denen mehrere DocumentPane 
 * verwaltet werden koennen
 * Die Funktionen von rechten MauseTaste auf tabbedPane 
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class M_TabbedPane extends JTabbedPane 
		implements MouseListener, KeyListener {

	private static final long serialVersionUID = 2792752141929399005L;
	
	private PolicyEditor owner;
	
	private JMenuItem policyMenuItem;
	
	/**
	 * Klassenkonstruktor.
	 * 
	 * Legt einen leeren Objekt an.
	 */
	public M_TabbedPane(PolicyEditor owner) {
		super();
		this.owner = owner;
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		addMouseListener(this);  
		addKeyListener(this);
	}

	/**
	 * Legt einen neuen Tab mit bestimmten Titlel Ikon und Tipp ein
	 * 
	 * @param title       der in diesem Tab angezeigte Titel
	 * @param extraIcon   das in diesem Tab angezeigte Ikon
	 * @param component   die Komponente, auf der dieser Tab angezeigt wird.
	 * @param tip         der fuer diesen Tab angezeigte Tipp
	 */
	public void addTab(String title, Icon extraIcon, 
			Component component, String tip) {
	    super.addTab(title, new CloseTabIcon(extraIcon), component, tip);  
		setSelectedComponent(component);
	}
	
	/**
	 * Schliess alle Tabs
	 * 
	 * @see   javax.swing.JTabbedPane#removeAll()
	 */
	public void closeAllTabs() {
		removeAll();
	}
	
	/**
	 * Schliess den selektierten Tab 
	 *
	 * @see   javax.swing.JTabbedPane#remove(Component)
	 */
	public void closeTab() {
		remove(getSelectedDocumentPane());
	}

	/**
	 * Gibt den DocumentPane mit dem gegebenen Index zurueck
	 * 
	 * @param i   der Index
	 * @return    der entsprechende DocumentPane
	 * @see       javax.swing.JTabbedPane#getComponentAt(int)
	 */
	public DocumentPane getDocumentPaneAt(int i) {
		return (DocumentPane)super.getComponentAt(i);
	}
	
	/**
	 * der aktuell selektierte DocumentPane
	 * 
	 * @see   javax.swing.JTabbedPane#getSelectedComponent()
	 */
	public DocumentPane getSelectedDocumentPane() {
		return (DocumentPane) super.getSelectedComponent();
	}   
	
	/*
	 * If the file is already opened in a tab, then select this tab and return
	 * true
	 */
	public boolean isFileOpened(File file) {
		int count = getTabCount();

		for (int i = 0; i < count; i++) {
			if (getToolTipTextAt(i).equals(file.getAbsolutePath())) {
				setSelectedIndex(i);
				return true;
			}
		}		
		return false;
	}	

	private void rightMouse(int count, MouseEvent e) {
		
		final JPopupMenu popupMenu = new JPopupMenu();   
		final JMenuItem closeTabMenuItem = 
				new JMenuItem("Close This Tab                       Ctrl+W");   
		final JMenuItem closeAllTabsMenuItem = 
				new JMenuItem("Close All Tabs             Ctrl+Shift+W");   
		final JMenuItem closeOtherTabsMenuItem = 
				new JMenuItem("Close Other Tabs");     		
		final JMenuItem NormalizeMenuItem = 
				new JMenuItem("Normalize the Policy");    		
		final JMenu IntersectMenu =
				new JMenu("Intersect   the Policy  with");   		
		final JMenu MergeMenu =
				new JMenu("Merge        the Policy  with");
      	final int curIdx = getSelectedIndex();
      	
		popupMenu.add(closeTabMenuItem);   
    	popupMenu.add(closeAllTabsMenuItem);   
    	popupMenu.add(closeOtherTabsMenuItem); 
    	
    	popupMenu.addSeparator();
    	
    	popupMenu.add(NormalizeMenuItem);
    	popupMenu.add(IntersectMenu);
    	popupMenu.add(MergeMenu);
    	
    	closeTabMenuItem.addActionListener(new ActionListener() {   
    		public void actionPerformed(ActionEvent e) {   
    			owner.closeTab(); 	
    		}    		
    	});  

    	closeAllTabsMenuItem.addActionListener(new ActionListener() {   
    		public void actionPerformed(ActionEvent e) {  
    			if (!owner.promptToCloseAll())
    				return;
    			owner.closeAllTabs();	
    		}   
    	});  
    	
    	closeOtherTabsMenuItem.addActionListener(new ActionListener() {   
    		public void actionPerformed(ActionEvent e) {   
    			if (!owner.promptToCloseOthers())
    				return;
    			owner.closeOtherTabs();	
    		}   
    	});  
    	
    	NormalizeMenuItem.addActionListener(new ActionListener() {   
    		public void actionPerformed(ActionEvent e) {   
    			
    			//owner.normalize();
    			
    			File file = getDocumentPaneAt(curIdx).getFile();

    			if (file == null) {
    				return;
    			}

    			Policy policy = PolicyReader.parsePolicy(file);

    			Policy normalized = (Policy) policy.normalize();

    			try {
    				File tempFile = File.createTempFile(file.getName(), null);
    				PolicyWriter writer = new PolicyWriter(tempFile);
    				writer.writePolicy(normalized);
    				owner.openDocument(tempFile);

    				tempFile.deleteOnExit();
    			} catch (IOException event) {
    			}
    		}    		
    	});                                                                  
		
		for (int i = 0; i < count; i++) { 	    			
			if (i != curIdx) {    				
				final int Idx = i;
				policyMenuItem = new JMenuItem();
				policyMenuItem.setText(getTitleAt(i));
				IntersectMenu.add(policyMenuItem);
				
				policyMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Policy result = PolicyUtil.intersect(
									getDocumentPaneAt(Idx).getFile(), 
									getDocumentPaneAt(curIdx).getFile());    
    					try {
    						File tempFile = File.createTempFile("Intersection", null);
    						PolicyWriter writer = new PolicyWriter(tempFile);
    						writer.writePolicy(result);
    						owner.openDocument(tempFile);
    						
    						tempFile.deleteOnExit();
    					} catch (IOException event) {
    					}        					                 
    				} 
				});    				
			}
		}
		
		for (int i = 0; i < count; i++) { 				
			if (i != curIdx) {    				
				final int Idx = i;
				policyMenuItem = new JMenuItem();
				policyMenuItem.setText(getTitleAt(i));
				MergeMenu.add(policyMenuItem);
				
				policyMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Policy result = PolicyUtil.intersect(
									getDocumentPaneAt(Idx).getFile(), 
									getDocumentPaneAt(curIdx).getFile());    
    					try {
    						File tempFile = File.createTempFile("Merge", null);
    						PolicyWriter writer = new PolicyWriter(tempFile);
    						writer.writePolicy(result);
    						owner.openDocument(tempFile);

    						tempFile.deleteOnExit();
    					} catch (IOException event) {
    					}        					
    				} 
				});    				
			}
		}	
    	    	
		if  (count > 0) {  
			if ((count == 1)) { 					
				closeAllTabsMenuItem.setEnabled(false);   
				closeOtherTabsMenuItem.setEnabled(false);   
				IntersectMenu.setEnabled(false);   
				MergeMenu.setEnabled(false);   
			}  
			popupMenu.show(this, e.getX(), e.getY());   
		}
	}
	
    public void mouseClicked(MouseEvent e) {  
    	
    	int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY()); 
    	int count = getTabCount();
    	
    	if (e.getButton() != MouseEvent.BUTTON3)   {     		   
    		if (tabNumber < 0)      
    			return;
    		Rectangle rect = ((CloseTabIcon) getIconAt(tabNumber)).getBounds();   
    		if (rect.contains(e.getX(), e.getY())) {    
    			owner.closeTab();
    		} 
    	}
    	else {  
    		rightMouse(count, e);
		}   
    }
    
    public void mouseEntered(MouseEvent e) {
    } 

    public void mouseExited(MouseEvent e) {
   	} 

    public void mousePressed(MouseEvent e) {
    } 

    public void mouseReleased(MouseEvent e) {
    }    
    
    public void keyPressed(KeyEvent e) {
		
    	if (e.getKeyCode() == KeyEvent.VK_S){
			if (e.getModifiers() == KeyEvent.CTRL_MASK) {
				owner.save();
			} else if (e.getModifiers() == KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK) {
				owner.saveAll();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_W) {
			if (e.getModifiers() == KeyEvent.CTRL_MASK) {
				owner.closeTab();
			} else if (e.getModifiers() == KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK){
				owner.closeAllTabs();
			}
		}	
	}

	public void keyReleased(KeyEvent e) {	
	}

	public void keyTyped(KeyEvent e) {	
	}
		
}

class CloseTabIcon implements Icon {
	
    private int x_pos;  
    private int y_pos;  
    private int width;
    private int height;  
    
    private Icon fileIcon;
    	
    public CloseTabIcon(Icon fileIcon) { 
    	this.fileIcon = fileIcon;  
    	width = 16;    
    	height = 16; 
    } 

    public void paintIcon(Component c, Graphics g, int x, int y) {    
    	this.x_pos = x;    
    	this.y_pos = y;
    	Color col = g.getColor();    
    	g.setColor(Color.gray);
    	int y_p = y + 2;
    	g.drawLine(x + 1,  y_p,      x + 12, y_p);
    	g.drawLine(x + 1,  y_p + 13, x + 12, y_p + 13); 
    	g.drawLine(x,      y_p + 1,  x,      y_p + 12);    
    	g.drawLine(x + 13, y_p + 1,  x + 13, y_p + 12);   
    	g.drawLine(x + 3,  y_p + 3,  x + 10, y_p + 10);    
    	g.drawLine(x + 3,  y_p + 4,  x + 9,  y_p + 10);   
    	g.drawLine(x + 4,  y_p + 3,  x + 10, y_p + 9);   
    	g.drawLine(x + 10, y_p + 3,  x + 3,  y_p + 10);  
    	g.drawLine(x + 10, y_p + 4,  x + 4,  y_p + 10);  
    	g.drawLine(x + 9,  y_p + 3,  x + 3,  y_p + 9);    
    	g.setColor(col);  
    	if (fileIcon != null) {   
    		fileIcon.paintIcon(c, g, x + width, y_p);    
    	}
    } 

    public int getIconWidth() {   
   		return (width + (fileIcon != null ? fileIcon.getIconWidth() : 0));  
   	} 

    public int getIconHeight() {    
    	return height;  
    } 

    public Rectangle getBounds() {    
    	return new Rectangle(x_pos, y_pos, width, height);  
    }                                                                 
    
}    