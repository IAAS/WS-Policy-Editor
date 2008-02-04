package org.w3c.policy.util;

import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.policy.AllAlternative;
import org.w3c.policy.IAssertion;
import org.w3c.policy.Policy;
import org.w3c.policy.PolicyReference;
import org.w3c.policy.PrimitiveAssertion;
import org.w3c.policy.ExactlyOneAlternative;

import util.MOptionPanes;
import util.StringUtil;

/**
 * Schreibt eine geparste Policy oder eine geparste Assertion in einer Datei auf
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         PolicyReader
 */
public class PolicyWriter {
	private Hashtable<QName, String> attributes;

	private int counter = 1;

	private int indent = 0;

	private final int tab = 4;

	private BufferedWriter writer;

	public PolicyWriter(File file) {
		try {
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by creating a policy writer.");
		}
	}

	private String generateNamespace() {
		return "ns" + counter++;
	}

	private void newline() {
		try {
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by writing a new line.");
		}
	}

	private void wirteEndPolicy(Policy policy) {
		Element element = policy.getElement();
		String prefix = element.getPrefix();
		String localName = element.getLocalName();
		String qName = prefix + ":" + localName;

		indent--;
		writeTabs();

		System.out.println("</" + qName + ">");
		writeToFile("</" + qName + ">");
		newline();
	}

	private void writeAndAlternative(AllAlternative assertion) {
		writeTabs();
		System.out.println("<" + PolicyConstants.QNAME_AND + ">");
		writeToFile("<" + PolicyConstants.QNAME_AND + ">");
		newline();

		indent++;

		Iterator iterator = assertion.getTerms().iterator();
		while (iterator.hasNext()) {
			IAssertion term = (IAssertion) iterator.next();
			writeAssertion(term);
		}

		indent--;

		writeTabs();
		System.out.println("</" + PolicyConstants.QNAME_AND + ">");
		writeToFile("</" + PolicyConstants.QNAME_AND + ">");
		newline();
	}

	private void writeAssertion(IAssertion assertion) {
		if (assertion instanceof PrimitiveAssertion) {
			writePrimitiveAssertion((PrimitiveAssertion) assertion);

		} else if (assertion instanceof ExactlyOneAlternative) {
			writeXorAlternatvie((ExactlyOneAlternative) assertion);

		} else if (assertion instanceof PolicyReference) {
			writePolicyReference((PolicyReference) assertion);

		} else if (assertion instanceof Policy) {
			writePolicy((Policy) assertion);

		} else if (assertion instanceof AllAlternative) {
			writeAndAlternative((AllAlternative) assertion);

		} else {
			throw new RuntimeException("unknown element type");
		}
	}

	private void writeAttribute(String prefix, String localName, String value) {
		if (!prefix.equals("")) {
			System.out.print(" " + prefix + ":" + localName + "=\"" + value
					+ "\"");
			writeToFile(" " + prefix + ":" + localName + "=\"" + value + "\"");
			return;
		}

		System.out.print(" " + localName + "=\"" + value + "\"");
		writeToFile(" " + localName + "=\"" + value + "\"");
	}

	private void writeAttributes(Hashtable attrTable) {
		Iterator iterator = attrTable.keySet().iterator();
		while (iterator.hasNext()) {
			QName qname = (QName) iterator.next();
			String value = (String) attrTable.get(qname);

			String prefix = qname.getPrefix();

			writeAttribute(prefix, qname.getLocalPart(), value);
		}
	}

	private void writeEndElement(String prefix, String localName) {
		writeTabs();

		if (prefix == null || prefix.equals("")) {
			System.out.println("</" + localName + ">");
			writeToFile("</" + localName + ">");
			newline();
			return;
		}
		System.out.println("</" + prefix + ":" + localName + ">");
		writeToFile("</" + prefix + ":" + localName + ">");
		newline();
	}

	private void writeNamespace(String prefix, String namespaceURI) {
		if (prefix == null || prefix.equals("")) {
			System.out.print(" " + PolicyConstants.XMLNS_PREFIX + "=\""
					+ namespaceURI + "\"");
			writeToFile(" " + PolicyConstants.XMLNS_PREFIX + "=\""
					+ namespaceURI + "\"");
			return;
		}

		System.out.print(" " + PolicyConstants.XMLNS_PREFIX + ":"
				+ prefix + "=\"" + namespaceURI + "\"");
		writeToFile(" " + PolicyConstants.XMLNS_PREFIX + ":" + prefix
				+ "=\"" + namespaceURI + "\"");
	}

