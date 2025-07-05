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
    /**
     * 文件ID
     */
    private long id;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件扩展名（文件夹为null）
     */
    private String ext;
    /**
     * 文件大小（文件夹为-1）
     */
    private long size;
    /**
     * 文件所有者
     */
    private long owner;
    /**
     * 父文件夹ID
     */
    private long parent;
    /**
     * 文件上传时间
     */
    private Date time;
    /**
     * 文件类型
     */
    private FileType type;
    /**
     * 文件mime类型
     */
    private String mime;

    public static RawFileObject of(FileVo file) {
        return new RawFileObject(file.getId(), file.getName(), file.getExt(), file.getSize(), file.getUploader(), file.getFolder(), file.getUploadTime(), FileType.FILE, file.getMime());
    }

    public static RawFileObject of(FolderVo folder) {
        return new RawFileObject(folder.getId(), folder.getName(), null, -1, folder.getCreator(), folder.getParent(), folder.getCreateTime(), FileType.FOLDER, "folder");
    }
    public static String getRawFileName(RawFileObject object){
        return object.type.equals(FileType.FILE) ? object.getName() + '.' + object.getExt() : object.getName();
    }
}