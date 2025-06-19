package com.ovah.arqithealth.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;


@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public byte[] encrypt(byte[] file, String sharedSecretReference) {
        try {
            byte[] key = sharedSecretReference.getBytes(StandardCharsets.UTF_8);
            byte[] aesKey = Arrays.copyOf(key, 32); // ← Simply take first 32 bytes
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(file);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public byte[] decrypt(byte[] encryptedData, String sharedSecretReference) {
        try {
            byte[] key = sharedSecretReference.getBytes(StandardCharsets.UTF_8);
            byte[] aesKey = Arrays.copyOf(key, 32); // ← Simply take first 32 bytes
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return cipher.doFinal(encryptedData);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
}