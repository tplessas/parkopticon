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
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			String input;
			String uid;
			String[] messages;

			input = reader.readLine();
			uid = input.split("*")[0];
			messages =  input.split("*");
			for (String msg : messages) {
				if (msg.length() == 18) {
					uid = msg.substring(0, 5);

					int opmode = Character.getNumericValue(msg.charAt(5));

					String rfuid = msg.substring(6, 17);

					System.out.println(uid + " " + opmode + " " + rfuid + " ");
				} else if (msg.equals("OCCD*")) {
					System.out.println("OCCD*");
				} else if (msg.equals("FREE*")) {
					System.out.println("FREE*");
				} else {
					if (msg.equals("ALLGOOD*")) {
						System.out.println("ALLGOOD*");
					} else if (msg.equals("UNAUTHPK*")) {
						System.out.println("UNAUTHPK*");
					} else if (msg.equals("NOCARD*")) {
						System.out.println("NOCARD*");
					} else if (msg.equals("ILLGLTAG*")) {
						System.out.println("ILLGLTAG*");
					} else if (msg.equals("RFIDFAIL*")) {
						System.out.println("RFIDFAIL*");
					} else {
					}
				}
			}

			socket.close();
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}