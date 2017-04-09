package team10.navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Handles odometer functions for robot
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */

public class Odometer extends Thread {
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	// Static ressources
	private static final double WHEEL_RADIUS = 2.1;
	private static final double WHEEL_BASE = 14.43;
	private static final long ODOMETER_PERIOD = 25;
	

	// lock object for mutual exclusion
	private Object lock;

	/**
	 *  Constructor
	 * 	
	 *  @since 1.0
	 */
	public Odometer() {
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}
	
	/**
	 *  Return the wheel radius
	 *  
	 *  @return double WHEEL_RADIUS
	 *  @since 1.0
	 */
	public static double getWheelRadius(){
		return WHEEL_RADIUS;
	}
	
	/**
	 *  Return the wheel base
	 * 
	 * 	@return double WHEEL_BASE
	 *  @since 1.0
	 */
	public static double getWheelBase(){
		return WHEEL_BASE;
	}

	/**
	 *  Runs the odometer as a thread
	 * 	
	 *  @return No return value
	 *  @since 1.0
	 */
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			
			// Get tacho count from each motor
			int rightTacho = rightMotor.getTachoCount();
			int leftTacho = leftMotor.getTachoCount();
			
			// Get the tacho difference between this reading and the previous one. Multiplication by 1000 helps to keep accuracy for integers
			int rightTachoDiff = (rightTacho - getRightMotorTachoCount())*1000;
			int leftTachoDiff = (leftTacho - getLeftMotorTachoCount())*1000;
			
			// Calculate the distance covered by mutliplying the tacho count difference by the wheel radius and the angle covered. Division by 1000
			double rightDist = rightTachoDiff*(Math.PI/180)*WHEEL_RADIUS/1000;
			double leftDist = leftTachoDiff*(Math.PI/180)*WHEEL_RADIUS/1000;
			double totalDist = (rightDist + leftDist)/2;
			
			// Calculate the angle by substracting the right from the left distance covered
			double thetaAngle = (rightDist - leftDist)/WHEEL_BASE;
			
			// Set tacho count for the motors
			setLeftMotorTachoCount(leftTacho);
			setRightMotorTachoCount(rightTacho);
			

			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				theta += thetaAngle;
				setTheta(theta);
				
				// Correct for angles greater than 360 degrees
				if (getTheta() > 2*Math.PI)
					setTheta(getTheta() - 2*Math.PI);
				else if (getTheta() < 0) 
					setTheta(getTheta() + 2*Math.PI);
				
				// Set x & y to the new value
				setX(x + (Math.cos(theta))*totalDist);
				setY(y + (Math.sin(theta))*totalDist);
				
				// Update the display
				double[] positionArray = {x, y, theta};
				boolean[] booleanArray = {true, true, true};
				setPosition(positionArray, booleanArray);
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	/**
	 *  Accesssor
	 *  
	 * 	@param double[] position
	 * 	@param boolean[] update - if true, update position
	 *  @return No return value
	 *  @since 1.0
	 */
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}
	
	/**
	 *  Accesssor
	 *  
	 *  @return double x
	 *  @since 1.0
	 */
	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}
	
	/**
	 *  Accesssor
	 *  
	 *  @return double y
	 *  @since 1.0
	 */
	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}
	
	/**
	 *  Accesssor
	 *  
	 *  @return double theta
	 *  @since 1.0
	 */
	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}
	
	/**
	 *  Accesssor
	 *  
	 *  @param boolean degrees - if true, return the angle in degrees
	 *  @return double theta
	 *  @since 1.0
	 */
	public double getTheta(boolean degrees) {
		double result;

		synchronized (lock) {
			result = theta;
		}
		
		if (degrees == true)
			result = result*180/Math.PI;

		return result;
	}

	/**
	 *  Mutator
	 *  
	 * 	@param double[] position
	 * 	@param boolean[] update - if true, update position
	 *  @return No return value
	 *  @since 1.0
	 */
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}
	
	/**
	 *  Mutator
	 *  
	 *  @param double x
	 *  @return No return value
	 *  @since 1.0
	 */
	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}
	
	/**
	 *  Mutator
	 *  
	 *  @param double y
	 *  @return No return value
	 *  @since 1.0
	 */
	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}
	
	/**
	 *  Mutator
	 *  
	 *  @param double theta
	 *  @return No return value
	 *  @since 1.0
	 */
	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
	
	/**
	 *  Convert the angle from degrees to radians
	 *  
	 *  @param double degrees
	 *  @return double angle - in rads
	 *  @since 2.0
	 */
	public static double getRadAngle(double degrees) {
		return degrees*Math.PI/180;
	}
	
	

	/**
	 * Accessor
	 * 
	 * @return the leftMotorTachoCount
	 * @since 1.0
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * Mutator
	 * 
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 * @return No return value
	 * @since 1.0
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * Accessor
	 * 
	 * @return the rightMotorTachoCount
	 * @since 1.0
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * Mutator
	 * 
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 * @return No return value
	 * @since 1.0
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
	
	/**
	 * Prevent other actions before the motor has completed its run
	 * 
	 * @return No return value
	 * @since 2.0
	 */
	public static void waitTillCompleted(){
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.waitComplete();
		}
	}
}