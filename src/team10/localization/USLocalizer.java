package team10.localization;

import lejos.robotics.SampleProvider;
import team10.navigation.Navigation;
import team10.navigation.Odometer;

/**
 * Handles the ultrasonic sensor localization routine
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static float distance;
	public static double angleA, angleB, theta;
	private final int BANDWIDTH = 5;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private float bandCenter = 50;
    private float distError, turnSpeed;
	
	/**
	 * Constructor method
	 * 
	 * @since 1.0
	 */
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
	    this.locType = locType;
	    this.turnSpeed = Navigation.ROTATE_SPEED;
	}
	
	/**
	 * Does the ev3 localisation routine
	 * 
	 * @since 1.0
	 */
	public void doLocalization() {
		angleA = 0; 
		angleB = 0;
		theta = 0;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			Navigation navigation = new Navigation (odo);
			navigation.setSpeeds(turnSpeed,-turnSpeed);

			// rotate the robot until it sees no wall
			distance = getFilteredData();
			distError = bandCenter - distance;
			if (distError > BANDWIDTH) {
				navigation.turn(-45);
			}
			navigation.setSpeeds(turnSpeed,-turnSpeed);
			turnUntilNoWall();
			// keep rotating until the robot sees a wall, then latch the angle
			turnUntilWall();
			navigation.setSpeeds(0,0);
			angleA = odo.getTheta()*180/Math.PI;
			
			// switch direction and wait until it sees no wall
			navigation.setSpeeds(-turnSpeed,turnSpeed);
			turnUntilNoWall();
			
			// keep rotating until the robot sees a wall, then latch the angle
			turnUntilWall();
			navigation.setSpeeds(0,0);
			angleB = odo.getTheta()*180/Math.PI;
			
			// get theta, turn to it and update position
			theta = getStartingAngle(angleA, angleB);
			
			theta = -theta;
			if(theta <= 0) {
				theta = theta +360;
			}
			
			navigation.turnTo(theta, true);
			//odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			//if(odo.getAng() == theta) {
				odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
				//}
			
		} else {
			Navigation navigation = new Navigation (odo);
			navigation.setSpeeds(turnSpeed,-turnSpeed);
			
			// Rotate until it's on a wall
			turnUntilWall();
			
			// rotate until it sees the wall
			turnUntilNoWall();
			
			// Stop the robot, get the first angle
			navigation.setSpeeds(0,0);
			angleB = odo.getTheta();
			
			// Face away
			navigation.turn(45);
			
			// Turn until it does'nt see the wall anymore
			navigation.setSpeeds(-turnSpeed,turnSpeed);
			turnUntilNoWall();
			
			// Stop the robot, get the second angle
			navigation.setSpeeds(0,0);
			angleA = odo.getTheta();
			
			// get the starting angle
			theta = getStartingAngle(angleA, angleB);
			
			// Correct it
			theta = -theta;
			if(theta <= 0) {
				theta = theta + 360;}
			
			// Move to tetha and set odometer to 0
			navigation.turnTo(theta, true);
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			
		}
	}
	
	/**
	 * Turns the robot until the sensors don't detect a wall anymore
	 * 
	 * @since 1.0
	 */
	private void turnUntilNoWall(){
		distance = getFilteredData();
		distError = bandCenter - distance;
		double distError2 = distError;
		double distError3 = distError;
		
		while (distError > BANDWIDTH || distError2 > BANDWIDTH  || distError3 > BANDWIDTH){
			distError3 = distError2;
			distError2 = distError;
			distance = getFilteredData();
			distError = bandCenter - distance;
		}
	}
	
	/**
	 * Turns the robot until the sensors detect a wall
	 * 
	 *  @since 1.0
	 */
	private void turnUntilWall(){
		distance = getFilteredData();
		distError = bandCenter - distance;
		double distError2 = distError;
		double distError3 = distError;
		
		while (distError < BANDWIDTH || distError2 < BANDWIDTH  || distError3 < BANDWIDTH){
			distError3 = distError2;
			distError2 = distError;
			distance = getFilteredData();
			distError = bandCenter - distance;
		}
	}
	
	/**
	 *  Get filtered data from US Sensor
	 * 
	 *  @since 1.0
	 */
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0]*100;
		
		if (distance > 60)
			distance = 60;
		
		return distance;
	}
	
	/**
	 *  Return starting angle in degrees
	 * 
	 *  @since 1.0
	 */
	private double getStartingAngle(double alpha, double beta){
		double angleDiff;
		if (alpha <= beta){
			angleDiff = ((45 - (alpha + beta)/2) % 360);
		}
		else {
			angleDiff = ((225 - (alpha + beta)/2) % 360);
		}
		return angleDiff;
	}

}
