package cn.wzpmc.filemanager.entities.files;

import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONCompiled
public class RawFileObject {
    private long id;
    private String name;
    private String ext;
    private long size;
    private long owner;
    private long parent;
    private Date time;
    private FileType type;

    public static RawFileObject of(FileVo file) {
        return new RawFileObject(file.getId(), file.getName(), file.getExt(), file.getSize(), file.getUploader(), file.getFolder(), file.getUploadTime(), FileType.FILE);
    }

    public static RawFileObject of(FolderVo folder) {
        return new RawFileObject(folder.getId(), folder.getName(), null, -1, folder.getCreator(), folder.getParent(), folder.getCreateTime(), FileType.FOLDER);
    }
    public static String getRawFileName(RawFileObject object){
        return object.type.equals(FileType.FILE) ? object.getName() + '.' + object.getExt() : object.getName();
    }
}