package android_serialport_api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by han.chen.
 * Date on 2019-08-17.
 **/
public class SerialPortManager {

    public static SerialPortManager sSerialPortManager;

    public static SerialPortManager getInstance() {
        if (sSerialPortManager == null) {
            synchronized (SerialPortManager.class) {
                if (sSerialPortManager == null) {
                    sSerialPortManager = new SerialPortManager();
                }
            }
        }
        return sSerialPortManager;
    }

    private SerialPortManager() {

    }

    public void initSerialPort() {
        try {
            SerialPort serialPort = new SerialPort(new File("/dev/" + "ttyMT1"), 2400, 0);
            mOutputStream = (FileOutputStream) serialPort.getOutputStream();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileOutputStream mOutputStream;
    public static final byte[] TURN_LEFT = {(byte) 0xff, 0x01, 0x00, 0x04, (byte) 0xff, 0x00, 0x04};
    public static final byte[] TURN_RIGHT = {(byte) 0xff, 0x01, 0x00, 0x02, (byte) 0xff, 0x00, 0x02};
    public static final byte[] STOP = {(byte) 0xff, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01};

    public void writeData(byte[] writeBytes) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(writeBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