	/**
	 * Schreibt eine Policy in einer Datei auf.
	 */
	public void writePolicy(Policy policy) {
		writeStartPolicy(policy);

		ArrayList<Comment> commentTerms = policy.getComments();

		Iterator commentIterator = commentTerms.iterator();

		while (commentIterator.hasNext()) {
			Comment comment = (Comment) commentIterator.next();
			String strComment = "<!-- "
					+ StringUtil.normalizeString(comment.getNodeValue())
					+ " -->";

			writeTabs();
			System.out.println(strComment);
			writeToFile(strComment);
			newline();
		}

		indent++;

		Iterator iterator = policy.getTerms().iterator();
		while (iterator.hasNext()) {
			IAssertion term = (IAssertion) iterator.next();
			writeAssertion(term);
		}

		wirteEndPolicy(policy);

		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by writing a policy.");
		}
	}

	private void writePolicyReference(PolicyReference reference) {
		writeTabs();

		System.out.print("<" + PolicyConstants.QNAME_POLICY_REFERENCE);
		writeToFile("<" + PolicyConstants.QNAME_POLICY_REFERENCE);

		writeAttribute("", "URI", reference.getPolicyURIString());

		System.out.println("/>");
		writeToFile("/>");

		newline();
	}
	
	/**
	 * Schreibt eine Assertion in einer Datei auf.
	 */
	public void writePrimitiveAssertion(PrimitiveAssertion assertion) {
		QName qName = assertion.getQName();

		String prefix = (qName.getPrefix() != null) ? qName.getPrefix()
				: generateNamespace();

		String localName = qName.getLocalPart();
		String namespaceURI = qName.getNamespaceURI();

		attributes = assertion.getAttributes();

		/* Write start tag */
		writeStartElement(prefix, localName);

		if (!attributes.containsValue(namespaceURI)) {
			writeNamespace(prefix, namespaceURI);
		}

		writeAttributes(attributes);

		System.out.println(">");
		writeToFile(">");
		newline();

		indent++;

		// writeComments(assertion.getComments());

		ArrayList<Comment> commentTerms = assertion.getComments();

		Iterator commentIterator = commentTerms.iterator();

		while (commentIterator.hasNext()) {
			Comment comment = (Comment) commentIterator.next();
			String strComment = "<!-- "
					+ StringUtil.normalizeString(comment.getNodeValue())
					+ " -->";

			writeTabs();
			System.out.println(strComment);
			writeToFile(strComment);
			newline();
		}

		ArrayList<String> textTerms = assertion.getStrValues();

		Iterator textIterator = textTerms.iterator();

		while (textIterator.hasNext()) {
			String textTerm = (String) textIterator.next();

			writeTabs();
			System.out.println(textTerms);
			writeToFile(textTerm);
			newline();
		}

		Iterator iterator = assertion.getTerms().iterator();

		while (iterator.hasNext()) {
			IAssertion term = (IAssertion) iterator.next();
			writeAssertion(term);
		}

		indent--;

		writeEndElement(prefix, localName);
		
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by writing a policy.");
		}
	}

	private void writeStartElement(String prefix, String localName) {
		writeTabs();

		if (prefix == null || prefix.equals("")) {
			System.out.print("<" + localName);
			writeToFile("<" + localName);
			return;
		}

		System.out.print("<" + prefix + ":" + localName);
		writeToFile("<" + prefix + ":" + localName);
	}

	private void writeStartPolicy(Policy policy) {
		Element element = policy.getElement();
		String prefix = element.getPrefix();
		String localName = element.getLocalName();
		String namespaceURI = element.getNamespaceURI();
		String qName = prefix + ":" + localName;

		writeTabs();

		System.out.print("<" + qName);
		writeToFile("<" + qName);

		attributes = policy.getAttributes();

		if (policy.getId() != null) {
			System.out.print(" wsu:Id=\"" + policy.getId() + "\"");
			writeToFile(" wsu:Id=\"" + policy.getId() + "\"");
			QName id = new QName(PolicyConstants.WSU_NAMESPACE_URI, "Id", "wsu");
			attributes.remove(id);
		}

		if (policy.getBase() != null) {
			System.out.print(" xml:base=\"" + policy.getBase() + "\"");
			writeToFile(" xml:base=\"" + policy.getBase() + "\"");
			QName id = new QName(PolicyConstants.XMLNS_NAMESPACE_URI, "base",
					"xml");
			attributes.remove(id);
		}

	    if (attributes.isEmpty()){
	    	writeNamespace(prefix, namespaceURI);
	    } else {
		    writeAttributes(attributes);
	    }

		System.out.println(">");
		writeToFile(">");
		newline();
	}

	private void writeTabs() {
		for (int i = 0; i < indent; i++) {
			for (int j = 0; j < tab; j++) {
				System.out.print(" ");
				writeToFile(" ");
			}
		}
	}

	private void writeToFile(String str) {
		try {
			writer.write(str);
		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by writing the policy to the file.");
		}
	}

	private void writeXorAlternatvie(ExactlyOneAlternative assertion) {
		writeTabs();
		System.out.println("<" + PolicyConstants.QNAME_XOR + ">");
		writeToFile("<" + PolicyConstants.QNAME_XOR + ">");
		newline();

		indent++;

		Iterator iterator = assertion.getTerms().iterator();
		while (iterator.hasNext()) {
			IAssertion term = (IAssertion) iterator.next();
			writeAssertion(term);
		}

		indent--;

		writeTabs();
		System.out.println("</" + PolicyConstants.QNAME_XOR + ">");
		writeToFile("</" + PolicyConstants.QNAME_XOR + ">");
		newline();
	}
}
