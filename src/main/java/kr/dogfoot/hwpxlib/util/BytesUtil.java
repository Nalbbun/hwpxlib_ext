package kr.dogfoot.hwpxlib.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

public class BytesUtil {
    public static byte[] randomBytes(int size) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    public static long computeCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        return crc.getValue();
    }

    public static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(DEFAULT_COMPRESSION, true);
        deflater.setInput(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        deflater.finish();

        byte[] buffer = new byte[1024];
        while(!deflater.finished()) {
            int size = deflater.deflate(buffer);
            if (size > 0) {
                baos.write(buffer, 0, size);
            }
        }
        byte[] b = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return b;
    }

    public static byte[] encrypt(byte[] data, byte[] encryptKey, byte[] initialisationVector) {
        byte[] data16;
        if (data.length % 16 != 0) {
            data16 = new byte[data.length / 16 * 16 + 16];
            System.arraycopy(data, 0, data16, 0, data.length);
        } else {
            data16 = data;
        }
        byte[] encrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES_256/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(encryptKey, "AES"),
                    new IvParameterSpec(initialisationVector));
            encrypted = cipher.doFinal(data16);
        } catch (Exception e) {
            System.out.println("\tCipher Error : " + e.getMessage());
            encrypted = null;
        }
        return encrypted;
    }

    public static byte[] uncompress(byte[] data, long originalSize) {
        byte[] uncompressed = new byte[(int) originalSize];
        try {
            Inflater inflater = new Inflater(true);
            inflater.setInput(data);
            inflater.inflate(uncompressed);
            inflater.end();
        } catch (Exception e) {
            System.out.println("\tInflater Error : " + e.getMessage());

            uncompressed = null;
        }
        return uncompressed;
    }

    public static byte[] decrypt(byte[] data, byte[] key, byte[] initialisationVector) {
        byte[] decrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES_256/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new IvParameterSpec(initialisationVector));
            decrypted = cipher.doFinal(data);
        } catch (Exception e) {
            System.out.println("\tCipher Error : " + e.getMessage());

            decrypted = null;
        }
        return decrypted;
    }

    public static String checkSum(byte[]data) throws Exception {
        byte[] fileBytes_1k = (data.length > 1024) ? subBytes(data, 0, 1023) : data;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(fileBytes_1k);

        return DatatypeConverter.printBase64Binary(messageDigest.digest());
    }

    public static byte[] subBytes(byte[] source, int start, int end) throws Exception {
        if (start > end ||
                start < 0 || start >= source.length
                || end >= source.length) {
            throw new Exception("범위 내에 있지 않습니다.");
        }
        int len = end - start + 1;
        byte[] target = new byte[len];
        System.arraycopy(source, start, target, 0, len);
        return target;
    }
}
