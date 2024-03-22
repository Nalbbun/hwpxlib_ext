package kr.dogfoot.hwpxlib.writer;

import kr.dogfoot.hwpxlib.commonstrings.*;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.chart.ChartXMLFile;
import kr.dogfoot.hwpxlib.object.common.HWPXObject;
import kr.dogfoot.hwpxlib.object.content.context_hpf.ManifestItem;
import kr.dogfoot.hwpxlib.object.etc.UnparsedXMLFile;
import kr.dogfoot.hwpxlib.object.metainf.RootFile;
import kr.dogfoot.hwpxlib.util.EncryptedFileInfo;
import kr.dogfoot.hwpxlib.util.EncryptionParameter;
import kr.dogfoot.hwpxlib.writer.common.ElementWriterManager;
import kr.dogfoot.hwpxlib.writer.common.ElementWriterSort;
import kr.dogfoot.hwpxlib.writer.util.ManifestSetter;
import kr.dogfoot.hwpxlib.writer.util.XMLStringBuilder;
import kr.dogfoot.hwpxlib.writer.util.ZipFileWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

public class HWPXWriterForEncrypted {
    public static void toFilepath(HWPXFile hwpxFile, String filepath, String password) throws Exception {
        FileOutputStream fos = new FileOutputStream(filepath);
        toStream(hwpxFile, fos, password);
    }

    public static void toStream(HWPXFile hwpxFile, OutputStream os, String password) throws Exception {
        HWPXWriterForEncrypted writer = new HWPXWriterForEncrypted(hwpxFile);
        writer.createZIPFile(os);
        writer.encryptionParameter(password);
        writer.write();
        writer.writeManifest();
        writer.close();
        os.close();
    }

