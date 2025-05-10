package cn.wzpmc.filemanager.mapper;

import cn.wzpmc.filemanager.entities.vo.FileVo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<FileVo> {
}