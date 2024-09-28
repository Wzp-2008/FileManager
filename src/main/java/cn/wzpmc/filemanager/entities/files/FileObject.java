package cn.wzpmc.filemanager.entities.files;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileObject extends RawFileObject {
    private String ext;
    private String mime;
    private String sha1;
}