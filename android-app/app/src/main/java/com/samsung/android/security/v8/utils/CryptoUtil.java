package com.samsung.android.security.v8.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "AlKhanjarPro2026SecretKey12345"; // 32 bytes
    
    /**
     * تشفير النص باستخدام AES-256
     */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8), 
                    ALGORITHM
            );
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // توليد IV عشوائي
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // دمج IV مع البيانات المشفرة
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.encodeToString(combined, Base64.NO_WRAP);
            
        } catch (Exception e) {
            return plainText; // Fallback
        }
    }
    
    /**
     * فك تشفير النص
     */
    public static String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.decode(encryptedText, Base64.NO_WRAP);
            
            // استخراج IV والبيانات المشفرة
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
            
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8), 
                    ALGORITHM
            );
            
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            return encryptedText; // Fallback
        }
    }
    
    /**
     * توليد Hash SHA-256
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            return input;
        }
    }
    
    /**
     * توليد مفتاح عشوائي
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            
            return Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
            
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * تشفير Base64 بسيط
     */
    public static String encodeBase64(String text) {
        try {
            return Base64.encodeToString(
                    text.getBytes(StandardCharsets.UTF_8), 
                    Base64.NO_WRAP
            );
        } catch (Exception e) {
            return text;
        }
    }
    
    /**
     * فك تشفير Base64
     */
    public static String decodeBase64(String encoded) {
        try {
            byte[] data = Base64.decode(encoded, Base64.NO_WRAP);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded;
        }
    }
}
