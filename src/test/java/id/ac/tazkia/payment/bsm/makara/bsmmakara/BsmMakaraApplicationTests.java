package id.ac.tazkia.payment.bsm.makara.bsmmakara;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.utils.ChecksumUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BsmMakaraApplicationTests {

	@Value("${shared.key}") private String sharedKey;

	@Test
	public void testCalculateChecksum() throws Exception {
		String tanggalTransaksi = "20180101085959";
		String nomorPembayaran = "765432002";
		String verifikasiChecksum = ChecksumUtils.calculateChecksum(nomorPembayaran, sharedKey, tanggalTransaksi);
		Assert.assertNotNull(verifikasiChecksum);
		System.out.println("Checksum : "+verifikasiChecksum);
	}

}
