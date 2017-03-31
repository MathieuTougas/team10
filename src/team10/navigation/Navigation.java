package team10.navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
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
	private static final Port leftColorPort = LocalEV3.get().getPort("S2");	
	private static final Port rightColorPort = LocalEV3.get().getPort("S3");	
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 100;
	private static final int ACCELERATION = 250;
	private static final double TILE_SIZE = 30.48;
	private final static double DEGREE_ERR = 1;
	private final static double SENSOR_TRACK = 11.6;
	private final static double SENSOR_OFFSET = 5.35;
	private final static double BLACK_LINE = 40.0;
	public static float leftColor, rightColor;
	
	static double destX, destY, distW;
	private double currentX, currentY, wheelRadius, width;
	private boolean onPoint;
	private SampleProvider leftColorValue;
	private SampleProvider rightColorValue;
	private float[] leftColorData;	
	private float[] rightColorData;
	private boolean leftPassed, rightPassed;
	private double[] offsets;
	//private SampleProvider usDistance;
	//private float[] usData;
	
	public static double angleToTurn;
	
	/**
	 *  Constructor
	 * 
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
		
		// Setup color sensor
		@SuppressWarnings("resource")
		SensorModes leftColorSensor = new EV3ColorSensor(leftColorPort);
		this.setLeftColorValue(leftColorSensor.getMode("Red"));
		this.leftColorData = new float[getLeftColorValue().sampleSize()];
				
		// Setup color sensor
		@SuppressWarnings("resource")
		SensorModes rightColorSensor = new EV3ColorSensor(rightColorPort);
		this.setRightColorValue(rightColorSensor.getMode("Red"));
		this.rightColorData = new float[getRightColorValue().sampleSize()];
		
	}
	
	/**
	 *  Drives to the set destinations
	 * 
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
	 *  Travel to the point
	 * 
	 *  @since 1.0
	 */	
	public void travelTo(double x, double y){
		// Turn to the desired angle
		currentX = odometer.getX();
		currentY = odometer.getY();
		double finalX = x;
		double finalY = y;
		
		
		destX = finalX;
		destY = currentY;
		
		// X Routine
		double tetha = getAngle(currentX, currentY, destX, destY);
		angleToTurn = tetha - odometer.getTheta();
		turn(angleToTurn);
		
		// Set the motors speed forward
		setSpeeds(FORWARD_SPEED, FORWARD_SPEED);
		
		leftPassed = false;
		rightPassed = false;
		offsets = new double[2];
		
		// While it is navigating, update odometer and US distance
		while (isNavigating("X"));
		
		
		// Y routine
		destX = currentX;
		destY = finalY;
		
		if (finalY - currentY > 0){
			angleToTurn = Math.PI/2 - odometer.getTheta();
		}
		else {
			angleToTurn = -Math.PI/2 - odometer.getTheta();
		}
		turn(angleToTurn);
		
		// Set the motors speed forward
		setSpeeds(FORWARD_SPEED, FORWARD_SPEED);
		
		leftPassed = false;
		rightPassed = false;
		offsets = new double[2];
		
		// While it is navigating, update odometer and US distance
		while (isNavigating("Y"));
		
	}
	

	
	/**
	 *  Turn to the desired angle (rads)
	 * 
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
	 *  Turn to desired angle, relative to xy plane
	 *  
	 *  @param double angle (degrees)
	 *  @param boolean stop
	 *  
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
	 *  @since 1.0
	 */
	private boolean isNavigating(String axis){
		currentX = odometer.getX();
		currentY = odometer.getY();
		float leftColor = getLeftColorData();
		float rightColor = getRightColorData();
		while (Math.abs((int) currentX - destX) > 1 || Math.abs((int) currentY - destY) > 1){
			setSpeeds(FORWARD_SPEED,FORWARD_SPEED);
			if (axis.equals("X")){
				if (leftColor < getBLACK_LINE()){
					offsets[0] = odometer.getX();
					leftPassed = true;
				}
				else if (rightColor < getBLACK_LINE()){
					offsets[1] = odometer.getX();
					rightPassed = true;
				}
			}
			else {
				if (leftColor < getBLACK_LINE()){
					offsets[0] = odometer.getY();
					leftPassed = true;
				}
				else if (rightColor < getBLACK_LINE()){
					offsets[1] = odometer.getY();
					rightPassed = true;
				}
			}
			
			// Correct angle
			if (rightPassed == true && leftPassed == true){
				setSpeeds(0,0);
				correctPosition(offsets, axis, false);
				leftPassed = false;
				rightPassed = false;
			}
			return true;
		}
		// Stop the motors when on the point, set onPoint to true
		setSpeeds(0,0);
		onPoint = true;
		return false;
	}
	
	
	/**
	 *  Correct the robot position
	 * 
	 *  @since 2.0
	 */
	public void correctPosition(double[] positions, String axis, boolean moveForward){
		double leftValue = positions[0];
		double rightValue = positions[1];
		
		double diff = leftValue-rightValue;
		double angle = Math.asin(diff/getSENSOR_TRACK());
		if (axis.equals("X")){
			turn(-angle);
			if (moveForward == true){
				goForward(getSENSOR_OFFSET()-Math.abs(Odometer.getWheelBase()/2*Math.sin(angle)));
			}
		}
		else{
			turn(-angle);
			if (moveForward == true){
				goForward(getSENSOR_OFFSET()-Math.abs(Odometer.getWheelBase()/2*Math.sin(angle)));
			}
		}
	}
		
	/**
	 * Get the distance from the US sensor
	 * 
	 *  @since 1.0
	 */
	private int getUsDistance(SampleProvider us, float[] usData){
		us.fetchSample(usData,0);// acquire data
		int distance=(int)(usData[0]*100.0);
		return distance;
	}
	
	/**
	 * Get the angle to travel. This function handles negative x values
	 * 
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
	 *  Get data from the left color sensor
	 * 
	 *  @since 2.0
	 */
	private float getLeftColorData() {
		getLeftColorValue().fetchSample(leftColorData, 0);
		float color = leftColorData[0]*100;
		return color;
	}
	
	/**
	 *  Get data from the rightcolor sensor
	 * 
	 *  @since 1.0
	 */
	private float getRightColorData() {
		getRightColorValue().fetchSample(rightColorData, 0);
		float color = rightColorData[0]*100;
		return color;
	}
	
	/**
	 *  Accessors
	 * 
	 *  @since 2.0
	 */
	public SampleProvider getRightColorValue() {
		return rightColorValue;
	}

	public SampleProvider getLeftColorValue() {
		return leftColorValue;
	}
	
	public static double getSENSOR_TRACK() {
		return SENSOR_TRACK;
	}

	public static double getSENSOR_OFFSET() {
		return SENSOR_OFFSET;
	}

	public double getBLACK_LINE() {
		return BLACK_LINE;
	}

	public static int getForwardSpeed(){
		return FORWARD_SPEED;
	}
	
	public static int getTurnSpeed(){
		return ROTATE_SPEED;
	}
	
	/**
	 *  Mutators
	 * 
	 *  @since 2.0
	 */
	
	public void setRightColorValue(SampleProvider rightColorValue) {
		this.rightColorValue = rightColorValue;
	}

	
	public void setLeftColorValue(SampleProvider leftColorValue) {
		this.leftColorValue = leftColorValue;
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
	 *  Converts distance in wheelturns
	 * 
	 *  @since 1.0
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 *  Converts radians to degrees
	 * 
	 *  @since 1.0
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**
	 *  Convert tiles coordinates to distances in cm
	 * 
	 *  @since 1.0
	 */
	public static double convertTileToDistance(int tile){
		return tile*TILE_SIZE + TILE_SIZE;
	}
	
	/**
	 *  Wait a determined amount of time
	 * 
	 *  @since 1.0
	 */
	public static void wait(double seconds){
		try {
			Thread.sleep((long) (seconds*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

}