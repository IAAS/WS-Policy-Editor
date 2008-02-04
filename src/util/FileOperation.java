package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Diese Klasse enthaelt alle nutzliche Methoden, mit denen einige Oprationen 
 * auf einer Datei ausgefuert werden koennen.
 * 
 * @author      Zhilei Ma
 * @version     1.0 
 *
 */
public class FileOperation {			
		
	private String currentRecord = null;	
	
	private String path;
	
	private BufferedReader file;	
				
	public  FileOperation() {	
	}
		
	/**
	 * Liest eine Datei ein.
	 * 
	 * @param filePath   der Pfad der Datei
	 * @return           die erste Zeile der Datei
	 * @throws FileNotFoundException
	 */
	public String readFile(String filePath) throws FileNotFoundException {
		path = filePath;		
	    file = new BufferedReader(new FileReader(path));
	    String returnStr =null;
	    try	{
		    currentRecord = file.readLine();
		} catch (IOException e) {
		    System.out.println("Read error.");
		}
		if (currentRecord == null)
		    returnStr = null;
		else {
		    returnStr = currentRecord;
		}		    
		return returnStr;
	}	
	
	/**
	 * Schreibt den bestimmten Inhalt in einer Datei auf.
	 * 
	 * @param filePath      der Pfad der Datei
	 * @param tempcon       der Inhalt, der in dieser Datei aufgeschrieben 
	 *                      werden soll
	 * @throws FileNotFoundException
	 */
	public void writeFile(String filePath, String tempcon) 
		throws FileNotFoundException
	{
	    try {
	        PrintWriter pw = new PrintWriter(new FileOutputStream(filePath));
       	    pw.println(tempcon);
			pw.close();
	    } catch(IOException e) {
	        System.out.println("Write error." + e.getMessage());
	    }
	}
	
	/**
	 * Kopiert den Inhalt einer Datei zu einer anderen Datei
	 * 
	 * @param oldPath   der Pfad der originalen Datei
	 * @param newPath   der Pfad der anderen Datei
	 */
	public void copyFile(String oldPath, String newPath)  {  
        try  {  
            int  bytesum  = 0;  
            int  byteread = -1;  
            File oldFile = new File(oldPath);  
            if (oldFile.exists())  {   
                int index = oldPath.lastIndexOf(File.separatorChar);
                String newDir = newPath + oldPath.substring(index);	       
                FileInputStream  inStream  = new FileInputStream(oldPath); 
                FileOutputStream outStream = new FileOutputStream(newDir);  
                byte[] buffer = new byte[(int) oldFile.length()];  
                while ((byteread = inStream.read(buffer)) != -1) {  
                    bytesum += byteread;
                    outStream.write(buffer, 0, byteread);  
                }  
                inStream.close();  
                outStream.flush();  
                outStream.close();  
            }  
        } catch  (Exception  e)  {  
            System.out.println("copy file error");  
            e.printStackTrace();  	 
       }  	 
    }
    
	/**
	 * Kopiert alle Dateien unter einem Ordner zu einem anderen Ordner
	 * 
	 * @param oldPath   der Pfad des originalen Ordners
	 * @param newPath   der Pfad des anderen Ordners
	 */
	public void copyFolder(String oldPath, String newPath)  {  			 
	    try  {  
	        File aFile = new File(oldPath); 
	        
	        if (!aFile.isDirectory()) {
	        	copyFile(oldPath, newPath);
	        } else {
	        	String fromPath = oldPath;
	        	String toPath   = newPath;
	        	toPath = toPath + File.separator + aFile.getName();
	        	if ((new File(toPath)).mkdirs()) {
	        		File[] files = aFile.listFiles();
	        		for (int i = 0; i < files.length; i++) { 
	        			fromPath = oldPath + File.separator + files[i].getName();            			
	        			copyFolder(fromPath, toPath);	
	        		}
	        	}	
	        } 
	    } catch  (Exception  e)  {  
	        System.out.println("copy folder error.");  
	        e.printStackTrace();  		 
	    }  
	} 
	
}

