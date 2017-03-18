package labs.lab5;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Catapult {
	private static final int THROW_SPEED = 1500;
	private static final int POSITION_SPEED = 50;
	private static boolean stabilizerActive;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, stabilizerMotor, catapultMotor;
	private Odometer odometer;
	private double leftRadius, rightRadius, width;
	public static double angle;
	
	public Catapult (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor catapultMotor, EV3LargeRegulatedMotor stabilizerMotor, Odometer odometer, double leftRadius, double rightRadius, double width){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.catapultMotor = catapultMotor;
		this.stabilizerMotor = stabilizerMotor;
		this.odometer = odometer;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
	}
	
	public void fire(double[][] targets) {
		// reset the firing motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { catapultMotor, stabilizerMotor}) {
			motor.stop();
			motor.setAcceleration(6000);
		}
		// reset the motion motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(100);
			motor.setSpeed(POSITION_SPEED);
		}

		// wait 5 seconds
		wait(3.0);
		
		stabilizerMotor.setSpeed(POSITION_SPEED);
		stabilizerActive = false;
		// Run firing mode
		while (true) {
			int buttonChoice = Button.waitForAnyPress();
			while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_UP && buttonChoice != Button.ID_ENTER);
			
			switch (buttonChoice) {
				case Button.ID_LEFT:
					// Disengage the stabilizers if they are active
					disengageStabilizers();
					
					// Get angle to the next position
					angle = getAngle(targets[0][0], targets[0][1]);
					angle += odometer.getTheta();
					
					// Turn to the desired angle
					leftMotor.rotate(convertAngle(leftRadius, width, angle*180/Math.PI), true);
					rightMotor.rotate(-convertAngle(rightRadius, width, angle*180/Math.PI), false);
					break;

					
				case Button.ID_UP:
					// Disengage the stabilizers if they are active
					disengageStabilizers();
					
					// Get angle to the next position
					angle = getAngle(targets[1][0], targets[1][1]);
					angle += odometer.getTheta();
					
					// Turn to the desired angle
					leftMotor.rotate(convertAngle(leftRadius, width, angle*180/Math.PI), true);
					rightMotor.rotate(-convertAngle(rightRadius, width, angle*180/Math.PI), false);
					break;
					
				case Button.ID_RIGHT:
					// Disengage the stabilizers if they are active
					disengageStabilizers();
					
					// Get angle to the next position
					angle = getAngle(targets[2][0], targets[2][1]);
					angle += odometer.getTheta();
					
					// Turn to the desired angle
					leftMotor.rotate(convertAngle(leftRadius, width, angle*180/Math.PI), true);
					rightMotor.rotate(-convertAngle(rightRadius, width, angle*180/Math.PI), false);
					break;
					
				case Button.ID_ENTER:
					// Activate the stabilizers if they are not active
					engageStabilizers();
					
					// Fire the catapult
					catapultMotor.setSpeed(THROW_SPEED);
					catapultMotor.rotate(150, true);
					
					// Relace the catapult arm
					wait(3.0);
					catapultMotor.setSpeed(POSITION_SPEED);
					catapultMotor.rotate(-150, true);
					break;
			}
		}
	}
	
	// Engage the stabilizers
	void engageStabilizers(){
		if (stabilizerActive == false) {
		stabilizerMotor.rotate(150, true);
		wait(4.0);
		}
		stabilizerActive = true;
	}
	
	// Disengage the stabilizers
	void disengageStabilizers(){
		if (stabilizerActive == true) {
			stabilizerMotor.rotate(-150, true);
			wait(4.0);
		}
		stabilizerActive = false;
	}
	
	// Convert distances to wheelturns
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// Convert angle to wheelturns
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	// Get the angle
	private static double getAngle(double x, double y) {
		return Math.atan(x/y);
	}
	
	// Wait a determined amount of time
	private static void wait(double seconds){
		try {
			Thread.sleep((long) (seconds*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}