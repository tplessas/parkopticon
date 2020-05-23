package afx.parkopticon.pbew;

import java.io.IOException;
import java.util.ArrayList;

import com.fazecast.jSerialComm.SerialPort;

public class ParkEye {

	private static ArrayList<ParkEye> eyes = new ArrayList<ParkEye>();

	private SerialPort port;
	private String uid;
	private int opmode;
	private String rfuid;

	private boolean occ;
	private int infract = 0;
	private String readid;

	private boolean rfid_health = true;
	private boolean sr04_health = true;

	public ParkEye(SerialPort port, String uid, int opmode, String rfuid) {
		super();
		this.port = port;
		this.uid = uid;
		this.opmode = opmode;
		this.rfuid = rfuid;
		eyes.add(this);
	}

	public static boolean initlatch = true;
	public static SerialPort[] peri;

	public static void init() {
		peri = SerialPort.getCommPorts();
		initlatch = false;
		for (SerialPort p : peri) {
			p.openPort();
			SerialEngine handler = new SerialEngine();
			p.addDataListener(handler);
		}
	}
	
	public static void deinit() {
		for (SerialPort p : peri) {
			try {
				p.getInputStream().close();
				p.getOutputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			p.removeDataListener();
			p.closePort();
		}
	}

	public static ArrayList<ParkEye> getEyes() {
		return eyes;
	}

	public static void setEyes(ArrayList<ParkEye> eyes) {
		ParkEye.eyes = eyes;
	}

	public SerialPort getPort() {
		return port;
	}

	public int getInfract() {
		return infract;
	}

	public void setInfract(int infract) {
		this.infract = infract;
	}

	public void setPort(SerialPort port) {
		this.port = port;
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

	public boolean isSr04_health() {
		return sr04_health;
	}

	public void setSr04_health(boolean sr04_health) {
		this.sr04_health = sr04_health;
	}

	public String getReadid() {
		return readid;
	}

	public void setReadid(String readid) {
		this.readid = readid;
	}

	@Override
	public String toString() {
		return "ParkEye [port=" + port + ", uid=" + uid + ", opmode=" + opmode + ", rfuid=" + rfuid + ", occ=" + occ
				+ ", infract=" + infract + ", rfid_health=" + rfid_health + ", sr04_health=" + sr04_health + "]";
	}

}
