package cn.wzpmc.filemanager.interfaces;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

public interface FilePathService {
    @NonNull
    String getFilePath(@NonNull RawFileObject file);

    @Nullable
    FullRawFileObject resolveFile(@NonNull String[] path);
}
