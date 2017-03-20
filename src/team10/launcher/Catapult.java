package team10.launcher;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Handles the ball launcher
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class Catapult {
	public static final EV3LargeRegulatedMotor catapultMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor stabilizerMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final int THROW_SPEED = 1500;
	private static final int POSITION_SPEED = 50;
	private static boolean stabilizerActive;
	public static double angle;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public Catapult (){
	}
	
	/**
	 *  Fire the ball
	 * 
	 *  @since 1.0
	 */
	public void fire() {
		// reset the firing motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { catapultMotor, stabilizerMotor}) {
			motor.stop();
			motor.setAcceleration(6000);
		}

		// wait
		wait(1.0);
		
		stabilizerMotor.setSpeed(POSITION_SPEED);
		stabilizerActive = false;
		// Run firing mode

		// Activate the stabilizers if they are not active
		engageStabilizers();
		
		// Fire the catapult
		catapultMotor.setSpeed(THROW_SPEED);
		catapultMotor.rotate(150, true);
		
		// Relace the catapult arm
		wait(1.0);
		catapultMotor.setSpeed(POSITION_SPEED);
		catapultMotor.rotate(-150, true);
		
		disengageStabilizers();
	}
	
	/**
	 *  Engage the stabilizers
	 * 
	 *  @since 1.0
	 */
	public void engageStabilizers(){
		if (stabilizerActive == false) {
		stabilizerMotor.rotate(150, true);
		wait(4.0);
		}
		stabilizerActive = true;
	}
	
	/**
	 *  Disengage the stabilizers
	 * 
	 *  @since 1.0
	 */

	public void disengageStabilizers(){
		if (stabilizerActive == true) {
			stabilizerMotor.rotate(-150, true);
			wait(4.0);
		}
		stabilizerActive = false;
	}
	
	
	/**
	 *  Wait a determined amount of time
	 * 
	 *  @since 1.0
	 */
	private static void wait(double seconds){
		try {
			Thread.sleep((long) (seconds*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}