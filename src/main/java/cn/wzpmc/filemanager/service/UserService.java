package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.dao.UserDao;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDao dao;
    private final JwtUtils jwtUtils;
    @Autowired
    public UserService(UserDao dao, JwtUtils jwtUtils){
        this.dao = dao;
        this.jwtUtils = jwtUtils;
    }

    public User login(User user, String remoteAddr, HttpServletResponse response) {
        User responseUser = this.dao.getUser(user.getUsername(), user.getPassword());
        if (responseUser == null){
            return null;
        }
        String lastLoginedIP = responseUser.getLastLoginedIP();
        if (!lastLoginedIP.equals(remoteAddr)){
            responseUser.setLastLoginedIP(remoteAddr);
            this.dao.updateUserIP(responseUser);
        }
        response.setHeader("Authorization", jwtUtils.createToken(responseUser));
        return responseUser;
    }
    public User register(User user, String remoteAddr, HttpServletResponse response){
        user.setLastLoginedIP(remoteAddr);
        this.dao.addUser(user);
        user.setPassword(null);
        response.setHeader("Authorization", jwtUtils.createToken(user));
        return user;
    }
}
