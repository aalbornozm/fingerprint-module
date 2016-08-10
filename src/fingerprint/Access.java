package fingerprint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class Access extends Module {

    public Access() {

        //this.connect(DEVICENAME);
    }

    //method 1.8 match a fingerprint with DSP module database by comparison 1:N
    //Fingerprinting requirement
    public int compareFingerprint_1n() {
        boolean term = true;
        int huellaId = 0;
        byte[] replycode;
        short cksum = getCksum((byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x0C,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) cksum,
                    (byte) 0xF5});
                System.out.println("Put fingerprint...");
                Thread.sleep(1500);
                replycode = getReply(8);
                if ((replycode[4] == 0x01) || (replycode[4] == 0x02) || (replycode[4] == 0x03)) {
                    //System.out.println("User Privilege: " + replycode[4]);
                    byte[] userByteID = new byte[]{0x00, 0x00, replycode[2], replycode[3]};
                    huellaId = fromByteArray(userByteID);
                    term = false;
                } else if (replycode[4] == 0x05) {
                    errmsg = "error: ACK_NOUSER";
                } else {
                    //replycode[4] == 0x08
                    errmsg = "error: ACK_TIMEOUT";
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Access.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (term);
        return huellaId;
    }
}
