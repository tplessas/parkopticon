package afx.parkopticon.pbewifi;

import java.io.*;
import java.net.*;

public class Server extends Thread {

	private ServerSocket serverSocket;

	public void run() {

		try {
			serverSocket = new ServerSocket(42069);

			while (true) {
				Socket socket = serverSocket.accept();

				new ServerThread(socket).start();
			}
		} catch (IOException ex) {
		}
	}

	public void shutdown() {
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}
}