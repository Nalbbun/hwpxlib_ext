package kr.dogfoot.hwpxlib.writer.util;

import kr.dogfoot.hwpxlib.commonstrings.ZipEntryName2;
import kr.dogfoot.hwpxlib.util.BytesUtil;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileWriter {
    public static void putStoredEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        zos.putNextEntry(zipEntry(name, ZipEntry.STORED, data));
        zos.write(data);
        zos.flush();
        zos.closeEntry();
    }

    public static void putEntry(ZipOutputStream zos, String entryName, byte[] data) throws IOException {
        if (entryName.equals(ZipEntryName2.Mimetype)
                || entryName.equals(ZipEntryName2.Version)) {
            putStoredEntry(zos, entryName, data);
        } else if (data != null) {
            putZipDeflatedEntry(zos, entryName, data);
        }
    }
    private static void putZipDeflatedEntry(ZipOutputStream zos, String entryName, byte[] data) throws IOException {
        zos.putNextEntry(zipEntry(entryName, ZipEntry.DEFLATED, data));
        zos.write(data, 0, data.length);
        zos.flush();
        zos.closeEntry();
    }

    private static ZipEntry zipEntry(String entryName, int deflated, byte[] data) {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setMethod(deflated);
        entry.setCrc(BytesUtil.computeCrc(data));
        entry.setSize(data.length);
        return entry;
    }
}
