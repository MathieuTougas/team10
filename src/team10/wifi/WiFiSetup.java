package team10.wifi;

import java.util.Map;
import lejos.hardware.Button;


public class WiFiSetup {
	/*
	 * We use System.out.println() instead of LCD printing so that full debug
	 * output (e.g. the very long string containing the transmission) can be
	 * read on the screen OR a remote console such as the EV3Control program via
	 * Bluetooth or WiFi
	 * 
	 * 
	 * 					****
	 			*** INSTRUCTIONS ***
	 			 		****
	 
	 * There are two variables each team MUST set manually below:
	 *  
	 * 1. SERVER_IP: the IP address of the computer running the server
	 * application. This will be your own laptop, until the beta beta demo or
	 * competition where this is the TA or professor's laptop. In that case, set
	 * the IP to 192.168.2.3. 
	 * 
	 * 2. TEAM_NUMBER: your project team number
	 */
	private static final String SERVER_IP = "192.168.2.9";
	private static final int TEAM_NUMBER = 10;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		System.out.println("Running..");

		// Initialize WifiConnection class
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);

		// Connect to server and get the data, catching any errors that might occur
		try {
			
			Map data = conn.getData();

			// Example 1: Print out all received data
			System.out.println("Map:\n" + data);

			// Example 2 : Print out specific values
			int fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
			System.out.println("Forward Team: " + fwdTeam);

			int w1 = ((Long) data.get("w1")).intValue();
			System.out.println("Defender zone size w1: " + w1);
			
			// Example 3: Compare value
			String orientation = (String) data.get("omega");
			if (orientation.equals("N")) {
				System.out.println("Orientation is North");
			}
			else {
				System.out.println("Orientation is not North");
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Wait until user decides to end program
		Button.waitForAnyPress();
	}
}
