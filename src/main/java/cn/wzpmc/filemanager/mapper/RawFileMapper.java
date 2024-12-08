package cn.wzpmc.filemanager.mapper;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RawFileMapper extends BaseMapper<FullRawFileObject> {
}
