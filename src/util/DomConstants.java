package util;

import org.w3c.dom.Node;
 
/**
 * Enthaelt zwoelf verschiedene Knotentype in DomTree
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class DomConstants {
	
	/**
	 * 12 types of DOM nodes in text.
	 */
	public static final String[] DOM_NODE_TYPE = { "none", "Element",
			"Attribute", "Text", "CDATA", "Entity Reference", "Entity",
			"Processing Instruction", "Comment", "Document", "Document Type",
			"Document Fragment", "Notation", };

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int ELEMENT_TYPE = Node.ELEMENT_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int ATTR_TYPE = Node.ATTRIBUTE_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int TEXT_TYPE = Node.TEXT_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int CDATA_TYPE = Node.CDATA_SECTION_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int ENTITYREF_TYPE = Node.ENTITY_REFERENCE_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int ENTITY_TYPE = Node.ENTITY_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int PROCINSTR_TYPE = Node.PROCESSING_INSTRUCTION_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int COMMENT_TYPE = Node.COMMENT_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int DOCUMENT_TYPE = Node.DOCUMENT_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int DOCTYPE_TYPE = Node.DOCUMENT_TYPE_NODE;
	
	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int DOCFRAG_TYPE = Node.DOCUMENT_FRAGMENT_NODE;

	/**
	 * Types of DOM nodes in integer.
	 */
	public static final int NOTATION_TYPE = Node.NOTATION_NODE;

}
