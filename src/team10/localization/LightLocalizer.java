package team10.localization;

import lejos.robotics.SampleProvider;
import team10.navigation.Navigation;
import team10.navigation.Odometer;

/**
 * Handles the light localization for the robot
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */

public class LightLocalizer {
	private Odometer odometer;
	private Navigation navigation;
	private float forwardSpeed;
	private SampleProvider leftColorSensor;
	private SampleProvider rightColorSensor;
	private float[] leftColorData;	
	private float[] rightColorData;	
	
	private final double BLACK_LINE = 40.0;
	private final double OFFSET_X = 6.5;
	private final double OFFSET_Y = 6.5;
	private final double SENSOR_TRACK = 11.6;
	private final double SENSOR_OFFSET = 5.35;

	public static float leftColor, rightColor;
	public static double locX;
	public static double locY;
	
	
	
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public LightLocalizer(Odometer odometer, Navigation navigation, SampleProvider leftColorSensor, float[] leftColorData, SampleProvider rightColorSensor, float[] rightColorData) {
		this.odometer = odometer;
		this.leftColorSensor = leftColorSensor;
		this.rightColorSensor = rightColorSensor;
		this.leftColorData = leftColorData;
		this.rightColorData = rightColorData;
		this.forwardSpeed = Navigation.getForwardSpeed();
		this.navigation = navigation;
		
	}
	
	/**
	 *  Do the localization routine
	 * 
	 *  @since 1.0
	 */
	public void doLocalization() {
		odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
		locX = 0;
		locY = 0;
		
		// Get the x-axis value for the line, back up to original position
		locX = odometer.getX() - OFFSET_X;
		backOff(0.0, 'x');
		
		// Turn to 90 and run
		navigation.turnTo(90, true);
		
		// Get the y-axis value for the line
		locY = odometer.getY() - OFFSET_Y;
		
		// Travel to the zero-zero point
		navigation.travelTo(locX, locY);
		navigation.turnTo(0, true);
		
		// Sets the odometer to (0,0);
		odometer.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
	}
	
	/**
	 *  Do the localization routine, overriding with initial position
	 * 
	 *  @param initialPosition (x, y, theta)
	 *  @since 2.0
	 */
	public void doLocalization(double[] initialPosition) {
		odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
		locX = 0;
		locY = 0;
		
		// Get the x-axis value for the line, back up to original position
		runUntilLine("X");
		// Turn to 90 and run
		odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
		navigation.turnTo(90, true);
		
		// Get the y-axis value for the line
		runUntilLine("Y");
		
		// Travel to the zero-zero point
		navigation.turnTo(0, true);
		
		// Sets the odometer to (0,0);
		odometer.setPosition(initialPosition, new boolean [] {true, true, true});
	}
	
	/**
	 *  Run the robot forward until it reaches a line
	 * 
	 *  @since 2.0
	 */
	private void runUntilLine(String axis){
		double[] offsets = new double[2];
		navigation.setSpeeds(forwardSpeed,forwardSpeed);
		leftColor = getLeftColorData();
		rightColor = getRightColorData();
		
		boolean leftPassed = false;
		boolean rightPassed = false;
		if (axis.equals("X")){
			while (leftPassed == false || rightPassed == false){
				leftColor = getLeftColorData();
				rightColor = getRightColorData();
				if (leftColor < BLACK_LINE){
					offsets[0] = odometer.getX();
					leftPassed = true;
				}
				else if (rightColor < BLACK_LINE){
					offsets[1] = odometer.getX();
					rightPassed = true;
				}
			}
		}
		else {
			while (leftPassed == false || rightPassed == false){
				leftColor = getLeftColorData();
				rightColor = getRightColorData();
				if (leftColor < BLACK_LINE){
					offsets[0] = odometer.getY();
					leftPassed = true;
				}
				else if (rightColor < BLACK_LINE){
					offsets[1] = odometer.getY();
					rightPassed = true;
				}
			}
		}
		correctPosition(offsets, axis);
		navigation.setSpeeds(0,0);
	}
	
	/**
	 *  Correct the robot position
	 * 
	 *  @since 2.0
	 */
	private void correctPosition(double[] positions, String axis){
		double leftValue = positions[0];
		double rightValue = positions[1];
		
		double diff = leftValue-rightValue;
		double angle = Math.asin(diff/SENSOR_TRACK);
		if (axis.equals("X")){
			navigation.turn(-angle);
			navigation.goForward(SENSOR_OFFSET-Math.abs(Odometer.getWheelBase()/2*Math.sin(angle)));
		}
		else{
			navigation.goForward(SENSOR_OFFSET);
		}
	}
	
	/**
	 *  Back the robot off by distance and axis
	 * 
	 *  @since 1.0
	 */
	private void backOff(double point, char axis){
		navigation.setSpeeds(-forwardSpeed,-forwardSpeed);
		if (axis == 'x'){
			double location = odometer.getX();
			while ((int) location != point){
				location = odometer.getX();
			}
		}
		else if (axis == 'y'){
			double location = odometer.getY();
			while ((int) location != point){
				location = odometer.getY();
			}
		}
		else {
			navigation.goForward(point);
		}
		navigation.setSpeeds(0,0);
	}
	
	/**
	 *  Get data from the left color sensor
	 * 
	 *  @since 2.0
	 */
	private float getLeftColorData() {
		leftColorSensor.fetchSample(leftColorData, 0);
		float color = leftColorData[0]*100;
		return color;
	}
	
	/**
	 *  Get data from the rightcolor sensor
	 * 
	 *  @since 1.0
	 */
	private float getRightColorData() {
		rightColorSensor.fetchSample(rightColorData, 0);
		float color = rightColorData[0]*100;
		return color;
	}
	

}
