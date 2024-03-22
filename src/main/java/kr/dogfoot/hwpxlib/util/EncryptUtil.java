package kr.dogfoot.hwpxlib.util;

import kr.dogfoot.hwpxlib.object.metainf.EncryptionAlgorithm;
import kr.dogfoot.hwpxlib.object.metainf.EncryptionKeyDerivation;
import kr.dogfoot.hwpxlib.object.metainf.FileEntry;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {
    public static byte[] startKey(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decrypt(byte[] encrypted, byte[] startKey, FileEntry fileEntry) {
        EncryptionKeyDerivation ekd = fileEntry.encryptionData().keyDerivation();
        byte[] decryptKey = PBKDF2Utils.deriveKey(startKey,
                DatatypeConverter.parseBase64Binary(ekd.salt()),
                ekd.iterationCount(),
                ekd.keySize());

        EncryptionAlgorithm ea = fileEntry.encryptionData().algorithm();
        byte[] decrypted = BytesUtil.decrypt(encrypted,
                decryptKey,
                DatatypeConverter.parseBase64Binary(ea.initialisationVector()));

        return BytesUtil.uncompress(decrypted, fileEntry.size());
    }

}
