package cn.wzpmc.filemanager.mapper;

import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.entities.vo.UserLoginVo;
import cn.wzpmc.filemanager.entities.vo.UserRegisterVo;
import cn.wzpmc.filemanager.enums.Auth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void createDefault();
    User getUser(UserLoginVo loginVo);
    int countUser();
    void addUser(User registerVo);
    int getUserCountByName(String name);
    Auth getUserAuthById(int id);
}
