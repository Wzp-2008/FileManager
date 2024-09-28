package cn.wzpmc.filemanager.entities.files;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FolderObject extends RawFileObject {
    private List<RawFileObject> children;
}