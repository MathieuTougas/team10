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

/**
 * Main class for robot control
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */
public class Main {
	// WIFI
	private static final String SERVER_IP = "192.168.2.38";
	private static final int TEAM_NUMBER = 10;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// NAVIGATION
	private static final double [][] CORNERS = {{-0.0, 0.0, Odometer.getRadAngle(90.0)},{10.0, 0.0, Odometer.getRadAngle(0.0)}, {10.0, 10.0, Odometer.getRadAngle(270.0)}, {0.0, 10.0, Odometer.getRadAngle(180.0)}};
	
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
		while (fwdTeam != 10 && defTeam != 10);

		// Forward
		if (fwdTeam == 10) {
			// Get data
			int fwd_corner = ((Long) data.get("FWD_CORNER")).intValue();
			int fwd_line = ((Long) data.get("d1")).intValue();
			int disp_x = ((Long) data.get("bx")).intValue();
			int disp_y = ((Long) data.get("by")).intValue();
			String disp_orientation = (String) data.get("omega");
			double [] initialPosition = CORNERS[fwd_corner-1];
			
			int xDest = disp_x;
			int yDest = disp_y;
			
			
			// Start odometry
			odometer.start();
			lcdDisplay.start();
			
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
			
			// Go in front of the ball dispenser
			navigation.travelTo(convertTileToDistance(xDest), convertTileToDistance(yDest));
			
			// Go to the ball dispenser
			navigation.travelTo(convertTileToDistance(disp_x), convertTileToDistance(disp_y));
			
			// Wait
			wait(1.0);
			
			// Go to the ball dispenser
			navigation.travelTo(convertTileToDistance(5), convertTileToDistance(fwd_line));
			
			// spawn a new Thread
			(new Thread() {
				public void run() {
					// Fire the catapult
					catapult.fire();
				}
			}).start();
		}
		
		// Defender
		else if (defTeam == 10){
			// Get data
			int def_corner = ((Long) data.get("DEF_CORNER")).intValue();
			int def_zone_x = ((Long) data.get("w1")).intValue();
			int def_zone_y = ((Long) data.get("w2")).intValue();
			double [] initialPosition = CORNERS[def_corner-1];

			
			odometer.start();
			lcdDisplay.start();

		} 
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		catapult.disengageStabilizers();
		System.exit(0);
	}
	
	/**
	 *  Wait a determined amount of time
	 * 
	 *  @since 1.0
	 */
	private static void wait(double seconds){
		try {
			Thread.sleep((long) (seconds*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  Wait a determined amount of time
	 * 
	 *  @since 1.0
	 */
	private static double convertTileToDistance(int tile){
		return tile*30.98;
	}
}
