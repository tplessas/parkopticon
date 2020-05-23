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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String[] messages;

			messages = in.readLine().split("\\*");
			ParkEye eye = ParkEye.getEyeByUID(messages[0]);
			int battery = Integer.parseInt(messages[1]);
			if (eye == null) { // get config and create new ParkEye
				out.print("CONFIG?\n");
				out.flush();
				String config = in.readLine();
				int opmode = Character.getNumericValue(config.charAt(0));
				String rfuid = config.substring(1, 12);
				eye = new ParkEye(messages[0], opmode, rfuid);
				eye.setBatterypercent(battery);
				parseMessages(eye, messages);
			} else {
				eye.setBatterypercent(battery);
				parseMessages(eye, messages);
				if (eye.getConfigbuffer() != null) { // send new config
					out.print("NEWCONFIG\n");
					out.flush();
					out.println(eye.getConfigbuffer());
					eye.setConfigbuffer(null);
				}
			}
			out.println("OK");
			socket.close();
		} catch (IOException ex) {
		}
	}

	private void parseMessages(ParkEye eye, String[] messages) {
		for (int i = 2; i < messages.length; i++) {
			String msg = messages[i];
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
}