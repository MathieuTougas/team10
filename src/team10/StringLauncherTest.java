package team10;

import lejos.hardware.Button;
import team10.launcher.StringLauncher;


/**
 * Launcher testing class
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */
public class StringLauncherTest {
	
	public static void main(String[] args) {

		// Instantiate objects
		final StringLauncher stringLauncher = new StringLauncher();
		
		
		// Get data

		stringLauncher.fire();
			
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
	

}
