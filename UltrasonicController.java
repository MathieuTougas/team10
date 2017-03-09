package team10;

/*
 * File: UltrasonicController.java
 * Written by: Mathieu Tougas
 * ECSE 211 - Team 10
 * Winter 2017
 * 
 * US Controller interface
 */


public interface UltrasonicController {
	
	public void processUSData(int distance);
	
	public int readUSDistance();
}
