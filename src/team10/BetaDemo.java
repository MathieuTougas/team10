package team10;


import java.util.Map;
import lejos.hardware.Button;
import team10.launcher.StringLauncher;
import team10.localization.Localization;
import team10.navigation.Display;
import team10.navigation.Navigation;
import team10.navigation.Odometer;
import team10.wifi.WifiConnection;

// TODO Add filtering for light sensor
/**
 * Beta demo running class
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */
public class BetaDemo {
	// WIFI
	private static final String SERVER_IP = "192.168.2.3";
	private static final int TEAM_NUMBER = 10;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// NAVIGATION
	private static final double [][] CORNERS = {{-0.0, 0.0, 0.0},{Navigation.convertTileToDistance(6), 0.0, Odometer.getRadAngle(90.0)}, {Navigation.convertTileToDistance(6), Navigation.convertTileToDistance(6), Odometer.getRadAngle(180.0)}, {0.0, Navigation.convertTileToDistance(6), Odometer.getRadAngle(270.0)}};

	public static void main(String[] args) {
		int fwdTeam = 0;
		int defTeam = 0;

		// Instantiate objects
		final WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		final Odometer odometer = new Odometer();
		final Navigation navigation = new Navigation(odometer);
		final StringLauncher stringLauncher = new StringLauncher();
		final Localization localization = new Localization (odometer, navigation);
		
		final Display lcdDisplay = new Display (odometer);
		
		
		// Get data
		try {
			// Get data
			@SuppressWarnings("rawtypes")
			Map data = conn.getData();

			// Get team numbers
			fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
			
			lcdDisplay.clear();
			// Wait for our team number to be called
			while (fwdTeam != TEAM_NUMBER);

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
			
			navigation.travelTo(Navigation.convertTileToDistance(5), Navigation.convertTileToDistance(0));
			navigation.turnTo(180, true);
			
			// Wait
			Navigation.wait(1.0);
			
			// Fire the ball
			stringLauncher.fire();
			
			while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
