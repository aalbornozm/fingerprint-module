package fingerprint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class Backup extends Module {

    public Backup() {
    }

    //method 1.17 upload eigenvalue from specific user
    public byte[] getEigenvalueFromDSP(byte[] userID) {
        byte userID_Hi = userID[2];
        byte userID_Low = userID[3];
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x31, userID_Hi, userID_Low, (byte) 0x00);
        byte attempt = 0;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return new byte[]{};
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x31,
                    userID_Hi,
                    userID_Low,
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
                    case 0x05:
                        errmsg = "error: ACK_NOUSER";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, null, ex);
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
