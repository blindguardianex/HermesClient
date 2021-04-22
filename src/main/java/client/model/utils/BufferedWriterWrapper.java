package client.model.utils;

import client.model.ClientApp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * Этот класс упрощает отправку запросов на сервер.
 * @See BufferedWriter
 */
@Slf4j
public class BufferedWriterWrapper extends BufferedWriter {

    private static String key = "Bar12345Bar12345"; // 128 bit key
    private static String initVector = "RandomInitVector"; // 16 bytes IV

    private static IvParameterSpec iv;
    private static SecretKeySpec skeySpec;

    public BufferedWriterWrapper(Writer out){
        super(out);
        iv =new IvParameterSpec(initVector.getBytes(ClientApp.getDefaultCharset()));
        skeySpec = new SecretKeySpec(key.getBytes(ClientApp.getDefaultCharset()), "AES");
    }

    @Override
    public void write(String message) throws IOException {
        super.write(encrypt(message)+"\n");
        super.flush();
    }

    /**
     * Шифрует строку
     * @param value
     * @return
     */
    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[]message=value.getBytes(ClientApp.getDefaultCharset());
            byte[] encrypted = cipher.doFinal(message);
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Расшифровывает строку
     * @param encrypted
     * @return
     */
    public static String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[]message=encrypted.getBytes(ClientApp.getDefaultCharset());
            byte[] original = cipher.doFinal(Base64.decodeBase64(message));
            String decodeData = new String(original, ClientApp.getDefaultCharset());
            String[]s=decodeData.split("\n");
            return s[0];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }
}
