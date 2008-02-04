package gui.util;

import gui.editor.AssertionEditor;
import gui.editor.PolicyEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Diese Klasse enthaelt alle benoetigte Methode, mit denen
 * die Position des Hauptfensters bestimmt werden kann
 *  
 * @author      Zhilei Ma
 * @version     1.0
 */
public class GUIUtil {

	/* How much space of the screen should the window get */
	private static final float proportion = (float) 4 / 5;

	/** 
	 * Get the owner component for JOptionPane to diaplay messages to users 
	 * 
	 */
	public static Component getEditor(Component component) {

		if (!(component.getParent() instanceof AssertionEditor)
				&& !(component.getParent() instanceof PolicyEditor)) {
			component = getEditor(component.getParent());
		}
		
		return component;
	}


	/**
	 * Get the current screen resolution for positioning and setting size of 
	 * the frames
	 */
	public static Dimension getScreenResoulution() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	/**
	 * Breite des Bildschirms
	 * 
	 * @see   #getScreenHeight()
	 */
	public static int getScreenWidth() {
		return getScreenResoulution().width;
	}

	/**
	 *  Hoehe des Bildschirms
	 *
	 *  @see   #getScreenWidth()
	 */
	public static int getScreenHeight() {
		return getScreenResoulution().height;
	}

	/**
	 * 
	 * die defaulte Hoehe des Fensters
	 * 
	 * @see   #getDefaultWindowWidth()
	 */
	public static int getDefaultWindowHeight() {
		return Math.round(getScreenHeight() * proportion);
	}

	/**
	 * die defaulte Breite des Fensters
	 * 
	 * @see   #getDefaultWindowHeight()
	 */
	public static int getDefaultWindowWidth() {
		return Math.round(getScreenWidth() * proportion);
	}

	/**
	 * die X-Koordinate des Startpunkts des Fensters
	 * 
	 * @param windowWidth   Breite des Fensters
	 * @see                 #getStartPositionY(int)
	 */
	public static int getStartPositionX(int windowWidth) {
		float result = windowWidth > getScreenWidth() ? 
				((windowWidth - getScreenWidth()) / 2)
				: ((getScreenWidth() - windowWidth) / 2);
		return Math.round(result);
	}
	
	/**
	 * die Y-Koordinate des Startpunkts des Fensters
	 * 
	 * @param windowHeight   Hoehe des Fensters
	 * @see                  #getStartPositionX(int)
	 */
	public static int getStartPositionY(int windowHeight) {
		float result = windowHeight > getScreenHeight() ? 
				((windowHeight - getScreenHeight()) / 2)
				: ((getScreenHeight() - windowHeight) / 2);
		return Math.round(result);
	}

}
