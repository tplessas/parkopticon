package afx.parkopticon.pbewifi;

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			String[] messages;
			
			messages = reader.readLine().split("\\*");
			ParkEye eye = ParkEye.getEyeByUID(messages[0]);
			int battery = Integer.parseInt(messages[1]);
			if (eye == null) {
				int opmode = Character.getNumericValue(messages[2].charAt(0));
				String rfuid = messages[2].substring(1, 12);
				ParkEye nueye = new ParkEye(messages[0], opmode, rfuid);
				nueye.setBatterypercent(battery);
				System.out.println(battery + "-");
			} else {
				eye.setBatterypercent(battery);
				System.out.println(battery + ".");
				for (String msg : messages) {
					if (msg.equals("OCCD")) {
						eye.setOcc(true);
					} else if (msg.equals("FREE")) {
						eye.setOcc(false);
						eye.setInfract(0);
					} else {
						if (msg.equals("ALLGOOD")) {
						} else if (msg.equals("UNAUTHPK")) {
							eye.setInfract(1);
						} else if (msg.equals("NOCARD")) {
							eye.setInfract(2);
						} else if (msg.equals("ILLGLTAG")) {
							eye.setInfract(3);
						} else if (msg.equals("RFIDFAIL")) {
							eye.setRfid_health(false);
						} else {
						}
					}
				}
			}
			socket.close();
		} catch (IOException ex) {
		}
	}
}