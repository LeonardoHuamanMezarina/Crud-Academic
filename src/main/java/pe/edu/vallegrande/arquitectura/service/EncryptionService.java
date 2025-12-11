package pe.edu.vallegrande.arquitectura.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // 16 caracteres para AES-128
    
    /**
     * Encripta un texto usando AES
     */
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                return plainText;
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedData = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
            
        } catch (Exception e) {
            log.error("Error al encriptar: {}", e.getMessage());
            throw new RuntimeException("Error en encriptación", e);
        }
    }
    
    /**
     * Desencripta un texto usando AES
     */
    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return encryptedText;
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedData);
            
        } catch (Exception e) {
            log.error("Error al desencriptar: {}", e.getMessage());
            throw new RuntimeException("Error en desencriptación", e);
        }
    }
    
    /**
     * Verifica si un texto coincide con un texto encriptado
     */
    public boolean matches(String plainText, String encryptedText) {
        try {
            String decryptedText = decrypt(encryptedText);
            return plainText.equals(decryptedText);
        } catch (Exception e) {
            return false;
        }
    }
}