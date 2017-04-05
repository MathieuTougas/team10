package team10;

import java.util.Map;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import team10.launcher.StringLauncher;
import team10.localization.Localization;
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
public class Main {
	// WIFI
	private static final String SERVER_IP = "192.168.2.38";
	private static final int TEAM_NUMBER = 10;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// NAVIGATION
	private static final double [][] CORNERS = {{Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(0), 0.0},{Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(0), Odometer.getRadAngle(90.0)}, {Navigation.convertTileToDistance(10), Navigation.convertTileToDistance(10), Odometer.getRadAngle(180.0)}, {Navigation.convertTileToDistance(0), Navigation.convertTileToDistance(10), Odometer.getRadAngle(270.0)}};
	
	private static Map data;

	public static void main(String[] args) {
		int fwdTeam = 0;
		int defTeam = 0;

		// Instantiate objects
		final WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		final Odometer odometer = new Odometer();
		final Navigation navigation = new Navigation(odometer);
		final StringLauncher stringLauncher = new StringLauncher();
		final Localization localization = new Localization (odometer, navigation);
		//final Display lcdDisplay = new Display (odometer);
		
		
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
			//lcdDisplay.clear();
			
		} 
		// Wait for our team number to be called
		while (fwdTeam != TEAM_NUMBER && defTeam != TEAM_NUMBER);

		// Forward
		if (fwdTeam == TEAM_NUMBER) {
			// Get data
			int fwd_corner = ((Long) data.get("FWD_CORNER")).intValue();
			int fwd_line = ((Long) data.get("d1")).intValue();
			int disp_x = ((Long) data.get("bx")).intValue();
			int disp_y = ((Long) data.get("by")).intValue();
			String disp_orientation = (String) data.get("omega");
			double [] initialPosition = CORNERS[fwd_corner-1];
			int i = 0;
			
			int xDest = disp_x;
			int yDest = disp_y;
			
			
			// Start odometry
			odometer.start();
			//lcdDisplay.start();
			
			// Do localization
			localization.doLocalization(initialPosition);
			
			// Go to Ball dispenser
			switch (disp_orientation){
			case "N":
				yDest += 1;
				break;
			case "E":
				yDest += 1;
				break;
			case "S":
				yDest -= 1;
				break;
			case "W":
				yDest -= 1;
				break;	
			}
			
			// Go the the middle of the field
			navigation.travelTo(Navigation.convertTileToDistance(5), Navigation.convertTileToDistance(5));
			
			// Shooting loop
			while (i < 1){
				// Lower Catapult
				stringLauncher.lowerCatapult();
				
				// Go in front of the ball dispenser
				navigation.travelTo(Navigation.convertTileToDistance(xDest), Navigation.convertTileToDistance(yDest));
				
				// Go to the ball dispenser
				if (disp_orientation.equals("N") || disp_orientation.equals("S")){
					navigation.travelTo(Navigation.convertTileToDistance(disp_x), Navigation.convertTileToDistance(disp_y)-10);
				}
				else {
					navigation.travelTo(Navigation.convertTileToDistance(disp_x)-10, Navigation.convertTileToDistance(disp_y));

				}
				// Beep to obtain ball
				Sound.beep();
				Navigation.wait(5.0);
				
				// Back off and go to shooting line
				navigation.goForward(-10);
				navigation.travelTo(Navigation.convertTileToDistance(5), Navigation.convertTileToDistance(7));
				navigation.turn(Math.PI/2 - odometer.getTheta());
				navigation.turn(Math.PI);
				localization.correctBeforeShort();
				stringLauncher.fire();
			}

		}
		
		// Defender
		else if (defTeam == TEAM_NUMBER){
			// Get data
			int def_corner = ((Long) data.get("DEF_CORNER")).intValue();
			int def_zone_x = ((Long) data.get("w1")).intValue();
			int def_zone_y = ((Long) data.get("w2")).intValue();
			double [] initialPosition = CORNERS[def_corner-1];

			
			odometer.start();
			//lcdDisplay.start();
			
			// Do localization
			localization.doLocalization(initialPosition);
			
			// Go the the middle of the field
			navigation.travelTo(Navigation.convertTileToDistance(5), Navigation.convertTileToDistance(5));
			
			// Go in front of the ball dispenser
			navigation.travelTo(Navigation.convertTileToDistance(5), Navigation.convertTileToDistance(def_zone_y));
		} 
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
