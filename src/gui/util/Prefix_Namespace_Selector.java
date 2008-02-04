package gui.util;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.policy.util.NSRegistry;

/**
 *  Diese Klasse bietet dem Benutzer die Moeglichkeit an, das gewuenschte 
 *  Praefix und Namespace mit dem Maus aus dem Combo Box zu selektieren bzw.
 *  mit der Tastatur einzutippen.
 * 
 * @author      Windy
 * @version     1.0
 */
public class Prefix_Namespace_Selector extends JPanel {

	private static final long serialVersionUID = -3039875615800216467L;
	
	private JComboBox prefixComboBox;
	
	private JTextField prefixEditor;
	
	private DefaultComboBoxModel prefixModel;
	
	private JComboBox namespaceComboBox;
	
	private JTextField namespaceEditor;
	
	private DefaultComboBoxModel namespaceModel;
	
	private JButton btNSList;
	
	private JLabel prefixLabel;
	
	private JLabel namespaceLabel;
	
	private Object[] newModel1;
	
	private Object[] newModel2;
	
	/**
	 * Create the frame
	 */
	public Prefix_Namespace_Selector(String defaultPrefix, String defaultNsURI) {
		super();
		setLayout(null);
        createComponent();
        
		prefixComboBox.setSelectedIndex(-1);
		namespaceComboBox.setSelectedItem(-1);
        prefixEditor.setText(defaultPrefix);
        namespaceEditor.setText(defaultNsURI);
	}
	
