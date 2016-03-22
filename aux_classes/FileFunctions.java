package aux_classes;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

public class FileFunctions {
	
	public BitSet generateBitString(File f) {
		long file_modif = f.lastModified();
		
		BitSet bit_s = BitSet.valueOf(new long[] {file_modif});
		return bit_s;
	}
	
	public BitSet generateFileId(File f) throws NoSuchAlgorithmException {
		byte[] byte_s;
		
		BitSet bit_s = this.generateBitString(f);
		byte_s = bit_s.toByteArray();
		
		MessageDigest mDigest = MessageDigest.getInstance("SHA256");
		byte[] byte_f = mDigest.digest(byte_s);
		
		BitSet bit_f = BitSet.valueOf(byte_f);
		
		return bit_f;
		
	}

}
