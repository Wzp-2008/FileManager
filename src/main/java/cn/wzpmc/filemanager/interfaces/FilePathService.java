package cn.wzpmc.filemanager.interfaces;

import cn.wzpmc.filemanager.entities.files.RawFileObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface FilePathService {
    @NonNull
    String getFilePath(@NonNull RawFileObject file);

    @Nullable
    RawFileObject resolveFile(@NonNull String[] path);
}
