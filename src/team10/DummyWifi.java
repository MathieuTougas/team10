package team10;

import java.util.Map;
import lejos.hardware.Button;
import team10.navigation.Navigation;
import team10.navigation.Odometer;
import team10.wifi.WifiConnection;

// TODO Add filtering for light sensor
/**
 * Main class for robot control
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */
public class DummyWifi {
	// WIFI
	private static final String SERVER_IP = "192.168.2.9";
	private static final int TEAM_NUMBER = 1;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// NAVIGATION
	private static final double [][] CORNERS = {{Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(0), 0.0},{Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(0), Odometer.getRadAngle(90.0)}, {Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(10), Odometer.getRadAngle(180.0)}, {Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(10), Odometer.getRadAngle(270.0)}};
	
	private static Map data;

	public static void main(String[] args) {
		int fwdTeam = 0;
		int defTeam = 0;

		// Instantiate objects
		final WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		
		
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
		
		while (fwdTeam != TEAM_NUMBER && defTeam != TEAM_NUMBER );
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
