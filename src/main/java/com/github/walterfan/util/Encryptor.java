package com.github.walterfan.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;


public class Encryptor {
     
    private static byte[] IV_BYTES = "1234567890123456".getBytes();

    private static String AES_CBC_NOPADDING = "AES/CBC/NoPadding";

    private static String AES_ALGORITHM = "AES";

    public static final String ENC_CBC_NOPADDING = "AES/CBC/NoPadding";
    
    private static final String ENC_ALGORITHM = "AES";

    private String algorithm = ENC_ALGORITHM;
    
    private static final int ENC_KEY_LEN = 16;

    public static final String ENC_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";


    private AlgorithmParameterSpec ivParamSpec = null;

    private SecretKeySpec keySpec = null;
    

    public Encryptor() {
        
    }

    public Encryptor(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] encode(byte[] bytes, byte[] kbytes, byte[] iv_bytes) throws Exception {
        if(kbytes.length % ENC_KEY_LEN != 0) {
            throw new Exception("invalid AES Key length(128, 192, or 256 bits)");
        }
        if(iv_bytes.length > 0 && iv_bytes.length != 16) {
            throw new Exception("invalid IV length(16 bytes)");
        }

        SecretKeySpec keySpec = new SecretKeySpec(kbytes, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(algorithm);

        AlgorithmParameterSpec paraSpec = new IvParameterSpec(iv_bytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, paraSpec);

        return cipher.doFinal(bytes);
    }

    public byte[] decode(byte[] encryptedBytes, byte[] kbytes, byte[] iv_bytes) throws Exception {
        if(kbytes.length % ENC_KEY_LEN != 0) {
            throw new Exception("invalid AES Key length(128, 192, or 256 bits)");
        }
        if(iv_bytes.length > 0 && iv_bytes.length != 16) {
            throw new Exception("invalid IV length(16 bytes)");
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(kbytes, AES_ALGORITHM);

        AlgorithmParameterSpec paraSpec = new IvParameterSpec(iv_bytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, paraSpec);

        return cipher.doFinal(encryptedBytes);
    }

   public  byte[] makeKey() throws NoSuchAlgorithmException {
    	KeyGenerator keygen = KeyGenerator.getInstance(algorithm);
    	SecretKey skey = keygen.generateKey();
    	byte[] raw = skey.getEncoded();

    	return raw;
    }

    //------------------------- new implementation ---------------//


    public static byte[] makeKeyBySHA1(String key, int len) {
        byte[] seed = EncodeUtils.sha1(key);
        //assert (seed.length == SHA1_LEN);
        byte[] raw = new byte[len];
        System.arraycopy(seed, 0, raw, 0, len);
        return raw;
    }


    public static byte[] makeKeyBySHA2(String key, int len) {
        byte[] seed = EncodeUtils.sha2(key);
        byte[] raw = new byte[len];
        System.arraycopy(seed, 0, raw, 0, len);
        return raw;
    }


    public Encryptor(String algorithm, byte[] keyBytes, byte[] ivBytes) {
        this.algorithm = algorithm;
        this.keySpec =  new SecretKeySpec(keyBytes, AES_ALGORITHM);
        this.ivParamSpec = new IvParameterSpec(ivBytes);

    }

    public Encryptor(String algorithm, String strKey, String strIv) {
        byte[] keyBytes = makeKeyBySHA2(strKey, 16);
        byte[] ivBytes = makeKeyBySHA2(strIv, 16);
        this.algorithm = algorithm;
        this.keySpec =  new SecretKeySpec(keyBytes, ENC_ALGORITHM);
        this.ivParamSpec = new IvParameterSpec(ivBytes);

    }

    public Encryptor(String strKey, String strIv) {
        byte[] keyBytes = makeKeyBySHA2(strKey, 16);
        byte[] ivBytes = makeKeyBySHA2(strIv, 16);
        this.algorithm = ENC_CBC_PKCS5PADDING;
        this.keySpec =  new SecretKeySpec(keyBytes, ENC_ALGORITHM);
        this.ivParamSpec = new IvParameterSpec(ivBytes);

    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public SecretKeySpec getKeySpec() {
        return keySpec;
    }

    public void setKeySpec(SecretKeySpec keySpec) {
        this.keySpec = keySpec;
    }

    public AlgorithmParameterSpec getIvParamSpec() {
        return ivParamSpec;
    }

    public void setIvParamSpec(AlgorithmParameterSpec ivParamSpec) {
        this.ivParamSpec = ivParamSpec;
    }

    public byte[] encrypt(byte[] inputBytes) throws Exception {

        Cipher cipher = Cipher.getInstance(algorithm);

        if(null == ivParamSpec)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        else
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

        //byte[] cipherText = new byte[cipher.getOutputSize(inputBytes.length)];
        //int ctLength = cipher.update(inputBytes, 0, inputBytes.length, cipherText, 0);
        return cipher.doFinal(inputBytes);

    }

    /*
    *     Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM));
            return cipher.doFinal(bytes);
    * */
    public byte[] decrypt(byte[] cipherText)  throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);

        if(null == ivParamSpec)
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        else
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
        //int ctLength = cipherText.length;
        //byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
        //int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
        return cipher.doFinal(cipherText);
        //return plainText;
    }

    public SecretKeySpec generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(algorithm);
        SecretKey skey = keygen.generateKey();
        byte[] raw = skey.getEncoded();
        return new SecretKeySpec(raw, AES_ALGORITHM);
    }


    //Key length is 16(128 bits) or 32(256 bits), if it's 32 bytes(256 bits),
//need to download the unlimited strength JCE policy files
//default ECB mode
    public static byte[] encodeAES(byte[] bytes, byte[] keyBytes) throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public static byte[] decodeAES(byte[] encryptedBytes, byte[] keyBytes) throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM));
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public static String encryptPwd(String pwd, String key64, String key2) throws Exception {
        byte[] keyBytes = makeKeyBySHA2(key64 + key2, 16);
        return new String(EncodeUtils.encodeBase64(encodeAES(pwd.getBytes(), keyBytes)));
    }

    public static String decryptPwd(String pwd, String key64, String key2) throws Exception {
        byte[] encryptResult = EncodeUtils.decodeBase64(pwd.getBytes());
        byte[] keyBytes = makeKeyBySHA2(key64 + key2, 16);
        return new String(decodeAES(encryptResult, keyBytes));
    }


    public static void main(String[] args) throws Exception {

        if(args.length > 2) {
            String encryptedPwd = args[0];
            String key1 = args[1];
            String key2 = args[2];
            String originPwd = decryptPwd(encryptedPwd, key1, key2);
            System.out.println(originPwd);
        }

    }

}
