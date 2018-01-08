package id.ac.tazkia.payment.bsm.makara.bsmmakara.utils;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public abstract class ChecksumUtils {
    public static String calculateChecksum(String nomorPembayaran, String sharedKey, String tanggalTransaksi) throws Exception {
        String input = nomorPembayaran + sharedKey + tanggalTransaksi;
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.reset();
        return DatatypeConverter.printHexBinary(sha1.digest(input.getBytes()));
    }
}
