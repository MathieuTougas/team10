package team10.localization;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import team10.navigation.Odometer;

public class Localization {
	private static final Port usPort = LocalEV3.get().getPort("S4");		
	private static final Port colorPort = LocalEV3.get().getPort("S3");	
	private static final USLocalizer.LocalizationType localization_type = USLocalizer.LocalizationType.FALLING_EDGE;
	private Odometer odometer;
	
	/**
	 *  Constructor
	 * 
	 *  @since 1.0
	 */
	public Localization (Odometer odometer){
		this.odometer = odometer;
	}
	
	/**
	 *  Do the localisation routine
	 * 
	 *  @since 1.0
	 */
	public void doLocalization() {
		
		// Setup US sensor
		@SuppressWarnings("resource")					    	
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		
		// Setup color sensor
		@SuppressWarnings("resource")
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");
		float[] colorData = new float[colorValue.sampleSize()];
				

		//while (Button.waitForAnyPress() != Button.ID_ENTER);
		
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odometer, usValue, usData, localization_type);
		usl.doLocalization();
		
		//while (Button.waitForAnyPress() != Button.ID_ENTER);
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odometer, colorValue, colorData);
		lsl.doLocalization();			
	}
	
	/**
	 *  Do the localisation routine, given initial starting corner
	 * 
	 *  @param initialPosition (x, y, theta)
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
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");
		float[] colorData = new float[colorValue.sampleSize()];
				

		//while (Button.waitForAnyPress() != Button.ID_ENTER);
		
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odometer, usValue, usData, localization_type);
		usl.doLocalization();
		
		//while (Button.waitForAnyPress() != Button.ID_ENTER);
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odometer, colorValue, colorData);
		lsl.doLocalization(initialPosition);			
	}
}
