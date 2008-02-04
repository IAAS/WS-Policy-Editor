package org.w3c.policy.util;

import util.Environment;
import util.MOptionPanes;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;


/**
 * Diese Klasse bietet dem Benuzer die Moeglichkeit an, beliebig viele
 * Namespace in System zu speichern und spaeter zu benutzen.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class NSRegistry 
{
	
	private static File NSRegistryFile = new File (Environment
			    .getNSRegistryDataFile());
	
	/**
	 * wie viele Namespaces im System gespeichert worden sind.
	 */
	public static int countNamespace() 
	{
		
		String stBuffer;
		
		int number = 0;
	
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader
					(NSRegistryFile));
			stBuffer = reader.readLine();
			while (!(stBuffer == null)) 
			{
				stBuffer = reader.readLine();
				number ++;
				stBuffer = reader.readLine();
			}
			reader.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
				"I/O error by reading NSRegistryFile.");
		}
		return number;
	}
		
	
	/**
	 * Ermittelt, ob die Datei, in der alle Namespaces gespeichert werden,
	 * im System vorhanden ist.
	 */
	public static boolean fileExists()
	{
		if (NSRegistryFile.exists())
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}
	
	/**
	 * Falls die Datei, in der alle Namespaces gespeichert werden, nicht 
	 * existiert, legt eine neue Datei mit fuenf vorliegenden Namespaces
	 * an, die in Zukunft nicht aendern, nicht loeschen duerfen.
	 *
	 */
	public static void createFile()
	{
		try 
		{
			Environment.checkEditorDataFolder();
			NSRegistryFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(NSRegistryFile));
			writer.write(PolicyConstants.SP_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.SP_NAMESPACE_URI);
			writer.newLine();
			writer.write(PolicyConstants.WSAM_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.WSAM_NAMEPSECE_URI);
			writer.newLine();
			writer.write(PolicyConstants.WS_POLICY_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.WS_POLICY_NAMESPACE_URI);
			writer.newLine();
			writer.write(PolicyConstants.WSPE_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.WSPE_NAMESPACE_URI);
			writer.newLine();
			writer.write(PolicyConstants.WSU_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.WSU_NAMESPACE_URI);
			writer.newLine();
			writer.write(PolicyConstants.XML_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.XML_NAMESPACE_URI);
			writer.newLine();
			writer.write(PolicyConstants.XMLNS_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.XMLNS_NAMESPACE_URI);
			writer.newLine();
			writer.write(PolicyConstants.XS_PREFIX);
			writer.newLine();
			writer.write(PolicyConstants.XS_NAMESPACE_URI);
			writer.newLine();
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
				"I/O error by writing NSRegistry File.");
		}
	}
	
	/**
	 * gibt alle Namespace als ein zwei-dimension-Feld zurueck
	 */
	public static String[][] getNSRegistry()
	{
	
		String stPrefix;
		
		String stNsURI;
		
		String[][]  nsRegistry;
		
		int number = countNamespace();
		
		nsRegistry = new String[number][2];
	
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader
					(NSRegistryFile));
			for (int i = 0; i <= number - 1; i++) 
			{
				stPrefix = reader.readLine();
				stNsURI = reader.readLine();
				nsRegistry[i][0] = stPrefix;
				nsRegistry[i][1] = stNsURI;
			}
			reader.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
				"I/O error by creating NSRegistry.");
		}	
		return nsRegistry;
	}

	/**
	 * Speichert ein neues Paar von "Prefix" und "NamespaceURI"
	 * 
	 * @param prefix   das Praefix
	 * @param nsURI    das NamespaceURI
	 */
	public static void add(String prefix, String nsURI) 
	{

		String[][] oldRegistry = getNSRegistry();
		
		int number = countNamespace();
		
		String[][] nsRegistry = new String [number + 1][2];		
		
		int n = 0;
		
		while ((n < number - 1) && (oldRegistry[n][0].compareTo(prefix) < 0))
		{
			nsRegistry [n][0] = oldRegistry [n][0];
			nsRegistry [n][1] = oldRegistry [n][1];
			n ++;
		}
		while ((n < number - 1) && (oldRegistry[n][0].equals(prefix)) && 
				(oldRegistry[n][1].compareTo(nsURI) < 0))
		{
			nsRegistry [n][0] = oldRegistry [n][0];
			nsRegistry [n][1] = oldRegistry [n][1];
			n ++;
		}
		if (((n == number - 1) && (oldRegistry[n][0].compareTo(prefix) < 0)) || 
			((n == number - 1) && (oldRegistry[n][0].equals(prefix)) && 
					(oldRegistry[n][1].compareTo(nsURI) < 0)))
		{
			nsRegistry [n][0] = oldRegistry [n][0];
			nsRegistry [n][1] = oldRegistry [n][1];
			n ++;		
		} 
		nsRegistry[n][0] = prefix;
		nsRegistry[n][1] = nsURI;
		for (int i = n; i <= number - 1; i++) 
		{
			nsRegistry[i + 1][0] = oldRegistry[i][0];
			nsRegistry[i + 1][1] = oldRegistry[i][1];
		}
		refreshFile(nsRegistry, number + 1);
	}
	
	/**
	 * modifiziert ein Namespace
	 */
	public static void edit(int rowIndex, String newPrefix, String newNsURI)
	{
		delete (rowIndex);
		add (newPrefix, newNsURI);
	}

	/**
	 * entfernt ein Namespace von der Datei
	 */
	public static void delete(int rowIndex)
	{
		String[][] oldRegistry = getNSRegistry();
		
		String[][] nsRegistry;
		
		int number = countNamespace();
		
		nsRegistry = new String [number - 1][2];

		for (int i = 0; i < rowIndex; i++)
		{
			nsRegistry[i][0] = oldRegistry[i][0];
			nsRegistry[i][1] = oldRegistry[i][1];
		}
		for (int i = rowIndex; i <= number - 2; i++) 
		{
			nsRegistry[i][0] = oldRegistry[i+1][0];
			nsRegistry[i][1] = oldRegistry[i+1][1];
		}
		refreshFile(nsRegistry, number - 1);
	}
	
	/**
	 * Sucht anhand des gegebene Praefixes nach das entsprechende NamespaceURI,
	 * falls kein gefunden wird, gibt Null zurueck.
	 * 
	 * @param prefix   das gegebene Praefix
	 * @return         das entsprechende Namespace, falls kein exisitiert, 
	 *                 gibt Null zurueck
	 */
	public static String lookup(String prefix) 
	{
		String[][]  nsRegistry = getNSRegistry();
		
		if (prefix == null) 
		{
			return null;
		}
		int number = countNamespace();
		
		int n = 0;
		
		while ((n < number - 1) && (nsRegistry[n][0].compareTo(prefix) < 0))
		{
			n ++;
		}
		
		if (n == number - 1) 
		{
			if (nsRegistry[n][0].equals(prefix)) 
			{
				return nsRegistry[n][1];
			} 
			else 
			{
				return null;
			}
		} 
		else 
		{
			return nsRegistry[n][1];
		}
	}
	
	/**
	 * Schreibt alle Namespace erneut in der Datei auf.
	 * 
	 * @param   nsRegistry   alle Namespace
	 * @param   number       Anzahl der Namespace
	 */
	public static void refreshFile(String [][] nsRegistry, int number)
	{
		String stPrefix;
		String stNsURI;
		try 
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(NSRegistryFile));
			for (int i = 0; i <= number - 1; i++) 
			{
				stPrefix = nsRegistry[i][0];
				stNsURI = nsRegistry[i][1];
				writer.write(stPrefix);
				writer.newLine();
				writer.write(stNsURI);
				writer.newLine();
			}
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			MOptionPanes.showError(Frame.getFrames()[0],
				"I/O error by writing NSRegistry.");
		}	
	}
	
	/**
	 * Ermittelt das Imdex von einem bestimmten Namespace
	 */
	public static int getIndexOf(String prefix, String nsURI)
	{
		int number = countNamespace();
		String[][] nsRegistry = getNSRegistry();
		for (int i = 0; i < number; i++)
		{
			if (nsRegistry[i][0].equals(prefix) && nsRegistry[i][1]
			    .equals(nsURI))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Ermittelt, ob das gegebene Namespace bereits im Registry vorhanden ist
	 * oder nicht. Falls nicht, fragt dem Benutzer mal nach, ob er dies 
	 * Namespace im System speichern will
	 * 
	 * @param prefix   das Praefix, das ueberprueft werden soll
	 * @param nsURI    das NamespaceURI, das ueberprueft werden soll
	 */
	public static void check(String prefix, String nsURI)
	{
		if ((prefix == null) && (nsURI == null))
		{
			return;
		}
		
		if (prefix.isEmpty() && nsURI.isEmpty())
		{
			return;
		}
		
		if (getIndexOf(prefix, nsURI) != -1)
		{
			return;
		} 
		else 
		{
			int ComfirmResult;
			ComfirmResult = MOptionPanes.getComfirmResult(null,
				"The namespace \n" + prefix + " : " + nsURI + 
				"\ndidn't exist in namespace-list. \nDo you want to add it" +
				" in namespace-list? ");
			if (ComfirmResult == JOptionPane.YES_OPTION) 
			{
				if (prefix == null)
				{
					prefix = "";
				}
				if (nsURI == null)
				{
					nsURI = "";
				}
				add(prefix, nsURI);
			}
		}
	}
	
	/**
	 * gibt alle Praefixe mit der bestimmten Reihenfolge zurueck
	 */
	public static Object[] getPrefixList()
	{
		int number = countNamespace();
		String[][] nsRegistry = getNSRegistry();
		String[] prefixList = new String[number];
		
		for (int i = 0; i < number; i++ )
		{
			prefixList[i] = nsRegistry[i][0];
		}
		return prefixList;
	}
	
	
	/**
	 * gibt alle NamespactURI mit der bestimmten Reihenfolge zurueck
	 */
	public static Object[] getNsURIList()
	{
		int number = countNamespace();
		String[][] nsRegistry = getNSRegistry();
		String[] nsURIList = new String[number];
		
		for (int i = 0; i < number; i++ )
		{
			nsURIList[i] = nsRegistry[i][1];
		}
		return nsURIList;
	}
	
}
