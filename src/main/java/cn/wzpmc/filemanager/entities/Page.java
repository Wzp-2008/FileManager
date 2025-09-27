package cn.wzpmc.filemanager.entities;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JSONCompiled
public class Page<T> {
    private int total;
    private List<T> data;
}