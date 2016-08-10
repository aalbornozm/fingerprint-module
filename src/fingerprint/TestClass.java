package fingerprint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class TestClass extends Module {

    public TestClass() {
        // this.connect(DEVICENAME);

    }

    //method 1.12 acquire and upload fingerprint image 
    //Fingerprinting requirement
    public byte[] getRawImageFromFingerprint(int userIntID) {
        //userIntID is just for file name
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x24, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return new byte[]{};
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x24,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                System.out.println("Put fingerprint...");
                Thread.sleep(1500);
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        break;
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    case 0x08:
                        errmsg = "error: ACK_TIMEOUT";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        if (getdata()) {
            fingerRaw = new byte[databuffer.length];
            System.arraycopy(databuffer, 0, fingerRaw, 0, databuffer.length);
            return fingerRaw;
        }
        return new byte[]{};
    }

    //method 1.13 adquire fingerprint image and upload extracted eigenvalue
    //Fingerprinting requirement
    public byte[] getEigenvalueFromFingerprint(int userIntID) {
        // userIntID is just for file name
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x23, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return new byte[]{};
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x23,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                System.out.println("Put fingerprint...");
                Thread.sleep(1500);
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        break;
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    case 0x08:
                        errmsg = "error: ACK_TIMEOUT";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        if (getdata()) {
            charfile = new byte[databuffer.length];
            System.arraycopy(databuffer, 0, charfile, 0, databuffer.length);
            return charfile;
        }
        return new byte[]{};
    }

    //method 1.7 match a fingerprint with DSP module database by comparison 1:1
    //Fingerprinting requirement
    public boolean compareFingerprint_11(byte[] userID) {
        byte userID_Hi = userID[2];
        byte userID_Low = userID[3];
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x0B, userID_Hi, userID_Low, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x0B,
                    userID_Hi,
                    userID_Low,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                System.out.println("Put fingerprint...");
                Thread.sleep(1500);
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x0:
                        System.out.println("OK!");
                        break;
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    case 0x08:
                        errmsg = "error: ACK_TIMEOUT";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x0);
        return true;
    }

    //method 1.14 send eigenvalue to DSP module and comapre with acquired fingerprint
    //Fingerprinting requirement
    public boolean compareEigenvalue(byte[] array) {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x44, (byte) 0x00, (byte) 0xC4, (byte) 0x00);
        byte attempt = 0;
        /*
        System.out.println("Reading from file: " + userIntID + "eigenvalue");
        Path path = Paths.get("" + userIntID + "eigenvalue");
        byte[] array = Files.readAllBytes(path);
         */
        charfile = new byte[array.length + 2];
        charfile[0] = (byte) 0xF5;
        System.arraycopy(array, 0, charfile, 1, array.length);
        charfile[array.length + 1] = (byte) 0xF5;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x44,
                    (byte) 0x00,
                    (byte) 0xC4,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                output.write(charfile);
                System.out.println("Put fingerprint...");
                Thread.sleep(1500);
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        break;
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    case 0x08:
                        errmsg = "error: ACK_TIMEOUT";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        return true;
    }

    //method 1.15 send eigenvalue to DSP module and compare 1:1
    public boolean compareEigenvalue_11(byte[] userID, byte[] array) {
        byte userID_Hi = userID[2];
        byte userID_Low = userID[3];
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x42, (byte) 0x00, (byte) 0xC4, (byte) 0x00);
        byte attempt = 0;
        /*
        System.out.println("Reading from file: " + userIntID + "eigenvalue");
        Path path = Paths.get("" + userIntID + "eigenvalue");
        byte[] array = Files.readAllBytes(path);
         */
        charfile = new byte[array.length + 2];
        charfile[0] = (byte) 0xF5;
        charfile[1] = userID_Hi;
        charfile[2] = userID_Low;
        System.arraycopy(array, 2, charfile, 3, array.length - 2);
        charfile[array.length + 1] = (byte) 0xF5;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x42,
                    (byte) 0x00,
                    (byte) 0xC4,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                output.write(charfile);
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        break;
                    case 0x01:
                        errmsg = "error: ACK_FAIL";
                        break;
                    case 0x05:
                        errmsg = "error: NO_USER";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        return true;
    }

    //method 1.16 send eigenvalue to DSP module and compare 1:N
    public boolean compareEigenvalue_1n(byte[] array) {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x43, (byte) 0x00, (byte) 0xC4, (byte) 0x00);
        byte attempt = 0;
        /*
        System.out.println("Reading from file: " + userIntID + "eigenvalue");
        Path path = Paths.get("" + userIntID + "eigenvalue");
        byte[] array = Files.readAllBytes(path);
         */
        charfile = new byte[array.length + 2];
        charfile[0] = (byte) 0xF5;
        System.arraycopy(array, 0, charfile, 1, array.length);
        charfile[array.length + 1] = (byte) 0xF5;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return false;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x43,
                    (byte) 0x00,
                    (byte) 0xC4,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                output.write(charfile);
                replycode = getReply(8);
                switch (replycode[4]) {
                    case 0x00:
                        break;
                    case 0x05:
                        errmsg = "error: NO_USER";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        System.out.println("User ID: " + replycode[2] + "" + replycode[3]);
        return true;
    }
}
