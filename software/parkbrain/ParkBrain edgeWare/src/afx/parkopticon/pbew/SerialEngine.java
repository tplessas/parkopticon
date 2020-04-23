package afx.parkopticon.pbew;

import java.io.UnsupportedEncodingException;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public final class SerialEngine implements SerialPortMessageListener {

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
	}

	@Override
	public byte[] getMessageDelimiter() {
		return new byte[] { '*' };
	}

	@Override
	public boolean delimiterIndicatesEndOfMessage() {
		return true;
	}

	public void serialEvent(SerialPortEvent event) {
		byte[] msg;
		msg = event.getReceivedData();

		if (msg.length == 18) {
			String uid = new String(msg, 0, 5);

			byte[] temp = new byte[1];
			temp[0] = msg[5];
			int opmode = 0;
			try {
				opmode = Integer.parseInt(new String(temp, "us-ascii"));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			String rfuid = new String(msg, 6, 11);

			new ParkEye(event.getSerialPort(), uid, opmode, rfuid);
		} else if (new String(msg).equals("OCCD*")) {
			for (int i = 0; i < ParkEye.getEyes().size(); i++) {
				if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
					ParkEye.getEyes().get(i).setOcc(true);
				}
			}
		} else if (new String(msg).equals("FREE*")) {
			for (int i = 0; i < ParkEye.getEyes().size(); i++) {
				if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
					ParkEye.getEyes().get(i).setOcc(false);
					ParkEye.getEyes().get(i).setInfract(0);
				}
			}
		} else if (new String(msg).equals("SR04FAIL*")) {
			for (int i = 0; i < ParkEye.getEyes().size(); i++) {
				if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
					ParkEye.getEyes().get(i).setSr04_health(false);
				}
			}
		} else if (new String(msg).equals("SR04PASS*")) {
		} else {
			if (new String(msg).equals("ALLGOOD*")) {
			} else if (new String(msg).equals("UNAUTHPK*")) {
				for (int i = 0; i < ParkEye.getEyes().size(); i++) {
					if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
						ParkEye.getEyes().get(i).setInfract(1);
					}
				}
			} else if (new String(msg).equals("NOCARD*")) {
				for (int i = 0; i < ParkEye.getEyes().size(); i++) {
					if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
						ParkEye.getEyes().get(i).setInfract(2);
					}
				}
			} else if (new String(msg).equals("ILLGLTAG*")) {
				for (int i = 0; i < ParkEye.getEyes().size(); i++) {
					if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
						ParkEye.getEyes().get(i).setInfract(3);
					}
				}
			} else if (new String(msg).equals("RFIDFAIL*")) {
				for (int i = 0; i < ParkEye.getEyes().size(); i++) {
					if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
						ParkEye.getEyes().get(i).setRfid_health(false);
					}
				}
			} else {
				for (int i = 0; i < ParkEye.getEyes().size(); i++) {
					if (ParkEye.getEyes().get(i).getPort().equals(event.getSerialPort())) {
						ParkEye.getEyes().get(i).setReadid(new String(msg));
					}
				}
			}
		}
	}
}