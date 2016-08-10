package fingerprint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class SystemClass extends Module {

    public SystemClass() {
        //this.connect(DEVICENAME);
    }

    public boolean enableDormantState() {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x2C,
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
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        System.out.println("Dormant State Enabled");
        return true;
    }

    //method 1.4 delete specified user, this remove all DSP module data from a single user 
    public boolean deleteUser(byte[] userID) {
        byte userID_Hi = userID[2];
        byte userID_Low = userID[3];
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x04, userID_Hi, userID_Low, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x04,
                    userID_Hi,
                    userID_Low,
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
                        errmsg = "error: NO_USER";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return true;
    }

    //method 1.5 delete all users from DSP module database
    public boolean deleteAll() {
        short cksum = getCksum((byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte[] replycode = null;
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x05,
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
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return true;
    }

    //method 1.2 read fingerprint mode, this is allow repeat(0) or prohibit repeat(1)
    public int getFingerprintMode() {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x2D, (byte) 0x00, (byte) 0x00, (byte) 0x01);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x2D,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x01,
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
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        if (replycode[3] == 1) {
            System.out.println("Prohibit repeat mode");
            return 1;
        }
        //else if (replycode[3] == 0) {
        System.out.println("Allow repeat mode");
        return 0;
    }

    //method 1.2 set fingerprint mode, this is allow repeat(0) or prohibit repeat(1)
    public boolean setFingerprintMode(int fingerprintModeValue) {
        byte fingerprintMode = (byte) fingerprintModeValue;
        byte[] replycode = null;
        byte attempt = 0;
        short cksum = getCksum((byte) 0x2D, (byte) 0x00, fingerprintMode, (byte) 0x00);
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x2D,
                    (byte) 0x00,
                    fingerprintMode,
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
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return true;
    }

    //method 1.11 read comparison level, how rigorous is fingerprint acquirement (0-9)
    public int getComparisonLevel() {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x01);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x28,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x01,
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
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return (int) replycode[3];
    }

    //method 1.11 set comparison level, how rigorous is fingerprint acquirement (0-9)
    public boolean setComparisonLevel(int comparisonLevel) {
        byte[] replycode = null;
        byte comparison = (byte) comparisonLevel;
        short cksum = getCksum((byte) 0x28, (byte) 0x00, comparison, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x28,
                    (byte) 0x00,
                    comparison,
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
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return true;
    }

    //method 1.25 read fingerprint capture timeout, this value could be 0-255
    public int getFingerprintTimeout() {
        int fingerprintTimeout;
        byte[] replycode = null, bytes;
        short cksum = getCksum((byte) 0x2E, (byte) 0x00, (byte) 0x00, (byte) 0x01);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x2E,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x01,
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
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        bytes = new byte[]{0x00, 0x00, 0x00, replycode[3]};
        fingerprintTimeout = fromByteArray(bytes);
        return fingerprintTimeout;
    }

    //method 1.25 set fingerprint capture timeout, this value could be 0-255
    public boolean setFingerprintTimeout(int timeoutValue) {
        byte timeout = (byte) timeoutValue;
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x2E, (byte) 0x00, timeout, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x2E,
                    (byte) 0x00,
                    timeout,
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
                Logger.getLogger(SystemClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return true;
    }
}
