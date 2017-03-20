package team10.navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import team10.Main;

/**
 * Handles all the movement for the robot (turnTo, travelTo, flt, localize)
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class NavigationTest {
	final static int FAST = 200, SLOW = 80, ACCELERATION = 4000;
	final static double DEG_ERR = 1, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public NavigationTest (Odometer odo) {
		this.odometer = odo;
		this.leftMotor = Odometer.leftMotor;
		this.rightMotor = Odometer.rightMotor;

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
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
	 *  Float the two motors to test odometer
	 *  
	 *  @since 1.0
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/**
	 *  Travel to point on plane
	 *  
	 *  @since 1.0
	 */
	public void travelTo(double x, double y) {
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			this.setSpeeds(SLOW, SLOW);
		}
		this.setSpeeds(0, 0);
	}

	/**
	 *  Turn to desired angle, relative to xy plane
	 *  
	 *  @param double angle (deg)
	 *  @param boolean sstop
	 *  
	 *  @since 1.0
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getTheta();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getTheta();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
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
			leftMotor.setSpeed(SLOW);
			rightMotor.setSpeed(SLOW);
			
			leftMotor.rotate(-convertAngle(Odometer.WHEEL_RADIUS, Odometer.WHEEL_BASE, angle), true);
			rightMotor.rotate(convertAngle(Odometer.WHEEL_RADIUS, Odometer.WHEEL_BASE, angle), false);
	}
	
	/**
	 *  Convert distance in wheelturns
	 *  
	 *  @since 1.0
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 *  Convert angle in wheelturns
	 *  
	 *  @since 1.0
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	} 
	
	/**
	 * Go foward a set distance in cm
	 * 
	 *  @since 1.0
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getTheta())) * distance, Math.cos(Math.toRadians(this.odometer.getTheta())) * distance);

	}
}
