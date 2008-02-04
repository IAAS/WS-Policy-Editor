
package gui.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A listener that you attach to the top-level JFrame of your application, so
 * that quitting the frame exits the application.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */

public class ExitListener extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}
}