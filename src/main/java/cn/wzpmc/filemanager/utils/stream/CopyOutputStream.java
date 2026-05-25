package cn.wzpmc.filemanager.utils.stream;

import org.jspecify.annotations.NonNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CopyOutputStream extends FilterOutputStream {
    private final OutputStream secondary;

    public CopyOutputStream(OutputStream primary, OutputStream secondary) {
        super(primary);
        this.secondary = secondary;
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
        this.secondary.write(b);
    }

    @Override
    public void write(byte @NonNull [] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        this.secondary.write(b, off, len);
    }

    @Override
    public void write(byte @NonNull [] b) throws IOException {
        this.out.write(b);
        this.secondary.write(b);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
        this.secondary.close();
    }
}
