package team10;

/*
 * File: USLocalizer.java
 * Written by: Mathieu Tougas
 * ECSE 211 - Team 10
 * Winter 2017
 * 
 * Localizer class
 */


import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static float ROTATION_SPEED = 50;
	public static float distance;
	public static double angleA, angleB, theta;
	private final int BANDWIDTH = 5;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	float bandCenter = 50;
	float distError;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
	    this.locType = locType;
	}
	
	public void doLocalization() {
		angleA = 0; 
		angleB = 0;
		theta = 0;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			Navigation navigation = new Navigation (odo);
			navigation.setSpeeds(ROTATION_SPEED,-ROTATION_SPEED);

			// rotate the robot until it sees no wall
			distance = getFilteredData();
			distError = bandCenter - distance;
			if (distError > BANDWIDTH) {
				navigation.turnAng(-45,true);
			}
			navigation.setSpeeds(ROTATION_SPEED,-ROTATION_SPEED);
			turnUntilNoWall();
			// keep rotating until the robot sees a wall, then latch the angle
			turnUntilWall();
			navigation.setSpeeds(0,0);
			angleA = odo.getAng();
			
			// switch direction and wait until it sees no wall
			navigation.setSpeeds(-ROTATION_SPEED,ROTATION_SPEED);
			turnUntilNoWall();
			
			// keep rotating until the robot sees a wall, then latch the angle
			turnUntilWall();
			navigation.setSpeeds(0,0);
			angleB = odo.getAng();
			
			// get theta, turn to it and update position
			theta = getStartingAngle(angleA, angleB);
			
			theta = -theta;
			if(theta <= 0) {
				theta = theta +360;}
			
			navigation.turnTo(theta, true);
			//odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			//if(odo.getAng() == theta) {
				odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
				//}
			
		} else {
			Navigation navigation = new Navigation (odo);
			navigation.setSpeeds(ROTATION_SPEED,-ROTATION_SPEED);
			
			// Rotate until it's on a wall
			turnUntilWall();
			
			// rotate until it sees the wall
			turnUntilNoWall();
			
			// Stop the robot, get the first angle
			navigation.setSpeeds(0,0);
			angleB = odo.getAng();
			
			// Face away
			navigation.turnAng(45,true);
			
			// Turn until it does'nt see the wall anymore
			navigation.setSpeeds(-ROTATION_SPEED,ROTATION_SPEED);
			turnUntilNoWall();
			
			// Stop the robot, get the second angle
			navigation.setSpeeds(0,0);
			angleA = odo.getAng();
			
			// get the starting angle
			theta = getStartingAngle(angleA, angleB);
			
			// Correct it
			theta = -theta;
			if(theta <= 0) {
				theta = theta + 360;}
			
			// Move to tetha and set odometer to 0
			navigation.turnTo(theta, true);
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILL THIS IN
			//
		}
	}
	
	// Turn until the sensor doesn't see a wall anymore
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
	
	// Turn until the sensor detects a wall
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
	
	// Get the data from the ultrasonic sensor
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0]*100;
		
		if (distance > 60)
			distance = 60;
		
		return distance;
	}
	
	// Returns the angle in degrees
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
