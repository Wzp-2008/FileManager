package cn.wzpmc.filemanager.utils.stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SerialFileInputStream extends InputStream {
    private final List<File> files;
    private FileInputStream currentFileStream;
    private int filePointer = 0;

    @Override
    public int read() throws IOException {
        if (files.isEmpty()) {
            return -1;
        }
        if (currentFileStream == null) {
            File file = files.get(0);
            currentFileStream = new FileInputStream(file);
        }
        int read = currentFileStream.read();
        if (read == -1) {
            filePointer++;
            File file = files.get(filePointer);
            if (file == null) {
                return -1;
            }
            currentFileStream.close();
            currentFileStream = new FileInputStream(file);
            read = currentFileStream.read();
        }
        return read;
    }

    @Override
    public int read(byte @NonNull [] b, int off, int len) throws IOException {
        if (len == 0) return 0;
        if (files.isEmpty()) return -1;
        if (currentFileStream == null) {
            File file = files.get(0);
            currentFileStream = new FileInputStream(file);
        }
        int readBytes = 0;
        while (true) {
            int currentReadBytes = currentFileStream.read(b, off + readBytes, len - readBytes);
            if (currentReadBytes != -1) {
                readBytes += currentReadBytes;
                if (readBytes >= len) {
                    return readBytes;
                }
            }
            filePointer++;
            log.debug("POINTER: {}", filePointer);
            if (filePointer >= files.size()) {
                if (readBytes == 0) return -1;
                return readBytes;
            }
            File file = files.get(filePointer);
            currentFileStream.close();
            currentFileStream = new FileInputStream(file);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.currentFileStream != null) {
            this.currentFileStream.close();
        }
        super.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        this.filePointer = 0;
        currentFileStream = null;
    }

    @Override
    public long skip(long n) throws IOException {
        long originalN = n;
        if (currentFileStream != null) {
            currentFileStream.close();
            currentFileStream = null;
        }
        for (File file : files) {
            long usableSpace = Files.size(file.toPath());
            if (usableSpace > n) break;
            n -= usableSpace;
            filePointer++;
            if (filePointer >= files.size()) {
                return originalN - n;
            }
        }
        currentFileStream = new FileInputStream(files.get(filePointer));
        if (n > 0) {
            return currentFileStream.skip(n) + (originalN - n);
        }
        return originalN;
    }
}
