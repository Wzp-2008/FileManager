package cn.wzpmc.filemanager.entities.files.enums;

import com.alibaba.fastjson2.annotation.JSONCompiled;

/**
 * 文件类型
 */
@JSONCompiled
public enum FileType {
    /**
     * 文件
     */
    FILE,
    /**
     * 文件夹
     */
    FOLDER
}