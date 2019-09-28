package com.bophia.erik.ykxrider.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * 加解密工具类
 */
public class EncryptionUtil {
	private EncryptionUtil(){}
	private static final String ALGORITHM = "DES";
	public static final String APPKEY="jpPX8LMS";
	/**
	 * Get Des Key
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] getKey() throws NoSuchAlgorithmException {
		KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
		SecretKey deskey = keygen.generateKey();
		return deskey.getEncoded();
	}

	/**
	 * Encrypt Messages
	 * @throws InvalidKeyException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public static byte[] encode(byte[] input, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, ALGORITHM);
		Cipher c1 = Cipher.getInstance(ALGORITHM);
		c1.init(Cipher.ENCRYPT_MODE, deskey);
		return c1.doFinal(input);
	}

	/**
	 * Decrypt Messages
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public static byte[] decode(byte[] input, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, ALGORITHM);
		Cipher c1 = Cipher.getInstance(ALGORITHM);
		c1.init(Cipher.DECRYPT_MODE, deskey);
		return c1.doFinal(input);
	}

	/**
	 * MD5
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] md5(byte[] input) throws NoSuchAlgorithmException {
		java.security.MessageDigest alg = java.security.MessageDigest
				.getInstance("MD5"); // or "SHA-1"
		alg.update(input);
		return alg.digest();
	}

	/**
	 * Convert byte[] to String
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		//String stmp = "";
		for (int n = 0; n < b.length; n++) {
			String stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
	
	/**
	 * Convert String to byte[]
	 */
	public static byte[] hex2byte(String hex) throws IllegalArgumentException {
		if (hex.length() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
			String swap = Character.toString(arr[i++]) + arr[i];
			Integer byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = byteint.byteValue();
		}
		return b;
	}

	//测试主函数
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
//		String userEn=byte2hex(encode("bophia".getBytes(),KEY.getBytes()));
//		System.out.println(userEn);
//		String passEn=byte2hex(encode("DB_stockdata_@!2018".getBytes(),KEY.getBytes()));
//		System.out.println(passEn);
//		
//		System.out.println(new String(EncryptionUtil.decode(EncryptionUtil.hex2byte(userEn), KEY.getBytes())));
//		System.out.println(new String(EncryptionUtil.decode(EncryptionUtil.hex2byte(passEn), KEY.getBytes())));
		
		String userEn=byte2hex(encode("bophia_stock".getBytes(),APPKEY.getBytes()));
		System.out.println(userEn);
		String passEn=byte2hex(encode("DB_stockdata_@!2018".getBytes(),APPKEY.getBytes()));
		System.out.println(passEn);
		
		System.out.println(new String(EncryptionUtil.decode(EncryptionUtil.hex2byte(userEn), APPKEY.getBytes())));
		System.out.println(new String(EncryptionUtil.decode(EncryptionUtil.hex2byte(passEn), APPKEY.getBytes())));

	}
}
