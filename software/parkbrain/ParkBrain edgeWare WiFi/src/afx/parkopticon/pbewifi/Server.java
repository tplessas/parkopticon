package afx.parkopticon.pbewifi;

import java.io.*;
import java.net.*;
 
public class Server extends Thread {
	
	private boolean working = true;
 
    public void run(String[] args) {
 
        try (ServerSocket serverSocket = new ServerSocket(42069)) {
 
            System.out.println("Server open.");
 
            while (working) {
                Socket socket = serverSocket.accept();
                System.out.println("New message.");
 
                new ServerThread(socket).start();
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void shutdown() {
    	working = false;
    }
}