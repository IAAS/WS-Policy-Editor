/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.w3c.policy.util;

import gui.editor.DocumentFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.policy.AllAlternative;
import org.w3c.policy.IAssertion;
import org.w3c.policy.Policy;
import org.w3c.policy.PolicyReference;
import org.w3c.policy.PrimitiveAssertion;
import org.w3c.policy.ExactlyOneAlternative;

import util.DocumentUtil;
import util.StringUtil;

/**
 * Diese Klasse spielt eine wichtige Rolle fuer die Policy Engine. Sie parst
 * eine PolicyDatei und liefert der Policy Engine die entsprechende Policy
 * zurueck
 * 
 * @author      Zhilei Ma
 * @version     1.0
 * @see         PolicyWriter
 */
public class PolicyReader {

	private static Hashtable<QName, String> getAttributes(Element element) {

		Hashtable<QName, String> attributes = new Hashtable<QName, String>();

		NamedNodeMap map = element.getAttributes();

		int length = map.getLength();

		for (int i = 0; i < length; i++) {
			Attr attribute = (Attr) map.item(i);
			String prefix = attribute.getPrefix();
			QName qn = null;
			if (prefix != null) {
				qn = new QName(attribute.getNamespaceURI(), attribute
						.getLocalName(), prefix);
			} else {
				qn = new QName(attribute.getNamespaceURI(), attribute
						.getLocalName());
			}
			attributes.put(qn, attribute.getValue());
		}
		return attributes;
	}

	/**
	 * Parst die gegebene Assertiondatei und dann liefert die entsprechende
	 * PrimitiveAssertion zurueck.
	 */
	public static PrimitiveAssertion parseAssertion(File fileName) {
		Document doc = DocumentFactory.parseXML(fileName);
		Element root = DocumentUtil.getDocumentRoot(doc);
		
		return readPrimitiveAssertion(root);
	}
	
	/**
	 * Parst die gegebene Policydatei und dann liefert die entsprechende
	 * Policy zurueck.
	 */
	public static Policy parsePolicy(File fileName) {
		Document doc = DocumentFactory.parseXML(fileName);
		Element root = DocumentUtil.getDocumentRoot(doc);

		return readPolicy(root);
	}

	private static AllAlternative readAndAlternative(Element element) {
		AllAlternative andAlternative = new AllAlternative();
		
		andAlternative.addTerms(readTerms(element));
		
		return andAlternative;
	}

	private static IAssertion readAssertion(Element element) {
		String namespace = element.getNamespaceURI();
		String localName = element.getLocalName();

		if (!(namespace.equals(PolicyConstants.WS_POLICY_NAMESPACE_URI))) {
			return readPrimitiveAssertion(element);
		}

		if (localName.equals(PolicyConstants.WS_POLICY)) {
			return readPolicy(element);

		} else if (localName.equals(PolicyConstants.AND_ALTERNATIVE)) {
			return readAndAlternative(element);

		} else if (localName.equals(PolicyConstants.XOR_ALTERNATIVE)) {
			return readXorAlternative(element);

		} else if (localName.equals(PolicyConstants.WS_POLICY_REFERENCE)) {
			return readPolicyReference(element);

		} else {
			throw new RuntimeException("unknown element ..");
		}
	}

	private static ArrayList<Comment> readComments(Element element) {
		ArrayList<Comment> terms = new ArrayList<Comment>();

		NodeList list = element.getChildNodes();

		int length = list.getLength();

		for (int i = 0; i < length; i++) {
			Object obj = list.item(i);

			if (obj instanceof Comment) {
				String value = ((Comment)obj).getNodeValue();
				
				if (value != null && value.length()!=0) {
					terms.add((Comment)obj);
				}
				
			}
		}

		return terms;
	}

	private static Policy readPolicy(Element element) {
		Policy policy = new Policy(element);

		policy.setAttributes(getAttributes(element));

		policy.addTerms(readTerms(element));

		policy.setComments(readComments(element));
		
		return policy;
	}

	private static PolicyReference readPolicyReference(Element element) {
		Attr attribute = element.getAttributeNode("URI");
		
		PolicyReference reference = new PolicyReference(attribute.getValue());
		
		return reference;
	}

	private static PrimitiveAssertion readPrimitiveAssertion(Element element) {
		QName qname;

		if (element.getPrefix() == null) {
			qname = new QName(element.getNamespaceURI(), element
					.getLocalName());
		} else {
			qname = new QName(element.getNamespaceURI(),
					element.getLocalName(), element.getPrefix());
		}

		PrimitiveAssertion result = new PrimitiveAssertion(qname);

		result.setAttributes(getAttributes(element));

		String isOptional = result.getAttribute(new QName(
				PolicyConstants.WS_POLICY_NAMESPACE_URI, "Optional"));

		result.setOptional(new Boolean(isOptional).booleanValue());
		
		String isIgnorable = result.getAttribute(new QName(
				PolicyConstants.WS_POLICY_NAMESPACE_URI, "Ignorable"));

		result.setIgnorable(new Boolean(isIgnorable).booleanValue());

		NodeList list = element.getChildNodes();
		int length = list.getLength();

		for (int i = 0; i < length; i++) {
			Node node = list.item(i);
			short nodeType = node.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;

				if (childElement.getNamespaceURI().equals(
						PolicyConstants.WS_POLICY_NAMESPACE_URI)) {
					if (childElement.getLocalName().equals(
							PolicyConstants.WS_POLICY)) {
						Policy policy = readPolicy(childElement);
						result.addTerm(policy);
					} else if (childElement.getLocalName().equals(
							PolicyConstants.XOR_ALTERNATIVE)) {
						ExactlyOneAlternative xor = readXorAlternative
						    (childElement);
						result.addTerm(xor);
					} else if (childElement.getLocalName().equals(
							PolicyConstants.AND_ALTERNATIVE)) {
						AllAlternative and = readAndAlternative(childElement);
						result.addTerm(and);
					}
				} else {
					PrimitiveAssertion pa = readPrimitiveAssertion
					    (childElement);
					result.addTerm(pa);
				}
			} else if (nodeType == Node.TEXT_NODE) {
				String value = node.getNodeValue();
				String strValue = StringUtil.normalizeString(value);

				if (strValue != null && strValue.length() != 0) {
					result.addStrValue(strValue);
				}
			} else if (nodeType == Node.COMMENT_NODE) {
				String comment = node.getNodeValue();

				if (comment != null && comment.length() != 0) {
					result.addComment((Comment)node);
				}
			}
		}
		return result;
	}

	private static ArrayList readTerms(Element element) {
		ArrayList<IAssertion> terms = new ArrayList<IAssertion>();

		NodeList list = element.getChildNodes();

		int length = list.getLength();

		for (int i = 0; i < length; i++) {
			Object obj = list.item(i);

			if (obj instanceof Element) {
				Element e = (Element) obj;
				terms.add(readAssertion(e));
			}
		}
		return terms;
	}

	private static ExactlyOneAlternative readXorAlternative(Element element) {
		ExactlyOneAlternative xorAlternative = new ExactlyOneAlternative();
		
		xorAlternative.addTerms(readTerms(element));
		
		return xorAlternative;
	}

}