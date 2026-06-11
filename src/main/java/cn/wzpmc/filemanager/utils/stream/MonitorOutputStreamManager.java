package cn.wzpmc.filemanager.utils.stream;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitorOutputStreamManager {
    private final Map<String, Boolean> monitor = new ConcurrentHashMap<>();

    private class Impl extends FilterOutputStream {
        private final String monitorId;

        public Impl(OutputStream out, String monitorId) {
            super(out);
            this.monitorId = monitorId;
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            monitor.put(monitorId, true);
        }

        @Override
        public void write(byte @NonNull [] b, int off, int len) throws IOException {
            out.write(b, off, len);
            monitor.put(monitorId, true);
        }

        @Override
        public void write(byte @NonNull [] b) throws IOException {
            out.write(b);
            monitor.put(monitorId, true);
        }

        @Override
        public void close() throws IOException {
            super.close();
            monitor.remove(monitorId);
        }
    }

    public OutputStream open(OutputStream out, String monitorId) throws IOException {
        return new Impl(out, monitorId);
    }

    public List<String> getActiveAndReset() {
        List<String> res = this.monitor.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
        this.monitor.clear();
        return res;
    }
}
