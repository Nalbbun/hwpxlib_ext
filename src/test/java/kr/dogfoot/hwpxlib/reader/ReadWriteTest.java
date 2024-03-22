package kr.dogfoot.hwpxlib.reader;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriterForEncrypted;
import org.junit.Test;

public class ReadWriteTest {
    @Test
    public void read() throws Exception {
        HWPXFile hwpxFile =  HWPXReaderForEncrypted.fromFilepath("testFile/encrypt.hwpx", "abcde");
    }

    @Test
    public void writer() throws Exception {
        HWPXFile hwpxFile =  HWPXReaderForEncrypted.fromFilepath("testFile/encrypt.hwpx", "abcde");
        HWPXWriterForEncrypted.toFilepath(hwpxFile, "testFile/encrypt_re.hwpx", "hwpx1234");
    }
}

