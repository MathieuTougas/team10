package team10;

import lejos.hardware.Button;
import team10.localization.Localization;
import team10.navigation.Display;
import team10.navigation.Odometer;

/**
 * Localisation testing class
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */
public class LocalizationTest {

	public static void main(String[] args) {

		// Instantiate objects
		final Odometer odometer = new Odometer();
		final Localization localization = new Localization (odometer);
		final Display lcdDisplay = new Display (odometer);
		
		
		// Get data

		odometer.start();
		lcdDisplay.start();
		
		localization.doLocalization();
			
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}