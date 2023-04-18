package cn.wzpmc.filemanager.dao;

import cn.wzpmc.filemanager.entities.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {
    User getUser(@Param("username") String username,@Param("password") String password);

    void updateUserIP(User user);

    void addUser(User user);
}
