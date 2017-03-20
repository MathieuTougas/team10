package team10;

import java.util.Map;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import team10.launcher.Catapult;
import team10.localization.Localization;
import team10.navigation.Display;
import team10.navigation.Navigation;
import team10.navigation.Odometer;
import team10.wifi.WifiConnection;

/**
 * Main class for robot control
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class Main {
	
	// Static Resources:
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	//public static final EV3LargeRegulatedMotor catapultMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	//public static final EV3LargeRegulatedMotor stabilizerMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S3");		
	private static final Port colorPort = LocalEV3.get().getPort("S4");	
	private static final TextLCD lcdDisplay = LocalEV3.get().getTextLCD();

	// WIFI
	private static final String SERVER_IP = "192.168.2.38";
	private static final int TEAM_NUMBER = 10;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// ODOMETRY
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 13.4;
	
	// NAVIGATION
	private static final double [][] TARGETS = {{-30.0, 90.0},{0, 90.0}, {30.0, 90.0}};
	
	private static Map data;

	public static void main(String[] args) {
		int fwdTeam = 0;
		int defTeam = 0;

		// Instantiate objects
		final WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		final Odometer odometer = new Odometer(leftMotor, rightMotor);
		//final Catapult catapult = new Catapult(catapultMotor, stabilizerMotor);
		final Localization localization = new Localization (usPort, colorPort, odometer);
		final Navigation navigation = new Navigation(odometer);
		final Display odometryDisplay = new Display (odometer, lcdDisplay);
		
		
		// Get data

		odometer.start();
		odometryDisplay.start();
		
		localization.doLocalization();
			
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}