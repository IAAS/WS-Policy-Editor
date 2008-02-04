package util;

import java.io.File;
import java.io.IOException;

/**
 * Diese Klasse bietet alle nutzliche Methoden, mit denen der WS-Policy Editor 
 * unter verschiedenen Betriebssysteme laufen kann.
 * 
 * @author      Windy
 * @version     1.0
 *
 */
public class Environment 
{
    
	private static boolean isWindows()
	{	    	
		String OS = System.getProperty("os.name").toLowerCase();	   
		if ((OS.indexOf("windows 9") > -1) 
			|| (OS.indexOf("nt") > -1) 
			|| (OS.indexOf("windows 20") > -1) 
			|| (OS.indexOf("windows xp") > -1) 
			|| (OS.indexOf("windows vi") > -1)) 
			/* For Windows Vista */
			return true;
		else
			return false;
	}
    
	/**
	 * Der Ordner, in dem alle nutzliche Data von Editor gespeichert werden.
	 */
	public static String getEditorDataFolder()
	{
		if (isWindows())
		{
			return System.getenv("appdata") + File.separator + "ws-policy";
		} 
		else 
		{
			return System.getProperty("user.home") + "//ws-policy//";
		}
	}
    
	/**
	 * die Datei, in der den PolicyWorkspacePath gespeichert wird.
	 */
	public static String getPolicyWorkspacePathFile()
	{
		if (isWindows())
		{
			return getEditorDataFolder() + File.separator + "policyFilePath.txt";
		} 
		else 
		{
			return getEditorDataFolder() + "//policyFilePath.txt";
		}
	}
    
	/**
	 * die Datei, in der den AssertionWorkspacePath gespeichert wird.
	 */
	public static String getAssertionWorkspacePathFile()
	{
		if (isWindows())
		{
			return getEditorDataFolder() + File.separator + 
			"assertionFilePath.txt";
		} 
		else 
		{
			return getEditorDataFolder() + "//assertionFilePath.txt";
		}
	}
 
	/**
	 * die Datei, in der alle Namespaces gespeichert werden
	 *
	 */
	public static String getNSRegistryDataFile()
	{
		if (isWindows())
		{
			return getEditorDataFolder() + File.separator + ".NSRegistry";
		} 
		else 
		{
			return getEditorDataFolder() + "//.NSRegistry";
		}
	}   
    
	/**
	 * Ueberprueft, ob der Ordner, in dem alle nutzliche Data von Editor
	 * gespeichert werden, im System vorhanden ist. Falls nein, legt einen an.
	 * 
	 * @see   #checkWorkspacePathFile()
	 *
	 */
	public static void checkEditorDataFolder()
	{
		File editorDataFolder = new File (getEditorDataFolder());
		if (!editorDataFolder.exists())
		{
			editorDataFolder.mkdirs();
		}	
	}
    
	/**
	 * Ueberprueft, ob die Dateien, in denen der Pfad der jeweiligen Workspace 
	 * gespeichert wird, im System existieren. Falls nein, legt beide an.
	 *
	 *@see    #checkEditorDataFolder()
	 */
	public static void checkWorkspacePathFile()
	{
		checkEditorDataFolder();
		
		File policyFile = new File (getPolicyWorkspacePathFile());
		if (!policyFile.exists())
		{
			try 
			{
				policyFile.createNewFile();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		File asssertionFile = new File (getAssertionWorkspacePathFile());
		if (!asssertionFile.exists())
		{
			try 
			{
				asssertionFile.createNewFile();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
    
}
