package cn.wzpmc.filemanager.entities.files;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckChunkResponse {
    private boolean has;
    private String uploadCode;
    public static CheckChunkResponse has() {
        return new CheckChunkResponse(true, null);
    }
    public static CheckChunkResponse shouldUpload(String uploadCode) {
        return new CheckChunkResponse(false, uploadCode);
    }
}