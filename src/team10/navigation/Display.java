package team10.navigation;

import team10.localization.LightLocalizer;
import team10.localization.USLocalizer;
import lejos.hardware.lcd.TextLCD;

/**
 * Handles robot display
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class Display extends Thread {
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odometer;
	private TextLCD lcd;

	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public Display(Odometer odometer, TextLCD lcd) {
		this.odometer = odometer;
		this.lcd = lcd;
	}

	/**
	 *  Thread updating the display
	 * 
	 *  @since 1.0
	 */
	public void run() {
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display once
		lcd.clear();

		while (true) {
			displayStart = System.currentTimeMillis();

			// get the odometry information
			odometer.getPosition(position, new boolean[] { true, true, true });
			

			
			lcd.drawString("Data", 0, 0);
			lcd.drawString("X: ", 0, 1);
			lcd.drawString("Y: ", 0, 2);
			lcd.drawString("TH: ", 0, 3);
			lcd.drawString("Calc", 0, 4);
			lcd.drawString("A: ", 0, 5);
			lcd.drawString("B: ", 0, 6);
			lcd.drawString("T: ", 0, 7);
			lcd.drawString("Sensors", 8, 0);
			lcd.drawString("Us: ", 8, 1);
			lcd.drawString("Cs: ", 8, 2);
			lcd.drawString("CalX: ", 8, 3);
			lcd.drawString("CalY: ", 8, 4);
			lcd.drawInt((int)(position[0] * 10), 3, 1);
			lcd.drawInt((int)(position[1] * 10), 3, 2);
			lcd.drawString(formattedDoubleToString(position[2], 2), 3, 3);
			lcd.drawString(formattedDoubleToString(USLocalizer.angleA, 2), 3, 5);
			lcd.drawString(formattedDoubleToString(USLocalizer.angleB, 2), 3, 6);
			lcd.drawInt((int)USLocalizer.theta, 3, 7);
			lcd.drawInt((int)USLocalizer.distance, 15, 1);
			lcd.drawInt((int)LightLocalizer.color, 15, 2);
			lcd.drawInt((int)LightLocalizer.locX, 15, 3);
			lcd.drawInt((int)LightLocalizer.locY, 15, 4);

			// throttle the OdometryDisplay
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that OdometryDisplay will be interrupted
					// by another thread
				}
			}
		}
	}
	
	/**
	 *  Formats a double input as a string
	 * 
	 *  @since 1.0
	 */
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}

}
