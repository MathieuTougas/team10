package team10;

import lejos.robotics.SampleProvider;

/**
 * Handles the light localization for the robot
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private final double BLACK_LINE = 40.0;
	public static float color;
	public double locX, locY;
	private float[] colorData;	
	private Navigation navigation;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		navigation = new Navigation (odo);
	}
	
	/**
	 *  Do the localization routine
	 * 
	 *  @since 1.0
	 */
	public void doLocalization() {
		odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		locX = 0;
		locY = 0;
		
		// Get the x-axis value for the line, back up to original position
		runUntilLine();
		locX = odo.getX();
		backOff(0, 'x');
		
		// Turn to 90 and run
		navigation.turnTo(90, true);
		
		// Get the y-axis value for the line
		runUntilLine();
		locY = odo.getY();
		
		// Travel to the zero-zero point
		navigation.travelTo(locX-15, locY-15);
		navigation.turnTo(0, true);
		
		// Sets the odometer to (0,0);
		odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
	}
	
	/**
	 *  Run the robot forward until it reaches a line
	 * 
	 *  @since 1.0
	 */
	private void runUntilLine(){
		navigation.setSpeeds(50,50);
		color = getColorData();
		
		while (color > BLACK_LINE){
			color = getColorData();
		}
		navigation.setSpeeds(0,0);
	}
	
	/**
	 *  Back the robot off by distance and axis
	 * 
	 *  @since 1.0
	 */
	private void backOff(int point, char axis){
		navigation.setSpeeds(-50,-50);
		if (axis == 'x'){
			double location = odo.getX();
			while ((int) location != point){
				location = odo.getX();
			}
		}
		else if (axis == 'x'){
			double location = odo.getY();
			while ((int) location != point){
				location = odo.getY();
			}
		}
		else {
			navigation.goForward(point);
		}
		navigation.setSpeeds(0,0);
	}
	
	/**
	 *  Get data from the color sensor
	 * 
	 *  @since 1.0
	 */
	private float getColorData() {
		colorSensor.fetchSample(colorData, 0);
		float color = colorData[0]*100;

		return color;
	}

}
