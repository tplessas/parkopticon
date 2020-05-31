package afx.parkopticon.pbewifi;

public class App {

	private static Server server = new Server();
	
	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
		ui.start();
		server.start();
	}

	public static Server getServer() {
		return server;
	}
}
