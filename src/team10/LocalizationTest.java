package team10;

import lejos.hardware.Button;
import team10.launcher.StringLauncher;
import team10.localization.Localization;
import team10.navigation.Display;
import team10.navigation.Navigation;
import team10.navigation.Odometer;


/**
 * Localisation testing class
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */
public class LocalizationTest {

	private static final double [][] CORNERS = {{Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(0), 0.0},{Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(0), Odometer.getRadAngle(90.0)}, {Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(10), Odometer.getRadAngle(180.0)}, {Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(10), Odometer.getRadAngle(270.0)}};
	
	public static void main(String[] args) {

		// Instantiate objects
		final Odometer odometer = new Odometer();
		final Localization localization = new Localization (odometer);
		final Display lcdDisplay = new Display (odometer);
		final Navigation navigation = new Navigation(odometer);
		final StringLauncher stringLauncher = new StringLauncher();
		
		int fwd_corner = 1;
		
		// Get data
		double [] initialPosition = CORNERS[fwd_corner-1];
		
		// Start odometry
		odometer.start();
		lcdDisplay.start();
		
		// Do localization
		localization.doLocalization(initialPosition);
		//odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
		navigation.travelTo(Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(2));
		//navigation.turnTo(180, true);
		//navigation.goForward(30.48*3);
		//navigation.turn(Math.PI*2);
		int x = 1;
		while (x == 1) {
			while (Button.waitForAnyPress() != Button.ID_ENTER);
			stringLauncher.fire();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}