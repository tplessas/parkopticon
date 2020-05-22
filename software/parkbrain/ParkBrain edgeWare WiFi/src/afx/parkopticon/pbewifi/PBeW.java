package afx.parkopticon.pbewifi;

public class PBeW {

	public static Server server = new Server();
	
	public static void main(String[] args) {	
		UserInterface ui = new UserInterface();
		ui.start();
		server.start();
	}

}
