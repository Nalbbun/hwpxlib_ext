package kr.dogfoot.hwpxlib.util;

import java.security.NoSuchAlgorithmException;

public class EncryptionParameter {
    private static final int PBKDF2_Iteration_Count = 1024;
    private static final int PBKDF2_Key_Length = 32;

    public static EncryptionParameter fromPassword(String password) throws NoSuchAlgorithmException {
        byte[] startKey = EncryptUtil.startKey(password);

        final byte[] salt_initialisationVector = BytesUtil.randomBytes(16);

        byte[] encryptKey = PBKDF2Utils.deriveKey(startKey,
                salt_initialisationVector,
                PBKDF2_Iteration_Count,
                PBKDF2_Key_Length);

        return new EncryptionParameter(encryptKey, salt_initialisationVector);
    }

    private final byte[] encryptKey;
    private final byte[] salt_initialisationVector;

    private EncryptionParameter(byte[] encryptKey, byte[] salt_initialisationVector) {
        this.encryptKey = encryptKey;
        this.salt_initialisationVector = salt_initialisationVector;
    }

    public byte[] encryptKey() {
        return encryptKey;
    }

    public byte[] salt_initialisationVector() {
        return salt_initialisationVector;
    }
}
