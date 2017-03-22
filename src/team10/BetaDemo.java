package team10;


import java.util.Map;
import lejos.hardware.Button;
import team10.launcher.Catapult;
import team10.localization.Localization;
import team10.navigation.Display;
import team10.navigation.Navigation;
import team10.navigation.Odometer;
import team10.wifi.WifiConnection;

// TODO Fix Odometry tile corrections
// TODO Add filtering for light sensor
/**
 * Main class for robot control
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */
public class BetaDemo {
	// WIFI
	private static final String SERVER_IP = "192.168.2.38";
	private static final int TEAM_NUMBER = 10;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// NAVIGATION
	private static final double [][] CORNERS = {{-0.0, 0.0, Odometer.getRadAngle(90.0)},{Navigation.convertTileToDistance(10), 0.0, Odometer.getRadAngle(0.0)}, {Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(10), Odometer.getRadAngle(270.0)}, {0.0, Navigation.convertTileToDistance(10), Odometer.getRadAngle(180.0)}};
	
	private static Map data;

	public static void main(String[] args) {
		int fwdTeam = 0;
		int defTeam = 0;

		// Instantiate objects
		final WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		final Odometer odometer = new Odometer();
		final Catapult catapult = new Catapult();
		final Localization localization = new Localization (odometer);
		final Navigation navigation = new Navigation(odometer);
		final Display lcdDisplay = new Display (odometer);
		
		
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
		while (fwdTeam != 10);

		// Forward
		// Get data
		int fwd_corner = ((Long) data.get("FWD_CORNER")).intValue();
		int fwd_line = ((Long) data.get("d1")).intValue();
		double [] initialPosition = CORNERS[fwd_corner-1];
		
		
		// Start odometry
		odometer.start();
		lcdDisplay.start();
		
		// Do localization
		localization.doLocalization(initialPosition);
		// Go to the forward line
		navigation.travelTo(Navigation.convertTileToDistance(5), Navigation.convertTileToDistance(fwd_line));
		
		// Wait
		Navigation.wait(1.0);
		
		// spawn a new Thread
		(new Thread() {
			public void run() {
				// Fire the catapult
				catapult.fire();
			}
		}).start();
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		catapult.disengageStabilizers();
		System.exit(0);
	}
}