    public static byte[] toBytes(HWPXFile hwpxFile, String password) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toStream(hwpxFile, baos, password);
        return baos.toByteArray();
    }

    private final HWPXFile hwpxFile;
    private final ElementWriterManager elementWriterManager;
    private ZipOutputStream zos;
    private EncryptionParameter encryptionParameter;
    private final ArrayList<EncryptedFileInfo> encryptedFileInfos;

    private HWPXWriterForEncrypted(HWPXFile hwpxFile) {
        this.hwpxFile = hwpxFile;
        elementWriterManager = new ElementWriterManager();
        encryptedFileInfos = new ArrayList<>();
    }

    public void createZIPFile(OutputStream outputStream) {
        zos = new ZipOutputStream(outputStream);
    }

    private void encryptionParameter(String password) throws NoSuchAlgorithmException {
        encryptionParameter = EncryptionParameter.fromPassword(password);
    }

    private void write() throws Exception {
        mineType();
        version_xml();
        META_INF_container_xml();
        content_hpf();
        contentFiles();
        chartFiles();
        etcContainedFile();
        unparsedXMLFiles();
    }

    private void mineType() throws Exception {
        putIntoZip(ZipEntryName.MineType, MineTypes.HWPX);
    }

    private void version_xml() throws Exception {
        writeChild(ElementWriterSort.Version, hwpxFile.versionXMLFile());
        putIntoZip(ZipEntryName.Version, xsb().toString());
    }

    private void writeChild(ElementWriterSort sort, HWPXObject child) {
        elementWriterManager.get(sort).write(child);
    }

    private XMLStringBuilder xsb() {
        return elementWriterManager.xsb();
    }

    private void putIntoZip(String entryName, String data) throws Exception {
        if (data == null) {
            return;
        }

        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        putIntoZip(entryName, bytes);
    }

    private void putIntoZip(String entryName, byte[] data) throws Exception {
        if (data == null) {
            return;
        }

        if (encryptingEntry(entryName)) {
            EncryptedFileInfo efi = EncryptedFileInfo.encryptEntry(entryName, data, encryptionParameter);
            if (efi != null && efi.encrypted() != null) {
                ZipFileWriter.putStoredEntry(zos, entryName, efi.encrypted());
                encryptedFileInfos.add(efi);
            }

        } else {
            ZipFileWriter.putEntry(zos, entryName, data);
        }
    }

    private boolean encryptingEntry(String entryName) {
        return entryName.equals(ZipEntryName2.Header)
                || entryName.startsWith(ZipEntryName2.Section)
                || entryName.equals(ZipEntryName2.PrvText)
                || entryName.equals(ZipEntryName2.Settings)
                || isImage(entryName);
    }

    private boolean isImage(String name) {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(ImageExt.Png)
                || lowerName.endsWith(ImageExt.Jpg)
                || lowerName.endsWith(ImageExt.Jpeg)
                || lowerName.endsWith(ImageExt.Gif)
                || lowerName.endsWith(ImageExt.Tif)
                || lowerName.endsWith(ImageExt.Bmp);
    }

    public void META_INF_container_xml() throws Exception {
        writeChild(ElementWriterSort.Container, hwpxFile.containerXMLFile());
        putIntoZip(ZipEntryName.Container, xsb().toString());
    }

    private void content_hpf() throws Exception {
        String packageXMLFilePath = hwpxFile.containerXMLFile().packageXMLFilePath();
        if (packageXMLFilePath != null) {
            writeChild(ElementWriterSort.Content, hwpxFile.contentHPFFile());
            putIntoZip(packageXMLFilePath, xsb().toString());
        }
    }

    private void contentFiles() throws Exception {
        if (hwpxFile.contentHPFFile().manifest() == null) {
            return;
        }

        for (ManifestItem item : hwpxFile.contentHPFFile().manifest().items()) {
            if (item.id().equals(FileIDs.Settings)) {
                writeChild(ElementWriterSort.Settings, hwpxFile.settingsXMLFile());
                putIntoZip(item.href(), xsb().toString());
            } else if (item.id().equals(FileIDs.Header)) {
                writeChild(ElementWriterSort.Header, hwpxFile.headerXMLFile());
                putIntoZip(item.href(), xsb().toString());
            } else if (item.id().startsWith(FileIDs.Section_Prefix)) {
                int sectionIndex = Integer.parseInt(item.id().substring(FileIDs.Section_Prefix.length()));
                writeChild(ElementWriterSort.Section, hwpxFile.sectionXMLFileList().get(sectionIndex));
                putIntoZip(item.href(), xsb().toString());
            } else if (item.id().startsWith(FileIDs.MasterPage_PreFix)) {
                int masterPageIndex = Integer.parseInt(item.id().substring(FileIDs.MasterPage_PreFix.length()));
                writeChild(ElementWriterSort.MasterPage, hwpxFile.masterPageXMLFileList().get(masterPageIndex));
                putIntoZip(item.href(), xsb().toString());
            } else if (item.hasAttachedFile() && item.attachedFile() != null) {
                putIntoZip(item.href(), item.attachedFile().data());
            }
        }
    }

    private void chartFiles() throws Exception {
        for (ChartXMLFile chartXMLFile : hwpxFile.chartXMLFileList().items()) {
            putIntoZip(chartXMLFile.path(), chartXMLFile.data());
        }
    }

    private void etcContainedFile() throws Exception {
        if (hwpxFile.containerXMLFile().rootFiles() == null) {
            return;
        }

        for (RootFile rootFile : hwpxFile.containerXMLFile().rootFiles().items()) {
            if (!MineTypes.HWPML_Package.equals(rootFile.mediaType())
                    && rootFile.attachedFile() != null) {
                putIntoZip(rootFile.fullPath(), rootFile.attachedFile().data());
            }
        }
    }

    private void unparsedXMLFiles() throws Exception {
        for (UnparsedXMLFile unparsedXMLFile : hwpxFile.unparsedXMLFiles()) {
            putIntoZip(unparsedXMLFile.href(), unparsedXMLFile.xml());
        }
    }

    private void writeManifest() throws Exception {
        ManifestSetter.reset(hwpxFile.manifestXMLFile(), encryptedFileInfos);
        writeChild(ElementWriterSort.Manifest, hwpxFile.manifestXMLFile());
        putIntoZip(ZipEntryName.Manifest, xsb().toString());
    }

    private void close() throws IOException {
        zos.close();
    }
}
