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
	public static final EV3LargeRegulatedMotor catapultMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor stabilizerMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");	
	private static final TextLCD lcdDisplay = LocalEV3.get().getTextLCD();

	// WIFI
	private static final String SERVER_IP = "192.168.2.38";
	private static final int TEAM_NUMBER = 10;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// ODOMETRY
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 15.7;
	
	// NAVIGATION
	private static final double [][] TARGETS = {{-30.0, 90.0},{0, 90.0}, {30.0, 90.0}};
	
	private static Map data;

	public static void main(String[] args) {
		int fwdTeam = 0;
		int defTeam = 0;

		// Instantiate objects
		final WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		final Odometer odometer = new Odometer(leftMotor, rightMotor);
		final Catapult catapult = new Catapult(catapultMotor, stabilizerMotor);
		final Localization localization = new Localization (usPort, colorPort, odometer);
		final Display odometryDisplay = new Display (odometer, lcdDisplay);
		
		
		// Get data
		try {
			// Get data
			@SuppressWarnings("rawtypes")
			Map data = conn.getData();

			// Get team numbers
			fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
			defTeam = ((Long) data.get("DEF_TEAM")).intValue();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		do {
			// Clear display
			lcdDisplay.clear();
			
		} 
		// Wait for our team number to be called
		while (fwdTeam != 10 && defTeam != 10);

		// Forward
		if (fwdTeam == 10) {
			// Get data
			int fwd_corner = ((Long) data.get("FWD_CORNER")).intValue();
			int fwd_line = ((Long) data.get("d1")).intValue();
			int disp_x = ((Long) data.get("bx")).intValue();
			int disp_y = ((Long) data.get("by")).intValue();
			String disp_orientation = (String) data.get("omega");
			
			

			odometer.start();
			odometryDisplay.start();
			
			localization.doLocalization();
			
		} 
		
		// Defender
		else if (defTeam == 10){
			// Get data
			int def_corner = ((Long) data.get("DEF_CORNER")).intValue();
			int def_zone_x = ((Long) data.get("w1")).intValue();
			int def_zone_y = ((Long) data.get("w2")).intValue();

			
			odometer.start();
			odometryDisplay.start();

			// spawn a new Thread
			(new Thread() {
				public void run() {
					// Fire the catapult
					catapult.fire(TARGETS);
				}
			}).start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		catapult.disengageStabilizers();
		System.exit(0);
	}
}