package cn.wzpmc.filemanager.exceptions;

import cn.wzpmc.filemanager.entities.Result;
import org.springframework.http.HttpStatus;

/**
 * @since 2025/5/19 20:01
 * @author wzp
 * @version 1.0.0
 */
public class TokenExpireAuthorizationException extends AuthorizationException {
    public TokenExpireAuthorizationException() {
        super(Result.failed(HttpStatus.UNAUTHORIZED, "token错误或已过期"));
    }

    public static TokenExpireAuthorizationException of() {
        return new TokenExpireAuthorizationException();
    }
}
