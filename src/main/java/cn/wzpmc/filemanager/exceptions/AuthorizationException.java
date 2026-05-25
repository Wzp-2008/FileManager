package cn.wzpmc.filemanager.exceptions;

import cn.wzpmc.filemanager.entities.Result;
import lombok.Getter;

@Getter
public class AuthorizationException extends RuntimeException {
    /**
     * 对应要返回的结果
     */
    private final Result<Void> result;

    public AuthorizationException(Result<Void> result) {
        super(result.getMsg());
        this.result = result;
    }
}