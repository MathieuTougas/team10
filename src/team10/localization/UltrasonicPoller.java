package team10.localization;

import lejos.robotics.SampleProvider;

/**
 * Ultrasonic poller thread for object avoidance
 * 
 * @author Mathieu Tougas
 * @version 1.0
 * 
 */
public class UltrasonicPoller extends Thread{
	private SampleProvider us;
	private PController cont;
	private float[] usData;
	
	/**
	 *  Constructor
	 * 
	 * 	@param SampleProvider us
	 *  @param float[] usData
	 *  @param PController cont
	 *  @since 1.0
	 */
	public UltrasonicPoller(SampleProvider us, float[] usData, PController cont) {
		this.us = us;
		this.cont = cont;
		this.usData = usData;
	}

	/**
	 *  Run the ultrasonic poller
	 * 
	 *  @return No return value
	 *  @since 1.0
	 */
	public void run() {
		int distance;
		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			cont.processUSData(distance);						// now take action depending on value
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
	}

}
