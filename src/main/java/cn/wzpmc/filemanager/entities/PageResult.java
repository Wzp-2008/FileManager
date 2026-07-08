package cn.wzpmc.filemanager.entities;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 通用分页响应体
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@JSONCompiled
public class PageResult<T> {
    /**
     * 总行数
     */
    private long total;
    /**
     * 当前页数据
     */
    private List<T> data;
}
