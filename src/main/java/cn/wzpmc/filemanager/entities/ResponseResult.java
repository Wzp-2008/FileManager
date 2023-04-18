package cn.wzpmc.filemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseResult<T> {
    private T data;
}
