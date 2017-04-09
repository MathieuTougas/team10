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
	private boolean onPoint, passed;
	
	public static double angleToTurn;
	
	/**
	 *  Constructor
	 * 	@param Odometer odometer
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
	 *  Get forward SPEED
	 *  
	 * 	@param No parameter
	 *  @return int FORWARD_SPEED
	 *  @since 2.0
	 */
	public static int getForwardSpeed(){
		return FORWARD_SPEED;
	}
	
	/**
	 *  Get rotate Speed
	 * 
	 * 	@param No parameter
	 *  @return int ROTATE_SPEED
	 *  @since 2.0
	 */
	public static int getTurnSpeed(){
		return ROTATE_SPEED;
	}
	
	/**
	 *  Drives to the set destinations
	 * 	
	 *  @param int[] destinations - the points to travel-to
	 *  @return No return value
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
	 *  @param float lSpd - the speed to set the left motor to
	 *  @param float rSpd - the speed to set the right motor to
	 *  @return No return value
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
	 *  @param double radius - the radius of the wheels
	 *  @param double distance - the distance to cover
	 *  @return int wheelturns - the number of wheelturns to cover
	 *  @since 1.0
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 *  Converts radians  wheelturns
	 * 	
	 *  @param double radius - the radius of the wheels
	 *  @param double width - the robot track width
	 *  @param double distance - the distance to cover
	 *  @return int wheelturns - the number of wheelturns to cover
	 *  @since 1.0
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	

	/**
	 *  Travel to a set point
	 * 	
	 *  @param double x - x-coordinate to travel to
	 *  @param double y - y coordinate to travel to
	 *  @return No return value
	 *  @since 1.0
	 */
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
		passed = false;
		onPoint = false;
		setSpeeds(FORWARD_SPEED, FORWARD_SPEED);
		
		// While it is navigating, update odometer and US distance
		while (isNavigating()){
			currentX = odometer.getX();
			currentY = odometer.getY();
		}
		
		if (onPoint == false){
			travelTo(destX, destY);
		}
	}

	
	/**
	 *  Turn to the desired angle (rads)
	 * 	
	 *  @param double tetha - angle to turn in rads
	 *  @return No return value
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
	 *  @param double tetha - angle to turn to in rads
	 *  @return No return value
	 *  @since 2.0
	 */
	public void turnTo(double tetha){
		angleToTurn = tetha - odometer.getTheta();
		turn(angleToTurn);
;	}
	
	/**
	 *  Turn to desired angle, relative to xy plane
	 *  
	 *  @param double angle - angle to turn in degrees
	 *  @param boolean stop - if true, stop the wheels after the turn
	 *  @return No return value
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
	 *  @param double distance - distance to go forward in cm
	 *  @return No return value
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
	 *  @return boolean navigating - true if the robot is navigating
	 *  @since 1.0
	 */
	private boolean isNavigating(){
		while (Math.abs((int) currentX - destX) > 1 || Math.abs((int) currentY - destY) > 1){
			if (Math.abs((int) currentX - destX) < 10 && Math.abs((int) currentY - destY) < 10){
				passed = true;
			}
			if (Math.abs((int) currentX - destX) > 10 && Math.abs((int) currentY - destY) > 10 && passed == true ){
				setSpeeds(0,0);
				return false;
			}
			return true;
		}
		// Stop the motors when on the point, set onPoint to true
		setSpeeds(0,0);
		onPoint = true;
		return false;
	}
	
	/**
	 * Get the angle to travel. This function handles negative x values
	 * 	
	 *  @param double initialX - inital x position
	 *  @param double initialY - initial y position
	 *  @param double finalX - destination y
	 *  @param double finalY - destination x
	 *  @return double tetha - in rads
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
	 * 	@param double seconds - amount time to wait
	 *  @return No return value
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
	 *  Convert a number of tiles to a distance on the field (cm)
	 *  
	 * 	@param int tile - the number of tiles
	 *  @return double distance - in cm
	 *  @since 2.0
	 */
	public static double convertTileToDistance(int tile){
		return tile*TILE_SIZE + TILE_SIZE;
	}
}