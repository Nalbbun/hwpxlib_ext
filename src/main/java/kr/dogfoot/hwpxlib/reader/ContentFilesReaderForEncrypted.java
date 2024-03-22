package kr.dogfoot.hwpxlib.reader;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.metainf.FileEntry;
import kr.dogfoot.hwpxlib.reader.common.ElementReaderManager;
import kr.dogfoot.hwpxlib.reader.util.ZipFileReader;
import kr.dogfoot.hwpxlib.util.EncryptUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ContentFilesReaderForEncrypted extends ContentFilesReader {
    private final byte[] startKey;

    public ContentFilesReaderForEncrypted(ElementReaderManager entryReaderManager, byte[] startKey) {
        super(entryReaderManager);
        this.startKey = startKey;
    }

    public void read(HWPXFile hwpxFile, String href, ZipFile zipFile, FileEntry fileEntry) throws ParserConfigurationException, IOException, SAXException {
        if (fileEntry == null) {
            read(hwpxFile, href, zipFile);
        } else {
            byte[] encrypted = ZipFileReader.readBinary(href, zipFile);
            byte[] decrypted = EncryptUtil.decrypt(encrypted, startKey, fileEntry);
            read(hwpxFile, new ByteArrayInputStream(decrypted));
        }
    }
}
