package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDao dao;
    @Autowired
    public UserService(UserDao dao){
        this.dao = dao;
    }
}
