package team10.launcher;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import team10.navigation.Navigation;

/**
 * Handles the ball launcher
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */

public class StringLauncher {
	public static final EV3LargeRegulatedMotor pullMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor releaseMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final int RELEASE_SPEED = 4000;
	private static final int PULL_SPEED = 300;
	public static double angle;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public StringLauncher (){
	}
	
	/**
	 *  Fire the ball
	 * 
	 *  @since 1.0
	 */
	public void fire() {
		// reset the firing motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { pullMotor, releaseMotor}) {
			motor.stop();
			motor.setAcceleration(6000);
		}
		
		// Pull back the string
		pullMotor.setSpeed(PULL_SPEED);
		pullMotor.rotate(360*6, true);
		Navigation.wait(1.0);
		
		// Activate the hook
		releaseMotor.setSpeed(PULL_SPEED);
		releaseMotor.rotate(45, true);
		Navigation.wait(1.0);
		
		// Unwind the string
		pullMotor.rotate(-360*6, true);
		
		// Fire the catapult
		releaseMotor.setSpeed(RELEASE_SPEED);
		releaseMotor.rotate(-45, true);
	}
	
}