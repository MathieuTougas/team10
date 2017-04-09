package team10.localization;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import team10.navigation.Navigation;
import team10.navigation.Odometer;

/**
 * Handles the localization routine for the robot
 * 
 * @author Mathieu Tougas
 * @version 2.0
 * 
 */
public class Localization {
	private static final Port usPort = LocalEV3.get().getPort("S4");		
	private static final Port leftColorPort = LocalEV3.get().getPort("S2");	
	private static final Port rightColorPort = LocalEV3.get().getPort("S3");	
	SampleProvider leftColorValue, rightColorValue;
	private static final USLocalizer.LocalizationType localization_type = USLocalizer.LocalizationType.FALLING_EDGE;
	private Odometer odometer;
	private Navigation navigation;
	
	/**
	 *  Constructor
	 * 
	 * 	@param Odometer odometer
	 *  @param Navigation navigation
	 *  @since 1.0
	 */
	public Localization (Odometer odometer, Navigation navigation){
		this.odometer = odometer;
		this.navigation = navigation;
	}
	
	/**
	 *  Do the localisation routine, given initial starting corner
	 * 
	 *  @param double[] initalPosition - the initialPosition given the starting corner
	 *  @return No return value
	 *  @since 1.0
	 */
	public void doLocalization(double[] initialPosition) {
		
		// Setup US sensor
		@SuppressWarnings("resource")					    	
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		
		// Setup color sensor
		@SuppressWarnings("resource")
		SensorModes leftColorSensor = new EV3ColorSensor(leftColorPort);
		leftColorValue =leftColorSensor.getMode("Red");
		float[] leftColorData = new float[leftColorValue.sampleSize()];
				
		// Setup color sensor
		@SuppressWarnings("resource")
		SensorModes rightColorSensor = new EV3ColorSensor(rightColorPort);
		rightColorValue = rightColorSensor.getMode("Red");
		float[] rightColorData = new float[rightColorValue.sampleSize()];
		//while (Button.waitForAnyPress() != Button.ID_ENTER);
		
		Sound.beep();
		
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odometer, navigation, usValue, usData, localization_type);
		usl.doLocalization();
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odometer, navigation, leftColorValue, leftColorData, rightColorValue, rightColorData);
		lsl.doLocalization(initialPosition);
		
		Sound.beep();
	}
	
	/**
	 *  Correct position using light sensors before shot, goes forward until it crosses a line
	 * 
	 *  @return No return value
	 *  @since 2.0
	 */
	public void correctBeforeShort(){
		// Setup color sensor
		float[] leftColorData = new float[leftColorValue.sampleSize()];
				
		// Setup color sensors
		float[] rightColorData = new float[rightColorValue.sampleSize()];
		navigation.goForward(15);
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odometer, navigation, leftColorValue, leftColorData, rightColorValue, rightColorData);
	    lsl.runUntilLine("Y", true);	
	}
}
