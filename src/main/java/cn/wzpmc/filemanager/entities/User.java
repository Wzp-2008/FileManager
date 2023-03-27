package cn.wzpmc.filemanager.entities;

import lombok.Data;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String lastLoginedIP;
}
