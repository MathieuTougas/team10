package team10.navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import team10.Main;

/**
 * Handles the directions calculation for the robot
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class Navigation {
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 100;
	private static final int ACCELERATION = 1000;
	final static double DEGREE_ERR = 1;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Odometer odometer;
	static int destX, destY, distW;
	private double currentX, currentY, leftRadius, rightRadius, width;
	private boolean onPoint;
	private SampleProvider usDistance;
	private float[] usData;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public Navigation (Odometer odometer) {
		this.leftMotor = Main.leftMotor;
		this.rightMotor = Main.rightMotor;
		this.odometer = odometer;
		this.leftRadius = Main.WHEEL_RADIUS;
		this.rightRadius = Main.WHEEL_RADIUS;
		this.width = Main.TRACK;
		//usDistance = usSensor.getMode("Distance");
		//usData = new float[usDistance.sampleSize()];
		
	}
	
	/**
	 *  Drives to the set destinations
	 * 
	 *  @since 1.0
	 */
	public void drive(int[] destinations) {
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(ACCELERATION);
		}

		// wait 5 seconds
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}
		
		//  Variable initialization
		onPoint = true;
		int point = 0;
		
		// Point navigation
		while (point <= destinations.length) {
			
			currentX = odometer.getX();
			currentY = odometer.getY();
			
			if (onPoint == true && point < destinations.length){
				destX = destinations[point];
				destY = destinations[point+1];
				point+=2;
				onPoint = false;
			}
			else if ( point == destinations.length && onPoint == true){
				break;
			}
			
			travelTo(destX, destY);
		}
	}
	
	/**
	 *  Functions to set the motor speeds jointly
	 *  
	 *  @since 1.0
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}
	
	/**
	 *  Turn fixed angle
	 *  
	 *  @param double angle (deg)
	 *  @param boolean stop
	 *  
	 *  @since 1.0
	 */
	public void turnAng(double angle, boolean stop){
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			
			leftMotor.rotate(-convertAngle(Main.WHEEL_RADIUS, Main.TRACK, angle), true);
			rightMotor.rotate(convertAngle(Main.WHEEL_RADIUS, Main.TRACK, angle), false);
	}
	
	/**
	 *  Converts distance in wheelturns
	 * 
	 *  @since 1.0
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 *  Converts radians to degrees
	 * 
	 *  @since 1.0
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**
	 *  Travel to the point
	 * 
	 *  @since 1.0
	 */
	public void travelTo(double x, double y){
		// Turn to the desired angle
		double tetha = getAngle(currentX, currentY, x, y);
		tetha += odometer.getTheta();
		turnTo(tetha);
		
		// Set the motors speed forward
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.forward();
		rightMotor.forward();
		
		//  boolean validating if the wall follower had been active
		boolean wF = false;
		
		// While it is navigating, update odometer and US distance
		while (isNavigating() && wF == false){
			currentX = odometer.getX();
			currentY = odometer.getY();
			distW = getUsDistance(usDistance, usData);
			
			// When it reaches a distance of less than 25cm
			while (distW < 25){
				turnTo(Math.PI/2);
				distW = getUsDistance(usDistance, usData);
				wF = true;
			}
		}
		
		// If the wall follower has been active, move forward
		if (wF == true){
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.rotate(convertDistance(leftRadius, 20), true);
			rightMotor.rotate(convertDistance(rightRadius, 20), false);
		}
	}
	
	/**
	 *  Turn to the desired angle
	 * 
	 *  @since 1.0
	 */
	private void turnTo(double tetha){
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(leftRadius, width, tetha*180/Math.PI), true);
		rightMotor.rotate(-convertAngle(rightRadius, width, tetha*180/Math.PI), false);
	}
	
	/**
	 *  Turn to desired angle, relative to xy plane
	 *  
	 *  @param double angle (degrees)
	 *  @param boolean stop
	 *  
	 *  @since 1.0
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getTheta();

		while (Math.abs(error) > DEGREE_ERR) {

			error = angle - this.odometer.getTheta();

			if (error < -180.0) {
				this.setSpeeds(-ROTATE_SPEED, ROTATE_SPEED);
			} else if (error < 0.0) {
				this.setSpeeds(ROTATE_SPEED, -ROTATE_SPEED);
			} else if (error > 180.0) {
				this.setSpeeds(ROTATE_SPEED, -ROTATE_SPEED);
			} else {
				this.setSpeeds(-ROTATE_SPEED, ROTATE_SPEED);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	/**
	 * Go foward a set distance in cm
	 * 
	 *  @since 1.0
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getTheta())) * distance, Math.cos(Math.toRadians(this.odometer.getTheta())) * distance);

	}
	
	/**
	 *  Check for the position while it's moving
	 * 
	 *  @since 1.0
	 */
	private boolean isNavigating(){
		while (Math.abs((int) currentX - destX) > 1 || Math.abs((int) currentY - destY) > 1){
			return true;
		}
		// Stop the motors when on the point, set onPoint to true
		leftMotor.stop();
		rightMotor.stop();
		onPoint = true;
		return false;
	}
	
	/**
	 * Get the distance from the US sensor
	 * 
	 *  @since 1.0
	 */
	private int getUsDistance(SampleProvider us, float[] usData){
		us.fetchSample(usData,0);// acquire data
		int distance=(int)(usData[0]*100.0);
		return distance;
	}
	
	/**
	 * Get the angle to travel. This function handles negative x values
	 * 
	 *  @since 1.0
	 */
	private double getAngle(double initialX, double initialY, double finalX, double finalY){
		double xDiff = finalX - initialX;
		double yDiff = finalY - initialY;
		double tetha = 0;
		
		if (yDiff >0){
			tetha = Math.atan(xDiff/yDiff);
		}
		else if (yDiff < 0 && xDiff > 0) {
			tetha = Math.atan(xDiff/yDiff) + Math.PI;
		}
		else if (yDiff < 0 && xDiff < 0) {
			tetha = Math.atan(xDiff/yDiff) - Math.PI;
		}		
		return tetha;
	}
}