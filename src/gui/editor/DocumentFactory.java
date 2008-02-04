package gui.editor;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import util.MOptionPanes;
import util.Messages;

/**
 * Alle DocumentPanen werden in einen TabbedPane zusammengefasst. Die TabbedPane
 * wird durch diese Klasse implementiert, die die Klasse JTabbedPane erweitert.
 * Alle Titel der einzelnen DocumentPane sind in einer Liste zusammengefasst.
 * Jeder Titel repraesentiert den Name der Policy im entsprechende DocumentPane.
 * Es wird jeweils nur eine DocumentPane im Vordergrund angezeigt.
 * Der Benutzer kann die gewuenschte DocumentPane in den Vordergrund bringen,
 * indem er mit der Maus auf den Titel des DocumentPane klickt. Mit diesem 
 * Mechanismus lassen sich mehrere Policies gleichzeitig oeffnen, ob jedoch
 * die Darstellung zu ueberladen ist.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class DocumentFactory implements Serializable {

	private static final long serialVersionUID = 6924707320668327607L;

	/*
	 * Whether you parse an XML document or create one, a Document instance 
	 * will result. We want to reference this object from another method later,
	 * so define it as a gui.util object here.
	 */

	private static Schema policySchema;

	/**
	 * Obtain a new instance of a DOM Document object to build a DOM tree with.
	 * 
	 * @return A new instance of a DOM Document object.
	 */
	public static Document createNewDocument() {
		DocumentBuilder builder = getDocumentBuilder();
		return builder.newDocument();
	}

	/**
	 * Return a <code>DocumentBuilder</code> for obtaining a DOM
	 * <code>Document</code> instance from a XML document.
	 * 
	 * @exception <code>ParserConfigurationException</code> if a 
	 *                DocumentBuilder cannot be created which satisfies the
	 *                configuration requested.
	 */
	private static DocumentBuilder getDocumentBuilder() {
		DocumentBuilderFactory factory = getDocumentBuilderFactory();

		DocumentBuilder builder = null;

		/*
		 * Creates a new instance of a using the currently configured
		 * parameters.
		 * 
		 * Throws ParserConfigurationException if a DocumentBuilder cannot be
		 * created which satisfies the configuration requested. Returns A new
		 * instance of a DocumentBuilder.
		 */
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException pconfex) {
			/*
			 * Parser with specified options cannot be built.
			 */
			pconfex.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"Parser configuration error.");
		}

		if (builder == null) {
			return null;
		}
		return builder;

	}

	/**
	 * Return a <code>DocumentBuilderFactory</code> that enables applications
	 * to obtain a parser that produces DOM object trees from XML documents.
	 * 
	 * @param validating
	 *            true, if the parser produced will validate documents as they
	 *            are parsed; false otherwise.
	 * @param nsAware
	 *            true, if the parser produced will provide support for XML
	 *            namespaces; false otherwise.
	 */
	private static DocumentBuilderFactory getDocumentBuilderFactory() {
		/*
		 * Obtain a instance of a factory that can give us a document builder.
		 */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		/*
		 * To use modern schema languages such as W3C XML Schema or RELAX NG
		 * instead of DTD, you can configure your parser to be a non-validating
		 * parser by leaving the setValidating(boolean) method false, then use
		 * the setSchema(Schema) method to associate a schema to a parser.
		 */

		factory.setValidating(false);

		/*
		 * Specifies that the parser produced by this code will provide support
		 * for XML namespaces. By default the value of this is set to false
		 */
		factory.setNamespaceAware(true);

		return factory;
	}

	/**
	 * Setzt Policyschema auf einem bestimmten Schema
	 *
	 */
	public static void setSchema() {
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Source schemaFile = new StreamSource(new File("WS-Policy.xsd"));

		try {
			policySchema = factory.newSchema(schemaFile);
		} catch (SAXException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"SAX error by creating a schema.");
		}
	}

	/**
	 * Legt ein neues Dokument an und dann gibt das zurueck
	 * @return   das neue Dokument
	 */
	public static Document newXMLDocument() {
		return getDocumentBuilder().newDocument();
	}

	/**
	 * Parse the content of the given file as an XML document and return a new
	 * DOM object. An <code>IllegalArgumentException</code> is thrown if the
	 * <code>File</code> is null.
	 * 
	 * @exception <code>IOException</code> If any IO errors occur.
	 * @exception <code>SAXException</code> If any parse errors occur.
	 */
	public static Document parseXML(File fileName) {

		try {
			DocumentBuilder builder = getDocumentBuilder();

			return builder.parse(fileName);

		} catch (SAXException saxex) {
			/*
			 * Display error generated during parsing.
			 */
			saxex.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"SAX error by parsing the file.\n"
							+ "Please validate your document.");
		} catch (IOException ioex) {
			/*
			 * Display I/O error.
			 */
			ioex.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by parsing the file.");
		}

		return null;

	}

	/**
	 * Macht ein Dokument gueltig
	 * 
	 * @param doc   das Document, das gueltig gemacht wird
	 * @return      ob diese Operation gelungen ist
	 */
	public static boolean validateDocument(Document doc) {
		boolean result = false;
		try {
			if (policySchema == null) {
				MOptionPanes
						.showError(Frame.getFrames()[0], Messages.NO_SCHEMA);
				result = false;
			}

			Validator validator = policySchema.newValidator();

			validator.validate(new DOMSource(doc));
			result = true;
		} catch (SAXException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"SAX error by validating the file.");

		} catch (IOException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"I/O error by validating the file.");
		}
		return result;
	}

	/**
	 * Schreibt ein Dokument in einer Datei auf.
	 * 
	 * @param doc   das Dokument, das in einer Datei aufgeschrieben werden soll.
	 * @param file  die Datei, in der das Dokument aufgeschrieben werden soll.
	 */
	public static void write(Document doc, File file) {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer;

		try {
			transformer = transFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD,"xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}" +
					"indent-amount", "2");
			
			DOMSource source = new DOMSource(doc);
			FileOutputStream out = new FileOutputStream(file);
			
			StreamResult result = new StreamResult(new OutputStreamWriter
					(out,"utf-8"));
			transformer.transform(source, result);

			StreamResult debug = new StreamResult(System.out);
			transformer.transform(source, debug);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"Transformer configuration error by writing the file.");
		} catch (TransformerException e) {
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
					"Transformer error by writing the file.");
		} catch (FileNotFoundException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (UnsupportedEncodingException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

}
