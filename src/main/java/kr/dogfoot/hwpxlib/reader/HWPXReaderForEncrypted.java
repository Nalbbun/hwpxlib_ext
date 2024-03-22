package kr.dogfoot.hwpxlib.reader;

import kr.dogfoot.hwpxlib.commonstrings.ErrorMessage;
import kr.dogfoot.hwpxlib.commonstrings.MineTypes;
import kr.dogfoot.hwpxlib.commonstrings.ZipEntryName;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.content.context_hpf.ManifestItem;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.Chart;
import kr.dogfoot.hwpxlib.object.metainf.FileEntry;
import kr.dogfoot.hwpxlib.object.metainf.RootFile;
import kr.dogfoot.hwpxlib.reader.common.ElementReaderManager;
import kr.dogfoot.hwpxlib.reader.util.ZipFileReader;
import kr.dogfoot.hwpxlib.util.EncryptUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HWPXReaderForEncrypted {
    public static HWPXFile fromFilepath(String filepath, String password) throws Exception {
        return fromFile(new File(filepath), password);
    }

    public static HWPXFile fromFile(File file, String password) throws Exception {
        HWPXReaderForEncrypted reader = new HWPXReaderForEncrypted();
        reader.openZipFile(file);
        reader.checkMineType();

        reader.createHWPXFileObject();

        reader.startKey(password);
        reader.read();

        reader.closeZipFile();

        return reader.hwpxFile;
    }

    private ZipFile zipFile;
    private HWPXFile hwpxFile;
    private byte[] startKey;
    private final ElementReaderManager entryReaderManager;

    public HWPXReaderForEncrypted() {
        zipFile = null;
        hwpxFile = null;
        entryReaderManager = new ElementReaderManager();
    }

    private void openZipFile(File file) throws IOException {
        zipFile = new ZipFile(file);
    }

    public void checkMineType() throws IOException {
        ZipEntry zipEntry = zipFile.getEntry(ZipEntryName.MineType);
        if (zipEntry != null) {
            InputStream is = zipFile.getInputStream(zipEntry);

            String text = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).readLine();

            if (!MineTypes.HWPX.equals(text)) {
                throw new IOException(ErrorMessage.Not_HWPX_File);
            }
        } else {
            throw new IOException(ErrorMessage.Not_HWPX_File);
        }
    }

    private void createHWPXFileObject() {
        hwpxFile = new HWPXFile();
    }

    private void read() throws Exception {
        versionXML();
        containerXML();
        manifestXML();
        contentHPF();
        packagedFiles();
        etcContainedFile();
    }

    public void startKey(String password) throws NoSuchAlgorithmException {
        startKey = EncryptUtil.startKey(password);
    }

    private void versionXML() throws Exception {
        new VersionXMLFileReader(entryReaderManager)
                .read(hwpxFile.versionXMLFile(), zipFile);
    }

    private void containerXML() throws Exception {
        new ContainerXMLFileReader(entryReaderManager)
                .read(hwpxFile.containerXMLFile(), zipFile);
    }

    private void manifestXML() throws Exception {
        new ManifestXMLFileReader(entryReaderManager)
                .read(hwpxFile.manifestXMLFile(), zipFile);
    }

    private void contentHPF() throws Exception {
        String packageXMLFilePath = hwpxFile.containerXMLFile().packageXMLFilePath();
        if (packageXMLFilePath != null) {
            new ContentHPFFileReader(entryReaderManager)
                    .read(hwpxFile.contentHPFFile(), zipFile, packageXMLFilePath);
        }
    }

    private void packagedFiles() throws Exception {
        ContentFilesReaderForEncrypted contentFilesReader = new ContentFilesReaderForEncrypted(entryReaderManager, startKey);

        for (ManifestItem item : hwpxFile.contentHPFFile().manifest().items()) {
            FileEntry fileEntry = encryptedFileEntry(item.href());

            if (MineTypes.XML.equals(item.mediaType())) {
                contentFilesReader.read(hwpxFile, item.href(), zipFile, fileEntry);
                if (contentFilesReader.stoppedParsing()) {
                    addUnparsedXMLFile(item.href(), fileEntry);
                }
            } else if (item.hasAttachedFile()) {
                byte[] data = ZipFileReader.readBinary(item.href(), zipFile);
                if (fileEntry != null) {
                    data = EncryptUtil.decrypt(data, startKey, fileEntry);
                }

                item.createAttachedFile();
                item.attachedFile().data(data);
            }
        }

        for (Chart chart : contentFilesReader.charts()) {
            FileEntry fileEntry = encryptedFileEntry(chart.chartIDRef());

            byte[] data = ZipFileReader.readBinary(chart.chartIDRef(), zipFile);
            if (fileEntry != null) {
                data = EncryptUtil.decrypt(data, startKey, fileEntry);
            }

            hwpxFile.chartXMLFileList().addNew()
                    .pathAnd(chart.chartIDRef())
                    .data(data);
        }
    }

    private FileEntry encryptedFileEntry(String href) {
        for (FileEntry fileEntry : hwpxFile.manifestXMLFile().fileEntries()) {
            if (fileEntry.fullPath().equals(href)) {
                return fileEntry;
            }
        }
        return null;
    }

    private void addUnparsedXMLFile(String href, FileEntry fileEntry) throws IOException {
        byte[] data = ZipFileReader.readBinary(href, zipFile);
        if (fileEntry != null) {
            data = EncryptUtil.decrypt(data, startKey, fileEntry);
        }

        hwpxFile.addUnparsedXMLFile(href,
                new String(data, StandardCharsets.UTF_8));
    }

    private void etcContainedFile() throws IOException {
        if (hwpxFile.containerXMLFile() != null &&
                hwpxFile.containerXMLFile().rootFiles() != null) {
            for (RootFile rootFile : hwpxFile.containerXMLFile().rootFiles().items()) {
                if (!MineTypes.HWPML_Package.equals(rootFile.mediaType())) {
                    FileEntry fileEntry = encryptedFileEntry(rootFile.fullPath());

                    byte[] data = ZipFileReader.readBinary(rootFile.fullPath(), zipFile);
                    if (fileEntry != null) {
                        data = EncryptUtil.decrypt(data, startKey, fileEntry);
                    }

                    rootFile.createAttachedFile();
                    rootFile.attachedFile().data(data);
                }
            }
        }
    }

    private void closeZipFile() throws IOException {
        zipFile.close();
    }
}
