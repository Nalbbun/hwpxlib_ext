package kr.dogfoot.hwpxlib.util;

import de.rtner.security.auth.spi.PBKDF2Engine;
import de.rtner.security.auth.spi.PBKDF2Parameters;

public class PBKDF2Utils {
    private static class PBKDF2EngineWithBinaryPassword extends PBKDF2Engine {
        private PBKDF2EngineWithBinaryPassword(PBKDF2Parameters parameters) {
            super(parameters);
        }

        public byte[] deriveKey(byte[] inputPassword, int dkLen) {
            this.assertPRF(inputPassword);
            return this.PBKDF2(prf, parameters.getSalt(), parameters.getIterationCount(), dkLen);
        }
    }

    public static byte[] deriveKey(
            byte[] password,
            byte[] salt,
            int iterationCount,
            int dkLen) {

        PBKDF2Parameters parameters = new PBKDF2Parameters("HmacSHA1", null, salt, iterationCount);

        PBKDF2EngineWithBinaryPassword engine = new PBKDF2EngineWithBinaryPassword(parameters);

        return engine.deriveKey(password, dkLen);
    }
}
