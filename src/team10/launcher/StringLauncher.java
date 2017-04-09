package team10.launcher;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import team10.navigation.Navigation;

/**
 * Handles the ball launcher
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */

public class StringLauncher {
	public static final EV3LargeRegulatedMotor pullMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor releaseMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final int RELEASE_SPEED = 4000;
	private static final int PULL_SPEED = 300;
	public static double angle;
	public static boolean catapultLowered;
	
	/**
	 *  Constructor
	 * 	
	 *  @since 2.0
	 */
	public StringLauncher (){
		catapultLowered = false;
	}
	
	/**
	 *  Fire the ball
	 *  
	 *  @return No return value
	 *  @since 2.0
	 */
	public void fire() {
		if (catapultLowered == true){
			// Fire the catapult
			releaseMotor.setSpeed(RELEASE_SPEED);
			releaseMotor.rotate(90, false);
		}
	}
	
	/**
	 *  Lower the catapult
	 * 	
	 *  @return No return value
	 *  @since 2.0
	 */
	public void lowerCatapult() {
		// reset the firing motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { pullMotor, releaseMotor}) {
			motor.stop();
			motor.setAcceleration(6000);
		}
		
		// Pull back the string
		pullMotor.setSpeed(PULL_SPEED);
		pullMotor.rotate(2*360, false);
		Navigation.wait(0.5);
		
		// Activate the hook
		releaseMotor.setSpeed(PULL_SPEED);
		releaseMotor.rotate(-90, false);
		Navigation.wait(0.5);
		
		// Unwind the string
		pullMotor.rotate(-360*2, false);
		
		catapultLowered = true;
	}
	
}