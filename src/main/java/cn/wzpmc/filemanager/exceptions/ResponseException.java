package cn.wzpmc.filemanager.exceptions;

import cn.wzpmc.filemanager.entities.Result;
import lombok.Getter;

@Getter
public class ResponseException extends RuntimeException {
    /**
     * 对应要返回的结果
     */
    private final Result<Void> result;

    public ResponseException(Result<Void> result) {
        super(result.getMsg());
        this.result = result;
    }
}