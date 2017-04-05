package team10;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import team10.launcher.StringLauncher;
import team10.localization.Localization;
//import team10.navigation.Display;
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
	
	public static void FinalMain(String[] args) {

		// Instantiate objects
		final Odometer odometer = new Odometer();
		final Navigation navigation = new Navigation(odometer);
		final Localization localization = new Localization (odometer, navigation);
		//final Display lcdDisplay = new Display (odometer);
		final StringLauncher stringLauncher = new StringLauncher();
		
		int fwd_corner = 1;
		
		// Get data
		double [] initialPosition = CORNERS[fwd_corner-1];
		
		// Start odometry
		odometer.start();
		//lcdDisplay.start();
		
		// Do localization
		//localization.doLocalization(initialPosition);
		//odometer.setPosition(new double [] {Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(0), 0}, new boolean [] {true, true, true});
		//navigation.travelTo(Navigation.convertTileToDistance(2), Navigation.convertTileToDistance(2));
		navigation.turn(Math.PI);
		navigation.turn(Math.PI);
		/*for(int i=0; i<4; i++){
			stringLauncher.lowerCatapult();
			navigation.travelTo(Navigation.convertTileToDistance(2), Navigation.convertTileToDistance(0));
			navigation.travelTo(Navigation.convertTileToDistance(2), 20);
			Sound.beep();

			Navigation.wait(5.0);
			navigation.goForward(-10);
			navigation.travelTo(Navigation.convertTileToDistance(4), Navigation.convertTileToDistance(1));
			navigation.turn(Math.PI/2 - odometer.getTheta());
			navigation.turn(Math.PI);
			//localization.correctBeforeShort();
			stringLauncher.fire();
		}*/
		
		//navigation.turnTo(180, true);
		//navigation.goForward(30.48*3);
		//navigation.turn(Math.PI/4);
		/*int x = 1;
		while (x == 1) {
			while (Button.waitForAnyPress() != Button.ID_ENTER);
			stringLauncher.fire();
		}*/
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}