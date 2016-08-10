package fingerprint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voladorx
 */
public class Restore extends Module {

    public Restore() {
        //this.connect(DEVICENAME);
    }

    //method 1.18 download eigenvalue to DSP module and save to specified user ID 
    public int downEigenvalueToDSP(byte[] array) {
        byte[] replycode = null;
        short cksum = getCksum((byte) 0x41, (byte) 0x00, (byte) 0xC4, (byte) 0x00);
        byte attempt = 0;
        charfile = new byte[array.length + 2];
        charfile[0] = (byte) 0xF5;
        System.arraycopy(array, 0, charfile, 1, array.length);
        charfile[array.length + 1] = (byte) 0xF5;
        do {
            try {
                attempt++;
                if (attempt > ATTEMPT) {
                    return -1;
                }
                output.write(new byte[]{(byte) 0xF5,
                    (byte) 0x41,
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
                    case 0x06:
                        errmsg = "error: ACK_USER_EXIST";
                        break;
                    default:
                        errmsg = "error: Unknown error";
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Restore.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (replycode[4] != 0x00);
        byte[] userByteID = new byte[]{0x00, 0x00, replycode[2], replycode[3]};
        return fromByteArray(userByteID);
    }
}
