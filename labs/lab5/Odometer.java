/*
 * Odometer.java
 */

package labs.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	private final double wheelRadius = 2.1; // cm
	private final double wheelBase = 15.7; //Outside-Outside 17.8cm - Middle-Middle 15.1cm - Inside-Inside 12.9cm

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}

	// run method (required for Thread)
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
			double rightDist = rightTachoDiff*(Math.PI/180)*wheelRadius/1000;
			double leftDist = leftTachoDiff*(Math.PI/180)*wheelRadius/1000;
			double totalDist = (rightDist + leftDist)/2;
			
			// Calculate the angle by substracting the right from the left distance covered
			double thetaAngle = (rightDist - leftDist)/wheelBase;
			
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
				else if (getTheta() < -2*Math.PI) 
					setTheta(getTheta() + 2*Math.PI);
				
				// Set x & y to the new value
				setX(x - (Math.sin(theta))*totalDist);
				setY(y + (Math.cos(theta))*totalDist);
				
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

	// accessors
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

	// mutators
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
}