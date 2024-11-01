package cn.wzpmc.filemanager.exceptions;

import cn.wzpmc.filemanager.entities.Result;
import lombok.Getter;

@Getter
public class AuthorizationException extends RuntimeException {
    private final Result<Void> result;
    public AuthorizationException(Result<Void> result) {
        super(result.getMsg());
        this.result = result;
    }
}