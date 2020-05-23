package afx.parkopticon.pbewifi;

import java.util.ArrayList;

public class ParkEye {

	private static ArrayList<ParkEye> eyes = new ArrayList<ParkEye>();

	private String uid;
	private int opmode;
	private String rfuid;

	private boolean occ;
	private int infract = 0;
	private String readid;
	private int batterypercent;

	private boolean rfid_health = true;
	
	private String configbuffer;

	public ParkEye(String uid, int opmode, String rfuid) {
		super();
		this.uid = uid;
		this.opmode = opmode;
		this.rfuid = rfuid;
		eyes.add(this);
	}
	
	public static ParkEye getEyeByUID(String input) {
		for(ParkEye eye : eyes) {
			if(input.equals(eye.getUid())) {
				return eye;
			}
		}
		return null;
	}

	public static ArrayList<ParkEye> getEyes() {
		return eyes;
	}

	public static void setEyes(ArrayList<ParkEye> eyes) {
		ParkEye.eyes = eyes;
	}

	public int getBatterypercent() {
		return batterypercent;
	}

	public void setBatterypercent(int batterypercent) {
		this.batterypercent = batterypercent;
	}

	public String getConfigbuffer() {
		return configbuffer;
	}

	public void setConfigbuffer(String configbuffer) {
		this.configbuffer = configbuffer;
	}

	public int getInfract() {
		return infract;
	}

	public void setInfract(int infract) {
		this.infract = infract;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getOpmode() {
		return opmode;
	}

	public void setOpmode(int opmode) {
		this.opmode = opmode;
	}

	public String getRfuid() {
		return rfuid;
	}

	public void setRfuid(String rfuid) {
		this.rfuid = rfuid;
	}

	public boolean isOcc() {
		return occ;
	}

	public void setOcc(boolean occ) {
		this.occ = occ;
	}

	public boolean isRfid_health() {
		return rfid_health;
	}

	public void setRfid_health(boolean rfid_health) {
		this.rfid_health = rfid_health;
	}

	public String getReadid() {
		return readid;
	}

	public void setReadid(String readid) {
		this.readid = readid;
	}

	@Override
	public String toString() {
		return "ParkEye [uid=" + uid + ", opmode=" + opmode + ", rfuid=" + rfuid + ", occ=" + occ
				+ ", infract=" + infract + ", rfid_health=" + rfid_health + "]";
	}

}
