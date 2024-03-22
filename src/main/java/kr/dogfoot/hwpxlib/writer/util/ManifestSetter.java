package kr.dogfoot.hwpxlib.writer.util;

import kr.dogfoot.hwpxlib.object.metainf.EncryptionData;
import kr.dogfoot.hwpxlib.object.metainf.FileEntry;
import kr.dogfoot.hwpxlib.object.metainf.ManifestXMLFile;
import kr.dogfoot.hwpxlib.util.EncryptedFileInfo;

import java.util.ArrayList;

public class ManifestSetter {
    private static final String ChecksumType = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0#sha256-1k";
    private static final String AlgorithmName = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
    private static final String KeyDerivationName = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0#pbkdf2";
    private static final String StartKeyGenerationName = "http://www.w3.org/2000/09/xmldsig#sha256";

    public static void reset(ManifestXMLFile manifestXMLFile, ArrayList<EncryptedFileInfo> encryptedFileInfos) {
        manifestXMLFile.removeAllFileEntries();

        for (EncryptedFileInfo encryptedFileInfo : encryptedFileInfos) {
            fileEntry(manifestXMLFile.addNewFileEntry(), encryptedFileInfo);
        }
    }

    private static void fileEntry(FileEntry fileEntry, EncryptedFileInfo encryptedFileInfo) {
        fileEntry
                .fullPathAnd(encryptedFileInfo.entryName())
                .mediaTypeAnd(encryptedFileInfo.mediaType())
                .size((long) encryptedFileInfo.originalSize());

        fileEntry.createEncryptionData();
        EncryptionData encryptionData = fileEntry.encryptionData();

        encryptionData
                .checksumTypeAnd(ChecksumType)
                .checksum(encryptedFileInfo.checkSum());

        encryptionData.createAlgorithm();
        encryptionData.algorithm()
                .algorithmNameAnd(AlgorithmName)
                .initialisationVector(encryptedFileInfo.salt_initialisationVector());

        encryptionData.createKeyDerivation();
        encryptionData.keyDerivation()
                .keyDerivationNameAnd(KeyDerivationName)
                .keySizeAnd(32)
                .iterationCountAnd(1024)
                .salt(encryptedFileInfo.salt_initialisationVector());

        encryptionData.createStartKeyGeneration();
        encryptionData.startKeyGeneration()
                .startKeyGenerationNameAnd(StartKeyGenerationName)
                .keySizeAnd(32);
    }
}
