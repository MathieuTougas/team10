package team10.navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * Handles the directions calculation for the robot
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */

public class Navigation {
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Odometer odometer;
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 125;
	private static final int ACCELERATION = 250;
	private static final double TILE_SIZE = 30.48;
	private final static double DEGREE_ERR = 1;
	
	static double destX, destY, distW;
	private double currentX, currentY, wheelRadius, width;
	private boolean onPoint;
	private SampleProvider usDistance;
	private float[] usData;
	
	public static double angleToTurn;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public Navigation (Odometer odometer) {
		this.leftMotor = Odometer.leftMotor;
		this.rightMotor = Odometer.rightMotor;
		this.odometer = odometer;
		this.wheelRadius = Odometer.getWheelRadius();
		this.width = Odometer.getWheelBase();
		//this.usDistance = Localization.usSensor.getMode("Distance");
		//this.usData = new float[usDistance.sampleSize()];
		
	}
	
	/**
	 * Get forward SPEED
	 * 
	 *  @since 1.0
	 */
	public static int getForwardSpeed(){
		return FORWARD_SPEED;
	}
	
	/**
	 *  Get rotate Speed
	 * 
	 *  @since 1.0
	 */
	public static int getTurnSpeed(){
		return ROTATE_SPEED;
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
	

	
	public void travelTo(double x, double y){
		// Turn to the desired angle
		currentX = odometer.getX();
		currentY = odometer.getY();
		destX = x;
		destY = y;
		double tetha = getAngle(currentX, currentY, x, y);
		angleToTurn = tetha - odometer.getTheta();
		turn(angleToTurn);
		
		// Set the motors speed forward
		setSpeeds(FORWARD_SPEED, FORWARD_SPEED);
		
		// While it is navigating, update odometer and US distance
		while (isNavigating()){
			currentX = odometer.getX();
			currentY = odometer.getY();
		}
	}

	
	/**
	 *  Turn to the desired angle (rads)
	 * 
	 *  @since 2.0
	 */
	public void turn(double tetha){
		if (tetha > Math.PI){
			tetha -= Math.PI*2;
		}
		else if (tetha < -Math.PI){
			tetha += Math.PI*2;
		}
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		// Turn right
		leftMotor.rotate(-convertAngle(wheelRadius, width, tetha*180/Math.PI), true);
		rightMotor.rotate(convertAngle(wheelRadius, width, tetha*180/Math.PI), false);
	}
	
	/**
	 *  Turn to the desired angle (rads)
	 * 
	 *  @since 2.0
	 */
	public void turnTo(double tetha){
		angleToTurn = tetha - odometer.getTheta();
		turn(angleToTurn);
;	}
	
	/**
	 *  Turn to desired angle, relative to xy plane
	 *  
	 *  @param double angle (degrees)
	 *  @param boolean stop
	 *  
	 *  @since 1.0
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getTheta(true);

		while (Math.abs(error) > DEGREE_ERR) {

			error = angle - this.odometer.getTheta(true);

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
		//this.travelTo(Math.cos(Math.toRadians(this.odometer.getTheta())) * distance, Math.cos(Math.toRadians(this.odometer.getTheta())) * distance);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertDistance(wheelRadius, distance), false);

	}
	
	/**
	 *  Check for the position while it's moving
	 * 
	 *  @since 1.0
	 */
	private boolean isNavigating(){
		while (Math.abs((int) currentX - destX) > 2 || Math.abs((int) currentY - destY) > 2){
			return true;
		}
		// Stop the motors when on the point, set onPoint to true
		setSpeeds(0,0);
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
		
		if (xDiff > 0){
			tetha = Math.atan(yDiff/xDiff);
		}
		else if (xDiff < 0 && yDiff > 0) {
			tetha = Math.atan(yDiff/xDiff) + Math.PI;
		}
		else if (xDiff < 0 && yDiff < 0) {
			tetha = Math.atan(yDiff/xDiff) - Math.PI;
		}		
		return tetha;
	}
	
	/**
	 *  Wait a determined amount of time
	 * 
	 *  @since 1.0
	 */
	public static void wait(double seconds){
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
	public static double convertTileToDistance(int tile){
		return tile*TILE_SIZE + TILE_SIZE;
	}
}