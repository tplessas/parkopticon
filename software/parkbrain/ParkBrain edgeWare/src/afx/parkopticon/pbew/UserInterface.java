package afx.parkopticon.pbew;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;

public class UserInterface extends Thread {

	static Screen screen = null;
	static MultiWindowTextGUI gui = null;

	public void run() {
		DefaultTerminalFactory factory = new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8"))
				.setInitialTerminalSize(new TerminalSize(150, 50)).setTerminalEmulatorTitle("Artifex PBSW 0.1a");
		try {
			screen = factory.createScreen();
			// screen = new TerminalScreen(new UnixTerminal(System.in, System.out,
			// Charset.forName("UTF8")));
			screen.startScreen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		gui = new MultiWindowTextGUI(screen);

		gui.setTheme(com.googlecode.lanterna.bundle.LanternaThemes.getRegisteredTheme("default"));
		StartupWindow startup = new StartupWindow();
		startup.setHints(Arrays.asList(Window.Hint.CENTERED));
		gui.addWindowAndWait(startup);
		MainView main = new MainView();
		main.setHints(Arrays.asList(Window.Hint.CENTERED));
		gui.addWindowAndWait(main);
		try {
			screen.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

class StartupWindow extends BasicWindow {

	public StartupWindow() {
		super("");
		Thread t;
		t = new Thread() {
			public void run() {
				Panel contentPanel = new Panel(new GridLayout(2));
				GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
				gridLayout.setHorizontalSpacing(3);

				Label name = new Label("Artifex Electronics");
				name.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, // Horizontal alignment in
																							// the
																							// grid cell if the cell is
																							// larger than the
																							// component's preferred
																							// size
						GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger
														// than the component's preferred size
						true, // Give the component extra horizontal space if available
						false, // Give the component extra vertical space if available
						2, // Horizontal span
						1)); // Vertical spanF

				Label logo = new Label(
						"  _____           _    ____            _                  _         __          __            \r\n" + 
						" |  __ \\         | |  |  _ \\          (_)                | |        \\ \\        / /            \r\n" + 
						" | |__) |_ _ _ __| | _| |_) |_ __ __ _ _ _ __     ___  __| | __ _  __\\ \\  /\\  / /_ _ _ __ ___ \r\n" + 
						" |  ___/ _` | '__| |/ /  _ <| '__/ _` | | '_ \\   / _ \\/ _` |/ _` |/ _ \\ \\/  \\/ / _` | '__/ _ \\\r\n" + 
						" | |  | (_| | |  |   <| |_) | | | (_| | | | | | |  __/ (_| | (_| |  __/\\  /\\  / (_| | | |  __/\r\n" + 
						" |_|   \\__,_|_|  |_|\\_\\____/|_|  \\__,_|_|_| |_|  \\___|\\__,_|\\__, |\\___| \\/  \\/ \\__,_|_|  \\___|\r\n" + 
						"                                                             __/ |                            \r\n" + 
						"                                                            |___/                             ");
				logo.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER,
						true, false, 2, 1));

				Label version = new Label("Version 0.1a\n");
				version.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
						false, false, 1, 1));

				Label senum = new Label("Enumerating serial ports...");
				version.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
						false, false, 1, 1));

				Label sinit = new Label("Initializing ParkEye instances...");
				version.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
						false, false, 1, 1));

				Label ok = new Label("All systems nominal!");
				version.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
						false, false, 1, 1));

				contentPanel.addComponent(name);
				contentPanel.addComponent(logo);
				contentPanel.addComponent(version);
				contentPanel.addComponent(senum);
				setComponent(contentPanel);
				while (ParkEye.initlatch) {
				}

				contentPanel.removeComponent(senum);
				contentPanel.addComponent(sinit);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				contentPanel.removeComponent(sinit);
				contentPanel.addComponent(ok);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				close();
			}
		};
		t.start();
	}
}

class MainView extends BasicWindow {

	public static Table<String> table = new Table<String>("UID", "Status", "Infraction", "Health", "Opmode", "RFID");

