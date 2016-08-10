package fingerprint;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class Module {

    public OutputStream output = null;
    public BufferedInputStream input = null;
    public byte[] reply;
    public byte[] fingerRaw;
    public byte[] charfile;
    public String errmsg;
    public SerialPort serialPort;
    public byte[] databuffer;

    //users privilege categories
    public static final byte STUDENT = 0x01;
    public static final byte PROFESSOR = 0x02;
    public static final byte EMPLOYEE = 0x03;

    //device name for DSP module serial communication
    //public static final String DEVICENAME = "COM3";
    public static final String DEVICENAME = "/dev/ttyAMA0";
    //timeout for DSP module serial communication
    public static final int WAITTIME = 2000;
    //baud rate for DSP module serial communication
    public static final int DEFAULTBAUDRATE = 19200;
    //number of attempt after bad reply
    public static final int ATTEMPT = 5;
    //timeout for DSP module reply
    public static final int REPLYWAITTIME = 700;
    //timeout for data pkg
    public static final int DATABREAKWAITTIME = 500;

    public Module() {
    }

    public void connect(String portName) {
        try {
            System.setProperty("gnu.io.rxtx.SerialPorts", portName);
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");
            } else {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), WAITTIME);
                if (commPort instanceof SerialPort) {
                    serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(DEFAULTBAUDRATE,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    input = new BufferedInputStream(serialPort.getInputStream());
                    output = serialPort.getOutputStream();
                } else {
                    System.out.println("Error: Only serial ports are handled.");
                }
            }
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException ex) {
            Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean getdata() {
        boolean term = true;
        int contador = 0;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        long lastreceived = System.currentTimeMillis();
        while (term) {
            try {
                if (input.available() > 0) {
                    while (input.available() > 0) {
                        contador++;
                        byte in = (byte) input.read();
                        if ((in != (byte) 0xF5) && (contador > 1)) {
                            data.write(in);
                        } else if ((in == (byte) 0xF5) && (contador > 1)) {
                            //Last byte
                            databuffer = data.toByteArray();
                            term = false;
                        }
                    }
                    lastreceived = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - lastreceived > DATABREAKWAITTIME) {
                    return false;
                }
            } catch (IOException ex) {
                Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    public short getCksum(byte arg1, byte arg2, byte arg3, byte arg4) {
        short ret = (short) (arg1 ^ arg2 ^ arg3 ^ arg4);
        return ret;
    }

    public byte[] getReply(int len) {
        int count = 0;
        ByteBuffer buf = ByteBuffer.allocate(len);
        long start = System.currentTimeMillis();
        while (count != len) {
            try {
                if (System.currentTimeMillis() - start > REPLYWAITTIME) {
                    /*
                    This reply exist in case the DSP module could not respond
                    This reply array is full with 0x01 and reply[4]=0x08 for ACK_TIMEOUT
                     */
                    reply = (new byte[]{(byte) 0x01,
                        (byte) 0x01,
                        (byte) 0x01,
                        (byte) 0x01,
                        (byte) 0x08,
                        (byte) 0x01,
                        (byte) 0x01,
                        (byte) 0x01});
                    return reply;
                }
                while (input.available() > 0 && count != len) {
                    count++;
                    buf.put((byte) input.read());
                }
            } catch (IOException ex) {
                Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        reply = buf.array();
        return reply;
    }

    public void close() {
        try {
            output.close();
            input.close();
            serialPort.close();
        } catch (IOException ex) {
            Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] intToByteArray(int userIntID) {
        return new byte[]{
            (byte) (userIntID >>> 24),
            (byte) (userIntID >>> 16),
            (byte) (userIntID >>> 8),
            (byte) userIntID};
    }

    public int fromByteArray(byte[] bytes) {
        return bytes[0] << 24
                | (bytes[1] & 0xFF) << 16
                | (bytes[2] & 0xFF) << 8
                | (bytes[3] & 0xFF);
    }

    //method 1.6 get number of users in DSP module database
    public int getTotalNumber() {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x09,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x0:
                        break;
                    case 0x1:
                        errmsg = "error: ACK_FAIL";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        byte[] bytes = new byte[]{0x00, 0x00, replycode[2], replycode[3]};
        int totalUsers = fromByteArray(bytes);
        return totalUsers;
    }

    //method 1.9 get privilege from specified user
    public int getUserPrivilege(byte[] userID) {
        byte userID_Hi = userID[2];
        byte userID_Low = userID[3];
        boolean term = true;
        byte[] replycode;
        short cksum = getCksum((byte) 0x0A, userID_Hi, userID_Low, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x0A,
                    userID_Hi,
                    userID_Low,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        errmsg = "error: ACK_NOUSER";
                        break;
                    case 0x01:
                        return 1;
                    case 0x02:
                        return 2;
                    case 0x03:
                        return 3;
                    default:
                        errmsg = "error: UNKNOWN";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (term);
        return 0;
    }

    //method 1.19 get all users ID and privileges from DSP module database
    public byte[] getUsersList() {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x2B, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return new byte[]{};
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x2B,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        break;
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Module.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        if (getdata()) {
            charfile = new byte[databuffer.length];
            System.arraycopy(databuffer, 0, charfile, 0, databuffer.length);
            return charfile;
        }
        return new byte[]{};
    }
}
