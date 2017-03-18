package team10.localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Handles the object avoidance routine
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */


public class PController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private Double propConst = 1.8;
	private int maxCorrection = 160;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	/**
	 *  Filters the US data
	 * 
	 *  @since 1.0
	 */
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
		
		int distError;
		
		distError = bandCenter - distance; // Compute error term
		
		int leftSpeed;
		int rightSpeed;
		int diff;

		if (Math.abs(distError) <= bandwidth) { // Case 1: Error in bounds, no
			leftSpeed=motorStraight; // correction
			rightSpeed=motorStraight;
			leftMotor.setSpeed(leftSpeed); // If correction was being applied on
			rightMotor.setSpeed(rightSpeed); // last update, clear it
			leftMotor.forward(); // Hack - leJOS bug
			rightMotor.forward();
		}
		else if (distError > 0) { // Case 2: positive error, move away from wall
			diff=calcProp(distError, false); // Get correction value and apply
			leftSpeed=motorStraight+diff;
			rightSpeed=motorStraight-diff;
			leftMotor.setSpeed(leftSpeed);
			rightMotor.setSpeed(rightSpeed);
			leftMotor.forward(); // Hack - leJOS bug
			rightMotor.forward();
		}
		else if (distError < 0) { // Case 3: negative error, move towards wall
			diff=calcProp(distError, true); // Get correction value and apply
			leftSpeed=motorStraight-diff;
			rightSpeed=motorStraight+diff;
			leftMotor.setSpeed(leftSpeed);
			rightMotor.setSpeed(rightSpeed);
			leftMotor.forward(); // Hack - leJOS bug
			rightMotor.forward();
		}
	}
	
	/**
	 *  Calculates the proportionality for direction adjustments
	 * 
	 *  @since 1.0
	 */
	int calcProp (int diff, boolean leftTurn) {
		int correction;
		if (leftTurn == true)
			maxCorrection = 80;
		else
			maxCorrection = 160;
		// PROPORTIONAL: Correction is proportional to magnitude of error
		if (diff < 0) diff=-diff;
		correction = (int)(propConst *(double)diff);
		if (correction >= motorStraight) correction = maxCorrection;
		return correction;
		}

	/**
	 *  Gets US distance from sensor
	 * 
	 *  @since 1.0
	 */
	public int readUSDistance() {
		return this.distance;
	}

}
