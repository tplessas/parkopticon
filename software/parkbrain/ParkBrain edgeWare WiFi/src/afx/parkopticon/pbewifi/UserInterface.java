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

/*
									!!!READER BEWARE!!!
	Do not, under threat of madness, attempt to read this code without a drink in hand.
									 It is *un-god-ly*.
 */

package afx.parkopticon.pbewifi;

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
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
//import com.googlecode.lanterna.screen.TerminalScreen;
//import com.googlecode.lanterna.terminal.ansi.UnixTerminal;

public class UserInterface extends Thread {

	private static Screen screen = null;
	private static MultiWindowTextGUI gui = null;
	private static final String VERSION_NUMBER = "0.2a";

	public void run() {
		DefaultTerminalFactory factory = new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8"))
				.setInitialTerminalSize(new TerminalSize(150, 50))
				.setTerminalEmulatorTitle("Artifex PBeWiFi " + VERSION_NUMBER);
		try {
			screen = factory.createScreen();
			// screen = new TerminalScreen(new UnixTerminal(System.in, System.out,
			// Charset.forName("UTF8")));
			screen.startScreen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setGui(new MultiWindowTextGUI(screen));

		getGui().setTheme(com.googlecode.lanterna.bundle.LanternaThemes.getRegisteredTheme("default"));
		StartupWindow startup = new StartupWindow();
		startup.setHints(Arrays.asList(Window.Hint.CENTERED));
		getGui().addWindowAndWait(startup);
		MainView main = new MainView();
		main.setHints(Arrays.asList(Window.Hint.CENTERED));
		getGui().addWindowAndWait(main);
		try {
			screen.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static MultiWindowTextGUI getGui() {
		return gui;
	}

	public static void setGui(MultiWindowTextGUI gui) {
		UserInterface.gui = gui;
	}

	public static String getVersionNumber() {
		return VERSION_NUMBER;
	}
}

class StartupWindow extends BasicWindow {

	public StartupWindow() {
		super("");
		Thread t;
		t = new Thread() {
			public void run() {
				Panel contentPanel = new Panel(new GridLayout(2));

				Label name = new Label("Artifex Electronics");
				name.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, // Horizontal alignment in
																							// the
																							// grid cell if the cell is
																							// larger than the
																							// component's preferred
																							// size
						GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell if the cell is larger
														// than the component's preferred size
						false, // Give the component extra horizontal space if available
						false, // Give the component extra vertical space if available
						2, // Horizontal span
						1)); // Vertical spanF

				Label logo = new Label(
						"  _____           _    ____            _                  _         __          __            \r\n"
								+ " |  __ \\         | |  |  _ \\          (_)                | |        \\ \\        / /            \r\n"
								+ " | |__) |_ _ _ __| | _| |_) |_ __ __ _ _ _ __     ___  __| | __ _  __\\ \\  /\\  / /_ _ _ __ ___ \r\n"
								+ " |  ___/ _` | '__| |/ /  _ <| '__/ _` | | '_ \\   / _ \\/ _` |/ _` |/ _ \\ \\/  \\/ / _` | '__/ _ \\\r\n"
								+ " | |  | (_| | |  |   <| |_) | | | (_| | | | | | |  __/ (_| | (_| |  __/\\  /\\  / (_| | | |  __/\r\n"
								+ " |_|   \\__,_|_|  |_|\\_\\____/|_|  \\__,_|_|_| |_|  \\___|\\__,_|\\__, |\\___| \\/  \\/ \\__,_|_|  \\___|\r\n"
								+ "                                                             __/ |                            \r\n"
								+ "                                                            |___/                             ");
				logo.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER,
						true, true, 2, 1));

				Label version = new Label("Version " + (String) UserInterface.getVersionNumber());
				version.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.CENTER, true, true, 1, 1));

