
package gui.util;
 
import gui.editor.DomTreeNode;
import gui.explorer.IconData;
import gui.images.Images;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.policy.util.PolicyUtil;

/**
 * Verwaltet alle TreeCellRenderer
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         M_TreeCellsEditor
 */
public class M_TreeCellsRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -8387588320125717274L;

	/**
	 * Legt einen neuen TreeCellRenderer anhand der gegebenen Parameter an.
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus1) {

		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus1);

		if (value instanceof DomTreeNode) {
			Node node = ((DomTreeNode) value).getDOMNode();
			if (node instanceof Element) {
				if (PolicyUtil.isPolicyReference(node)) {
					setIcon(Images.ICON_POLICY_REFERENCE_16);
					return result;
				}

				if (PolicyUtil.isPolicyOperator(node)) {
					setIcon(Images.ICON_POLICY_OPERATOR_16);
					return result;
				}

				if (PolicyUtil.isXOROperator(node)) {
					setIcon(Images.ICON_XOR_16);
					return result;
				}

				if (PolicyUtil.isAllOperator(node)) {
					setIcon(Images.ICON_ALL_16);
					return result;
				}

				if (PolicyUtil.isAssertion(node)) {
					setIcon(Images.ICON_ASSERTION_16);
					return result;
				}

				setIcon(expanded ? openIcon : closedIcon);
			} else if (node instanceof Text)
				setIcon(Images.ICON_TEXT_16);
			else if (node instanceof Comment)
				setIcon(Images.ICON_COMMENT_16);
			return result;
		}

		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object obj = node.getUserObject();
			String nodeName = obj.toString();
			if (nodeName.endsWith(".asrt")) {
				nodeName = nodeName.replace(".asrt", "");
			} else if (nodeName.endsWith(".xml")){
				nodeName = nodeName.replace(".xml", "");
			}
			setText(nodeName);
			if (obj instanceof Boolean)
				setText("Retrieving data...");

			if (obj instanceof IconData) {
				IconData idata = (IconData) obj;
				if (expanded)
					if (idata.getExpandedIcon() != null) {
						setIcon(idata.getExpandedIcon());
					} else {
						setIcon(leafIcon);
					}

				else if (idata.getIcon() != null) {
					setIcon(idata.getIcon());
				} else {
					setIcon(leafIcon);
				}
			}
			return result;
		}

		return result;
	}
}
