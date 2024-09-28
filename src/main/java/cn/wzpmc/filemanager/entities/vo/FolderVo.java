package cn.wzpmc.filemanager.entities.vo;

import java.util.Date;

public record FolderVo(int id, String name, int parent, int creator, Date createTime) {
}