				Label sinit = new Label("Initializing PMP server...");
				sinit.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
						true, true, 1, 1));

				Label ok = new Label("All systems nominal!");
				ok.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
						true, true, 1, 1));

				contentPanel.addComponent(name);
				contentPanel.addComponent(logo);
				contentPanel.addComponent(version);
				contentPanel.addComponent(sinit);
				setComponent(contentPanel);
				try {
					Thread.sleep(1000);
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

	private static Table<String> table = new Table<String>("UID", "Status", "Infraction", "Health", "Opmode", "Battery",
			"RFID");

	public MainView() {
		super("System Overview");
		Thread t;
		t = new Thread() {
			public void run() {
				Panel contentPanel = new Panel(new GridLayout(2));
				ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

				Button about = new Button("About", new Runnable() {
					@Override
					public void run() {
						MessageDialog.showMessageDialog(UserInterface.getGui(), "About", "ParkBrain edgeWare WiFi "
								+ UserInterface.getVersionNumber() + "\n\nProof-of-concept implementation of\n"
								+ "edge node software for Parkopticon.\n\n(c) 2020 Theodoros Plessas for Artifex Electronics\nDistributed under the terms of the MIT License.\n\nThird-party software:\n"
								+ "lanterna@3.0.2\nCopyright (C) 2010-2020 Martin Berglund\nDistributed under the terms of the\nGNU Lesser General Public License, version 3.",
								MessageDialogButton.valueOf("OK"));
					}
				});

				Button exit = new Button("Exit", new Runnable() {
					@Override
					public void run() {
						executor.shutdown();
						App.getServer().shutdown();
						close();
					}
				});

				contentPanel.addComponent(about);
				contentPanel.addComponent(exit);

				table.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER,
						GridLayout.Alignment.CENTER, true, true, 2, 1));
				table.setPreferredSize(new TerminalSize(80, 10));
				table.setEscapeByArrowKey(true);
				contentPanel.addComponent(table);
				setComponent(contentPanel);
				table.getTableModel().addRow("1", "1", "1", "1", "1", "1", "1"); // Placates
																					// table.getTableModel().clear() the
																					// first time.

				Runnable tableUpdate = new Runnable() {
					public void run() {
						table.getTableModel().clear();
						for (ParkEye eye : Sensors.getEyes()) {
							String occ = eye.isOcc() ? "Occupied" : "Free";
							String infract = null;
							switch (eye.getInfract()) {
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
							switch (eye.getOpmode()) {
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
							String health = eye.isRfid_health() ? "Good" : "RFID FAIL";

							table.getTableModel().addRow(eye.getUid(), occ, infract, health, opmode,
									eye.getBatterypercent() + "%", eye.getRfuid());
						}
					}
				};
				executor.scheduleAtFixedRate(tableUpdate, 0, 1, TimeUnit.SECONDS);

				Runnable itemSelect = new Runnable() {
					public void run() {
						if (table.getTableModel().getRowCount() > 0) {
							String uid = table.getTableModel().getCell(0, table.getSelectedRow());
							Runnable newConfig = new Runnable() {
								public void run() {
									ConfigWizard wiz = new ConfigWizard(uid);
									wiz.setCloseWindowWithEscape(true);
									wiz.setHints(Arrays.asList(Window.Hint.CENTERED));
									UserInterface.getGui().addWindow(wiz);
								}

								public String toString() {
									return "New config";
								}
							};
							ActionListDialog.showDialog(UserInterface.getGui(), uid, "", newConfig);
						}
					}
				};
				table.setSelectAction(itemSelect);

				Panel connsPanel = new Panel(new GridLayout(1));
				connsPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));
				contentPanel.addComponent(connsPanel);
				Runnable connsUpdate = new Runnable() {
					public void run() {
						Label conns = new Label("ParkEyes connected: " + String.valueOf(Sensors.getEyes().size()));
						conns.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
								GridLayout.Alignment.BEGINNING, true, true, 1, 1));
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
						GridLayout.Alignment.CENTER, true, true, 1, 1));
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

class ConfigWizard extends BasicWindow {

	public ConfigWizard(String uid) {
		super("New config");
		Thread t = new Thread() {
			public void run() {
				Panel contentPanel = new Panel(new GridLayout(2));
				GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
				gridLayout.setVerticalSpacing(1);

				Label description = new Label(
						"The new config will be staged and delivered\nthe next time the ParkEye contacts the server.");
				description.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 2, 1));

				Label uidLabel = new Label("UID:");
				uidLabel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));

				Label uidValue = new Label(uid);
				uidValue.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));

				Label opmodeLabel = new Label("Opmode:");
				opmodeLabel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));

				String[] opmodeChoices = { "0. Basic", "1. RFID Stored", "2. RFID Remote" };

				ComboBox<String> opmodeBox = new ComboBox<String>(opmodeChoices);
				opmodeBox.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));

				Label rfidLabel = new Label("RFID UID:");
				rfidLabel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));

				TextBox rfidBox = new TextBox(new TerminalSize(12, 1));
				rfidBox.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING, true, true, 1, 1));

				Button abort = new Button("Abort", new Runnable() {
					@Override
					public void run() {
						close();
					}
				});
				Button commit = new Button("Commit", new Runnable() {
					@Override
					public void run() {
						if (opmodeBox.getSelectedIndex() == 1) {
							if (rfidBox.getText().length() != 11) {
								MessageDialog.showMessageDialog(UserInterface.getGui(), "Error",
										"RFID UID should be 11 characters long.", MessageDialogButton.valueOf("OK"));
							} else {
								ParkEye eye = Sensors.getEyeByUID(uid);
								eye.setConfigbuffer(Integer.toString(opmodeBox.getSelectedIndex()) + rfidBox.getText());
								close();
							}
						} else {
							if (rfidBox.getText().length() != 0) {
								MessageDialog.showMessageDialog(UserInterface.getGui(), "Error",
										"Selected opmode does not support RFID UID.",
										MessageDialogButton.valueOf("OK"));
							} else {
								ParkEye eye = Sensors.getEyeByUID(uid);
								eye.setConfigbuffer(Integer.toString(opmodeBox.getSelectedIndex()) + "00 00 00 00");
								close();
							}
						}
					}
				});

				contentPanel.addComponent(description);
				contentPanel.addComponent(uidLabel);
				contentPanel.addComponent(uidValue);
				contentPanel.addComponent(opmodeLabel);
				contentPanel.addComponent(opmodeBox);
				contentPanel.addComponent(rfidLabel);
				contentPanel.addComponent(rfidBox);
				contentPanel.addComponent(abort);
				contentPanel.addComponent(commit);
				setComponent(contentPanel);
			}
		};
		t.start();
	}
}
