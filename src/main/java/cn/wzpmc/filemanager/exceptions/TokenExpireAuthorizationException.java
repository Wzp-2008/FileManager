package cn.wzpmc.filemanager.exceptions;

import cn.wzpmc.filemanager.entities.Result;
import org.springframework.http.HttpStatus;

/**
 * Token过期错误
 */
public class TokenExpireAuthorizationException extends ResponseException {
    public TokenExpireAuthorizationException() {
        super(Result.failed(HttpStatus.UNAUTHORIZED, "token错误或已过期"));
    }

    /**
     * 创建一个过期错误
     *
     * @return token过期错误
     */
    public static TokenExpireAuthorizationException of() {
        return new TokenExpireAuthorizationException();
    }
}
