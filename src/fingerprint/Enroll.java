package fingerprint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class Enroll extends Module {

    public Enroll() {
    }

    //method 1.3 add user fingerprint in DSP module database  
    //Fingerprinting requirement
    public boolean addFingerprint(byte[] userID, byte privilege) {
        byte userID_Hi = userID[2];
        byte userID_Low = userID[3];
        byte[] replycode = null;
        System.out.println("Put your fingerprint 3 times...");
        for (int i = 1; i < 4; i++) {
            byte iterator = 0x01;
            if (i == 2) {
                iterator = 0x02;
            } else if (i == 3) {
                iterator = 0x03;
            }
            short cksum = getCksum(iterator, userID_Hi, userID_Low, privilege);
            byte attempt = 0;
            do {
                try {
                    attempt++;
                    if (attempt > ATTEMPT) {
                        return false;
                    }
                    output.write(new byte[]{(byte) 0xF5,
                        iterator,
                        userID_Hi,
                        userID_Low,
                        privilege,
                        (byte) 0x00,
                        (byte) cksum,
                        (byte) 0xF5});
                    Thread.sleep(1500);
                    replycode = getReply(8);
                    switch (replycode[4]) {
                        case 0x00:
                            break;
                        case 0x01:
                            errmsg = "error: ACK_FAIL";
                            break;
                        case 0x04:
                            errmsg = "error: ACK_FULL"; // First iteration
                            break;
                        case 0x06:
                            errmsg = "error: ACK_USER_EXIST"; // Third iteration
                            break;
                        case 0x07:
                            errmsg = "error: ACK_FINGERPRINT_EXIST";
                            break;
                        case 0x08:
                            errmsg = "error: ACK_TIMEOUT";
                            break;
                        default:
                            errmsg = "error: Unknown error";
                            break;
                    }
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Enroll.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (replycode[4] != 0x0);
        }
        System.out.println("OK! Acquired fingerprint");
        return true;
    }
}
