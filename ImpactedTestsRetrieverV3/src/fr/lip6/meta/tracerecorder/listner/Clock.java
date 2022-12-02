package fr.lip6.meta.tracerecorder.listner;

public class Clock {
	private static int timestamp = 0;
	public static int getNextTimestamp() {
		return timestamp++;
	}
	
	public static int getCurrentTimestamp() {
		return timestamp;
	}
}