	private void createComponent(){

		prefixLabel = new JLabel();
		prefixLabel.setText("prefix :");
		prefixLabel.setBounds(10, 10, 90, 26);
		add(prefixLabel);

		namespaceLabel = new JLabel();
		namespaceLabel.setText("namespace :");
		namespaceLabel.setBounds(121, 10, 198, 26);
		add(namespaceLabel);
		
		prefixModel = new DefaultComboBoxModel(NSRegistry.getPrefixList());
		prefixComboBox = new JComboBox(prefixModel);
		prefixComboBox.setBounds(10, 42, 90, 26);
		prefixComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int selectedIndex = prefixComboBox.getSelectedIndex();
				if (selectedIndex == -1) {
					return;
				}		
				if (selectedIndex < namespaceComboBox.getItemCount()){
		            namespaceComboBox.setSelectedIndex(selectedIndex);
				}
			}
		});

        prefixEditor = (JTextField) prefixComboBox.getEditor()
            .getEditorComponent();
        prefixEditor.addKeyListener(new KeyAdapter(){
        	
        	@Override
    		public void keyReleased(KeyEvent e) {

                if (e.getKeyChar() ==  KeyEvent.CHAR_UNDEFINED){
                	return;   
                }   
                
        		if (e.getKeyCode() == KeyEvent.VK_ENTER){
    				prefixComboBox.hidePopup();
    				return;
        		}
        		
                setPopupMenu("prefix", prefixEditor.getText());   
    		}
        	
        });
        
		prefixComboBox.setEditable(true);
		this.add(prefixComboBox);

		namespaceModel = new DefaultComboBoxModel(NSRegistry.getNsURIList());
		namespaceComboBox = new JComboBox(namespaceModel);
		namespaceComboBox.setEditable(true);
		namespaceComboBox.setBounds(121, 42, 198, 26);
		namespaceComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int selectedIndex = namespaceComboBox.getSelectedIndex();
				if (selectedIndex == -1) {
					return;
				}			
				if (selectedIndex < prefixComboBox.getItemCount()){
					prefixComboBox.setSelectedIndex(selectedIndex);
				}
			}
		});
        namespaceEditor = (JTextField) namespaceComboBox.getEditor()
            .getEditorComponent();
        namespaceEditor.addKeyListener(new KeyAdapter(){
        	
    		public void keyReleased(KeyEvent e) {

                if (e.getKeyChar() ==  KeyEvent.CHAR_UNDEFINED){
                	return;   
                }   
                
        		if (e.getKeyCode() == KeyEvent.VK_ENTER){
    				namespaceComboBox.hidePopup();
    				return;
        		}

                setPopupMenu("namespace", namespaceEditor.getText());   
    		}
        });
		this.add(namespaceComboBox);

		btNSList = new JButton("NS-List");
		btNSList.setBounds(341, 42, 90, 26);
		btNSList.setToolTipText("Select a namespace from list");
	    btNSList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NamespaceList nsList = new NamespaceList();
				nsList.setLocationRelativeTo(null);
			    nsList.setVisible(true);
			    String prefix = nsList.getSelectedPrefix();
			    String nsURI = nsList.getSelectedNsURI();
			    if (prefix == null || nsURI == null) {
			    	return;
			    } 
			    refreshComboBox();
			    int selectedIndex = NSRegistry.getIndexOf(prefix,nsURI);
			    prefixComboBox.setSelectedIndex(selectedIndex);
			    namespaceComboBox.setSelectedIndex(selectedIndex);
			}
	    });
		this.add(btNSList);
	}
	     
      private void setPopupMenu(String purpose, String typedString){
    	  String otherString;
    	  if (purpose.equals("prefix")){
    		  otherString = getNsURI();
    		  matchingMenu("prefix", typedString);
    		  prefixModel = new DefaultComboBoxModel(newModel1);
              prefixComboBox.setModel(prefixModel);   
    		  namespaceModel = new DefaultComboBoxModel(newModel2);
              namespaceComboBox.setModel(namespaceModel);
              prefixEditor.setText(typedString);  
              namespaceEditor.setText(otherString);
              if (prefixComboBox   !=   null)   {   
             	  prefixComboBox.showPopup();
              }   
    	  } else {
    		  otherString = getPrefix();
    		  matchingMenu("namespace", typedString);
    		  namespaceModel = new DefaultComboBoxModel(newModel1);
              namespaceComboBox.setModel(namespaceModel);
    		  prefixModel = new DefaultComboBoxModel(newModel2);
              prefixComboBox.setModel(prefixModel);  
    		  namespaceEditor.setText(typedString);  
    		  prefixEditor.setText(otherString);
              if (namespaceComboBox   !=   null)   {   
            	  namespaceComboBox.showPopup();
              }   
    	  }
              
      }   
       
      private void matchingMenu(String purpose, String typedString){
    	  List<Object> matched1 = new Vector<Object>();   
          List<Object> unmatched1 = new Vector<Object>();
    	  List<Object> matched2 = new Vector<Object>();   
          List<Object> unmatched2 = new Vector<Object>();
          DefaultComboBoxModel model1;
          DefaultComboBoxModel model2;         
          Object[] obj1;
          Object[] obj2;
          
          if (purpose.equals("prefix")){
        	  obj1 = NSRegistry.getPrefixList();
        	  obj2 = NSRegistry.getNsURIList();
          } else {
        	  obj1 = NSRegistry.getNsURIList();
        	  obj2 = NSRegistry.getPrefixList();
          }
          
          model1 = new DefaultComboBoxModel(obj1);
          model2 = new DefaultComboBoxModel(obj2);
     
          for (int k = 0; k < model1.getSize(); k++){
        	  Object item =  model1.getElementAt(k);   
              if (item != null){ 
            	  if (item.toString().startsWith(typedString)){
            		  matched1.add(item);
            		  matched2.add(model2.getElementAt(k));
            	  } else {
            		  unmatched1.add(item);
            		  unmatched2.add(model2.getElementAt(k));
            	  }
               }else {
            	   unmatched1.add(item);
            	   unmatched2.add(model2.getElementAt(k));
               }
          }   
      
          if (matched1.size() == 0){
        	  newModel1 = unmatched1.toArray();
        	  newModel2 = unmatched2.toArray();
        	  return;
          }
          
          for (int i = 0; i < unmatched1.size(); i++){   
              matched1.add(unmatched1.get(i));   
              matched2.add(unmatched2.get(i));
          }   
          
          newModel1 = matched1.toArray();   
          newModel2 = matched2.toArray();
  
      }   
      
	private void refreshComboBox(){	
		int number = NSRegistry.countNamespace();
		Object[] prefixList = NSRegistry.getPrefixList();
		Object[] nsURIList = NSRegistry.getNsURIList();
		prefixComboBox.removeAllItems();
		namespaceComboBox.removeAllItems();
		for (int i = 0; i < number; i++){
			prefixComboBox.addItem(prefixList[i]);
			namespaceComboBox.addItem(nsURIList[i]);
		}
	}
	
	/**
	 * 	
	 * Stellt den JLabels, was wird als Titel auf den angezeigt.
	 * 
	 * @param title1   der Name von PrefixLabel
	 * @param title2   der Name von NamespaceLabel
	 */
	public void setLabelTitle(String title1, String title2){
		prefixLabel.setText(title1);
		namespaceLabel.setText(title2);
	}
	
	/**
	 * Stellt, ob das Praefix und das NamespaceURI aenderbar sind.
	 * 
	 * @see   #isEditable()
	 */
	public void setSelectorEditable(boolean editable){
		prefixComboBox.setEnabled(editable);
		namespaceComboBox.setEnabled(editable);
		btNSList.setEnabled(editable);
	}
	
	/**
	 * das Combo Box fuer Praefix
	 */
	public JComboBox getPrefixComboBox(){
		return this.prefixComboBox;
	}
	
	/**
	 * das Combo Box fuer NamespaceURI
	 */
	public JComboBox getNamespaceComboBox(){
		return this.namespaceComboBox;
	}
	
	/**
	 * das vom Benutzer eingegebene Praefix
	 */
	public String getPrefix(){
		return prefixEditor.getText();
	}
	
	/**
	 * das vom Benutzer eingegebene NamespaceURI
	 */
	public String getNsURI(){
		return namespaceEditor.getText();
	}
	
	/**
	 * ob das Praefix und das NamespaceURI aenderbar sind.
	 * 
	 * @see     #setSelectorEditable(boolean)
	 */
	public boolean isEditable(){
		if (prefixComboBox.isEnabled() && namespaceComboBox.isEnabled() 
				&& btNSList.isEnabled()){
			return true;
		} else {
			return false;
		}
	}

}
