package cn.wzpmc.filemanager.entities.vo;

import java.util.Date;

public record FileVo(int id, String name, String ext, String mime, String sha1, int uploader, int folder, Date uploadTime) {
}