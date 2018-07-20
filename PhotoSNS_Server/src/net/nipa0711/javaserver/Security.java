package net.nipa0711.javaserver;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

public class Security {
	public String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[32];
		random.nextBytes(bytes);
		Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		String salt = encoder.encodeToString(bytes);
		return salt;
	}

	public String getHash(String password, String salt) {
		MessageDigest digest;
		String result = "";
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			byte[] bytes = salt.getBytes();
			digest.update(bytes);
			byte byteData[] = digest.digest(password.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			result = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return result;
	}
}
