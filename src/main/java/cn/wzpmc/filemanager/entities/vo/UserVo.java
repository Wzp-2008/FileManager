package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.user.enums.Auth;

public record UserVo(int id, String name, String password, Auth auth, boolean banned) {
}