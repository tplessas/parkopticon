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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

	private ServerSocket serverSocket;

	public void run() {

		try {
			serverSocket = new ServerSocket(42069);
			System.out.println("oof");

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("ouch");

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