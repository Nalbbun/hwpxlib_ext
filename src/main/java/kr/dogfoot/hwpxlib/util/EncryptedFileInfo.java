package kr.dogfoot.hwpxlib.util;

import kr.dogfoot.hwpxlib.commonstrings.ImageExt;
import kr.dogfoot.hwpxlib.commonstrings.ZipEntryName2;

import javax.xml.bind.DatatypeConverter;

public class EncryptedFileInfo {
    private static final String MediaType_AppXML = "application/xml";
    private static final String MediaType_TextXML = "text/xml";
    private static final String MediaType_TextPlain = "text/plain";
    private static final String MediaType_ImagePng = "image/png";
    private static final String MediaType_ImageJpeg = "image/jpeg";
    private static final String MediaType_ImageGif = "image/gif";
    private static final String MediaType_ImageBmp = "image/bmp";
    private static final String MediaType_ImageTiff = "image/tiff";

    public static EncryptedFileInfo encryptEntry(String name, byte[] data, EncryptionParameter parameter) throws Exception {
        byte[] compressed = BytesUtil.compress(data);
        if (compressed == null) {
            return null;
        }
        byte[] encrypted = BytesUtil.encrypt(compressed, parameter.encryptKey(), parameter.salt_initialisationVector());

        return new EncryptedFileInfo()
                .entryName(name)
                .nameToMediaType(name)
                .salt_initialisationVector(DatatypeConverter.printBase64Binary(parameter.salt_initialisationVector()))
                .originalSize(data.length)
                .checkSum(BytesUtil.checkSum(data))
                .encrypted(encrypted);
    }

    private String entryName;
    private String mediaType;
    private String salt_initialisationVector;
    private int originalSize;
    private String checkSum;
    private byte[] encrypted;

    private EncryptedFileInfo() {
    }

    public String entryName() {
        return entryName;
    }

    public EncryptedFileInfo entryName(String entryName) {
        this.entryName = entryName;
        return this;
    }

    public String mediaType() {
        return mediaType;
    }

    private EncryptedFileInfo nameToMediaType(String name) {
        if (name.equals(ZipEntryName2.Header) ||
                name.startsWith(ZipEntryName2.Section)) {
            mediaType = MediaType_AppXML;
        } else if (name.equals(ZipEntryName2.PrvText)) {
            mediaType = MediaType_TextPlain;
        } else if (name.equals(ZipEntryName2.Settings)) {
            mediaType = MediaType_TextXML;
        } else if (name.endsWith(ImageExt.Png)) {
            mediaType = MediaType_ImagePng;
        } else if (name.endsWith(ImageExt.Jpg) || name.endsWith(ImageExt.Jpeg)) {
            mediaType = MediaType_ImageJpeg;
        } else if (name.endsWith(ImageExt.Gif)) {
            mediaType = MediaType_ImageGif;
        } else if (name.endsWith(ImageExt.Bmp)) {
            mediaType = MediaType_ImageBmp;
        } else if (name.endsWith(ImageExt.Tif)) {
            mediaType = MediaType_ImageTiff;
        } else {
            mediaType = MediaType_TextXML;
        }

        return this;
    }

    public String salt_initialisationVector() {
        return salt_initialisationVector;
    }

    public EncryptedFileInfo salt_initialisationVector(String salt_initialisationVector) {
        this.salt_initialisationVector = salt_initialisationVector;
        return this;
    }

    public int originalSize() {
        return originalSize;
    }

    public EncryptedFileInfo originalSize(int originalSize) {
        this.originalSize = originalSize;
        return this;
    }

    public String checkSum() {
        return checkSum;
    }

    public EncryptedFileInfo checkSum(String checkSum) {
        this.checkSum = checkSum;
        return this;
    }

    public byte[] encrypted() {
        return encrypted;
    }

    public EncryptedFileInfo encrypted(byte[] encrypted) {
        this.encrypted = encrypted;
        return this;
    }
}