	public MainView() {
		super("System Overview");
		Thread t;
		t = new Thread() {
			public void run() {
				Panel contentPanel = new Panel(new GridLayout(2));
				GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
				gridLayout.setHorizontalSpacing(3);
				ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

				Button about = new Button("About", new Runnable() {
					@Override
					public void run() {
						MessageDialog.showMessageDialog(UserInterface.gui, "About",
								"ParkBrain edgeWare v0.1a\n\nProof-of-concept implementation of\n"
										+ "edge node software for Parkopticon.\n\n(c) 2020 Artifex Electronics\nDistributed under the terms of the MIT License.\n\nThird-party software:\nFazecast jSerialComm@2.6.1\n"
										+ "Copyright (C) 2012-2019 Fazecast, Inc.\nDistributed under the terms of the\nGNU Lesser General Public License, version 3.\n\n"
										+ "lanterna@3.0.2\nCopyright (C) 2010-2020 Martin Berglund\nDistributed under the terms of the\nGNU Lesser General Public License, version 3.",
								MessageDialogButton.valueOf("OK"));
					}
				});

				Button exit = new Button("Exit", new Runnable() {
					@Override
					public void run() {
						executor.shutdown();
						ParkEye.deinit();
						close();
					}
				});

				contentPanel.addComponent(about);
				contentPanel.addComponent(exit);

				table.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER,
						GridLayout.Alignment.CENTER, true, true, 2, 1));
				table.setPreferredSize(new TerminalSize(80, 10));
				contentPanel.addComponent(table);
				setComponent(contentPanel);
				table.getTableModel().addRow("1", "1", "1", "1", "1", "1"); //So that tableUpdate can clear the first time without exceptions.

				Runnable tableUpdate = new Runnable() {
					public void run() {
						table.getTableModel().clear();
						for (ParkEye e : ParkEye.getEyes()) {
							String occ = e.isOcc() ? "Occupied" : "Free";
							String infract = null;
							switch (e.getInfract()) {
							case 0:
								infract = "None";
								break;
							case 1:
								infract = "UNAUTHPK";
								break;
							case 2:
								infract = "NOCARD";
								break;
							case 3:
								infract = "ILLGLTAG";
								break;
							}
							String opmode = null;
							switch (e.getOpmode()) {
							case 0:
								opmode = "Basic";
								break;
							case 1:
								opmode = "RFID Stored";
								break;
							case 2:
								opmode = "RFID Remote";
								break;
							}
							String health = null;
							if (!e.isRfid_health()) {
								health = "RFID FAIL";
							} else if (!e.isSr04_health()) {
								health = "SR04 FAIL";
							} else {
								health = "Good";
							}

							table.getTableModel().addRow(e.getUid(), occ, infract, health, opmode, e.getRfuid());
						}
					}
				};
				executor.scheduleAtFixedRate(tableUpdate, 0, 1, TimeUnit.SECONDS);

				Panel connsPanel = new Panel(new GridLayout(1));
				connsPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.CENTER, false, false, 1, 1));
				contentPanel.addComponent(connsPanel);
				Runnable connsUpdate = new Runnable() {
					public void run() {
						Label conns = new Label("Current connections: " + String.valueOf(ParkEye.getEyes().size()));
						conns.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
								GridLayout.Alignment.CENTER, false, false, 1, 1));
						connsPanel.addComponent(conns);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						connsPanel.removeComponent(conns);
					}
				};
				executor.scheduleAtFixedRate(connsUpdate, 0, 1, TimeUnit.SECONDS);

				Panel timePanel = new Panel(new GridLayout(1));
				timePanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END,
						GridLayout.Alignment.CENTER, false, false, 1, 1));
				contentPanel.addComponent(timePanel);
				Runnable timeUpdate = new Runnable() {
					public void run() {
						Label time = new Label(ZonedDateTime.now(ZoneId.systemDefault())
								.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
						time.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END,
								GridLayout.Alignment.CENTER, false, false, 1, 1));
						timePanel.addComponent(time);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						timePanel.removeComponent(time);
					}
				};
				executor.scheduleAtFixedRate(timeUpdate, 0, 1, TimeUnit.SECONDS);
			}
		};
		t.start();
	}
}
