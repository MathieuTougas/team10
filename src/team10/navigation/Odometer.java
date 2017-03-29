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
	private static final double WHEEL_BASE = 13.8;
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
	 *  @since 1.0
	 */
	public static double getWheelRadius(){
		return WHEEL_RADIUS;
	}
	
	/**
	 *  Return the wheel base
	 * 
	 *  @since 1.0
	 */
	public static double getWheelBase(){
		return WHEEL_BASE;
	}

	/**
	 *  Runs the odometer as a thread
	 * 
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
				setTheta(theta + thetaAngle);
				
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

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}
	
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

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
	/**
	 * @return angle in radians
	 */
	public static double getRadAngle(double degrees) {
		return degrees*Math.PI/180;
	}
	
	

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
	
	/**
	 * Get the angle to travel. This function handles negative x values
	 * 
	 *  @since 1.0
	 */
	public static void waitTillCompleted(){
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.waitComplete();
		}
	}
}