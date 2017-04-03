package team10.navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;


/**
 * This class handles obstacle avoidance. It uses the same odometer as main and the other classes
 * An object of this class is created when an object or wall is detected by the ultrasonic sensor. This class then wall follows
 * around the object until the robot is back on its original trajectory, now safe after having avoided the obstacle
 * 
 * @author Russ Xiang
 * @version 2.0
 * 
 */

public class ObstacleAvoidance extends Thread
{
	/**
	 * Boolean that robot is safe to continue traveling to coordinate. Intially false, set to true when robot has avoided obstacle.
	 */
	boolean safe;
	/**
	 * Stores the x coordinate of the robot at the moment where the robot detects the obstacle
	 */
	double pastX;
	/**
	 * Stores the y coordinate of the robot at the moment where the robot detects the obstacle
	 */
	double pastY;
	double idealTheta;
	/**
	 * Stores how far in x we have gone away from the initial position when we detected the obstacle in (cm)
	 */
	double calcX;
	/**
	 * Stores how far in y we have gone away from the initial position when we detected the obstacle in (cm)
	 */
	double calcY;
	/**
	 * Stores the theta of the robot relative to the initial position when we detected the obstacle in (radians)
	 */
	double calcTheta;
	
	private Odometer odometer;
	private final int bandCenter, bandwidth;

	/**
	 * Stores values of the low and high motors speeds in (deg/s)
	 */
	private final int motorLow, motorHigh;

	/**
	 * Stores the current x coordinate of the robot in (cm)
	 */
	private double avoidanceNowX;
	/**
	 * Stores the current y coordinate of the robot in (cm)
	 */
	private double avoidanceNowY;
	private double distThreshold = 0.5;
	private double thetaThreshold = 0.0078565804;
	

	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	private SampleProvider sampleProvider;
	private float[] usData;


	/**
	 * Takes many variables in the constructor of this class. Many of these parameters are set in main and eventually set here.
	 * 
	 * @param pastX - x coordinate of the robot at the moment where the robot detects the obstacle
	 * @param pastY - y coordinate of the robot at the moment where the robot detects the obstacle
	 * @param idealTheta - the heading of the robot at the moment where the robot detects the obstacle.
	 * This is also the same theta that the robot needs to head to get to its destination
	 * @param odometer - the odometer object that this thread will use
	 * @param leftMotor - the left motor that this thread will use
	 * @param rightMotor - the right motor that this thread will use
	 * @param bandCenter - the distance that the ultrasonic sensor needs to read to keep the robot going straight
	 * @param bandWidth - the deviation from the bandCenter that will keep the robot going straight
	 * @param motorLow - the low motor speed, used to turn away or towards the wall
	 * @param motorHigh - the high motor speed, used to turn away or towards the wall
	 * @param sampleProvider - the sample provider for the ultrasonic sensor that this class uses
	 */
	public ObstacleAvoidance(double pastX, double pastY, double idealTheta, Odometer odometer,EV3LargeRegulatedMotor leftMotor,
			EV3LargeRegulatedMotor rightMotor, int bandCenter, int bandWidth, int motorLow, int motorHigh, SampleProvider sampleProvider)
	{
		this.odometer = odometer;
		this.safe = false;
		this.pastX = pastX;
		this.pastY = pastY;
		this.idealTheta = idealTheta;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.bandCenter = bandCenter;
		this.bandwidth = bandWidth;
		this.motorLow = motorLow; 
		this.motorHigh = motorHigh;
		
		this.sampleProvider = sampleProvider;
		this.usData = new float[sampleProvider.sampleSize()];
	}
	
	public void run() {
		
		while(!safe) {
			
			int distance;
			sampleProvider.fetchSample(usData,0);
			distance = (int)(usData[0]*100.0);
			
			avoidanceNowX = odometer.getX();
			avoidanceNowY = odometer.getY();

			calcX = avoidanceNowX - pastX;
			calcY = avoidanceNowY - pastY;
			
			calcTheta = Math.atan(calcY/calcX); // returns angle between -pi/2 to pi/2)
			
			calcTheta = convertTheta(calcTheta); // now between 0 and 2*pi
			
			
			if (Math.abs(calcTheta - idealTheta) <= thetaThreshold) {
				if(Math.abs(avoidanceNowX - pastX) < distThreshold && Math.abs(avoidanceNowY - pastY) < distThreshold) 
					processUSData(distance);
				else
					safe = true;
			} else {
				processUSData(distance); // run the Bang Bang controller
			}
			
		}
		
		try
		{
			Thread.sleep(30);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		} 	
		
	}
	
	/**Converts the angle so that it is in the range 0 to 2pi based on calcX and calcY.
	 * @param calcTheta2 - angle that is to be converted (as Math.atan only returns angle between -pi/2 to pi/2)
	 * @return angle that is converted (from 0 to 2pi)
	 */
	private double convertTheta(double calcTheta2) {
		
		if(calcX > 0) {
			
			if(calcY > 0)//positive theta 
				return Math.atan(calcY/calcX);
			else //converts quadrant 4 into a positive theta
				return 2*Math.PI + Math.atan(calcY/calcX);
			
		} else if(calcX < 0) {
			
			if(calcY > 0) //quad 2, positive theta
				return (Math.atan(calcY/calcX) + Math.PI);
			else if(calcY < 0) //quad 3, positive theta
				return (Math.atan(calcY/calcX) + Math.PI);
			
		} else if(Math.abs(calcX) < distThreshold){
			
			if(calcY > 0)
				return 0.5*Math.PI;
			else
				return 1.5*Math.PI;
			
		} else if(Math.abs(calcY) < distThreshold) {
			
			if(calcX > 0)
				return 0;
			else
				return Math.PI;
			
		}
		
		return 0.0; // null case, error occurs
		
	}
	
	/**Takes the distance read from the ultrasonic sensor and determines if the robot should move away or towards the wall, or keep going straight.
	 * Bang bang wall follower.
	 * @param distance - distance read from the ultrasonic sensor
	 */
	public void processUSData(int distance) { 

		int difference = distance - bandCenter;
		
		if (Math.abs(difference)<=(bandwidth)) {// within band, continue
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
		}


		else if ((difference)>0) {// robot too far from the wall, turn left to the wall
			leftMotor.setSpeed(motorLow);
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
		}

		else if ((difference)<0) {// robot too close, turn right away from the wall
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorLow);
			leftMotor.forward();
			rightMotor.forward();
		}
		
	}
}