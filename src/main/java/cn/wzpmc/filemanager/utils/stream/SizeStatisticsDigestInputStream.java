package cn.wzpmc.filemanager.utils.stream;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * 兼具大小统计以及哈希计算的输入流
 */
@Getter
public class SizeStatisticsDigestInputStream extends DigestInputStream {
    /**
     * 文件大小，单位：字节
     */
    protected long size = 0;

    public SizeStatisticsDigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream, digest);
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1) size++;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read != -1) size += read;
        return read;
    }
}
