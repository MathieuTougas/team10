package team10;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import team10.localization.Localization;
import team10.navigation.Display;
import team10.navigation.Odometer;

/**
 * Main class for robot control
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class LocalizationTest {
	
	// Static Resources:
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	//public static final EV3LargeRegulatedMotor catapultMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	//public static final EV3LargeRegulatedMotor stabilizerMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S3");		
	private static final Port colorPort = LocalEV3.get().getPort("S4");	
	private static final TextLCD lcdDisplay = LocalEV3.get().getTextLCD();
	
	// ODOMETRY
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 13.4;

	public static void main(String[] args) {

		// Instantiate objects
		final Odometer odometer = new Odometer(leftMotor, rightMotor);
		//final Catapult catapult = new Catapult(catapultMotor, stabilizerMotor);
		final Localization localization = new Localization (usPort, colorPort, odometer);;
		final Display odometryDisplay = new Display (odometer, lcdDisplay);
		
		
		// Get data

		odometer.start();
		odometryDisplay.start();
		
		localization.doLocalization();
			
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}