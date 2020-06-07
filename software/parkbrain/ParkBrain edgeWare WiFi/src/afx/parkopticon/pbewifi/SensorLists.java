package afx.parkopticon.pbewifi;

import java.util.ArrayList;

public class SensorLists {
	
	private static ArrayList<ParkEye> eyes = new ArrayList<ParkEye>();
	
	public static ParkEye getEyeByUID(String input) {
		for(ParkEye eye : eyes) {
			if(input.equals(eye.getUid())) {
				return eye;
			}
		}
		return null;
	}
	
	public static void addEye(ParkEye eye) {
		eyes.add(eye);
	}
	
	public static ArrayList<ParkEye> getEyes() {
		return eyes;
	}
}
