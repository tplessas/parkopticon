/*
   ParkBrain edgeWare WiFi
   Created by Theodoros Plessas (8160192) for Artifex Electronics

   MIT License

   Copyright (c) 2020 Theodoros Plessas

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

package afx.parkopticon.pbewifi;

import java.util.ArrayList;

public class ParkEye {

	private static ArrayList<ParkEye> eyes = new ArrayList<ParkEye>();

	private String uid;
	private int opmode;
	private String rfuid;

	private boolean occ;
	private int infract = 0;
	//private String readid;
	private int batterypercent;

	private boolean rfid_health = true;
	
	private String configbuffer;

	public ParkEye(String uid, int opmode, String rfuid) {
		super();
		setUid(uid);
		setOpmode(opmode);
		setRfuid(rfuid);
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

	@Override
	public String toString() {
		return "ParkEye [uid=" + uid + ", opmode=" + opmode + ", rfuid=" + rfuid + ", occ=" + occ
				+ ", infract=" + infract + ", rfid_health=" + rfid_health + "]";
	}

}
