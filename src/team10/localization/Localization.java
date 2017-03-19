package team10.localization;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import team10.navigation.Odometer;

public class Localization {
	private static final USLocalizer.LocalizationType localization_type = USLocalizer.LocalizationType.FALLING_EDGE;
	private static Port usPort;		
	private static Port colorPort;
	private Odometer odometer;
	

	public Localization (Port usPort, Port colorPort, Odometer odometer){
		Localization.usPort = usPort;
		Localization.colorPort = colorPort;
		this.odometer = odometer;
	}
	
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
}
