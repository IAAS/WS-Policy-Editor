package util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.policy.util.PolicyConstants;

/**
 * @author      Zhilei Ma
 * @version     1.0 
 */
public class DocumentUtil {

	public static Element createElementNS(Document document,
			String namespaceURI, String qName, boolean addNSAsAttribute) {
		Element newElement = document.createElementNS(namespaceURI, qName);
		if (addNSAsAttribute) {
			String prefix = XMLUtil.getPrefix(qName);
			String attrName;

			if (prefix == null) {
				attrName = PolicyConstants.XMLNS_PREFIX;
			} else {
				attrName = PolicyConstants.XMLNS_PREFIX + ":" + prefix;
			}
			
			Attr attr = document.createAttribute(attrName);
			attr.setNodeValue(namespaceURI);
			
			newElement.getAttributes().setNamedItem(attr);
		}

		return newElement;
	}

	/**
	 * Return the nomarlized root element of a document.
	 * 
	 * @param document
	 *            a instance of the <code>Document</code> interface
	 * @return the normailized root element of the document.
	 */
	public static Element getDocumentRoot(Document document) {
		Element root = document.getDocumentElement();
		root.normalize();
		return root;
	}
}
