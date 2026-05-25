package cn.wzpmc.filemanager.annotation;

import cn.wzpmc.filemanager.entities.user.enums.Auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要验证或注入用户。
 * <p>代码样例：
 * <pre>{@code
 * @GetMapping("/api/user/info")
 * public String getUserInfo(@AuthorizationRequired UserVo user) {
 *     return user.getName();
 * }
 * // 不带token/token错误 -> 403
 * // 带token -> 用户名
 * }
 * </pre>
 * <p>也可以不在参数中使用：
 * <pre>{@code
 * @GetMapping("/api/user/something")
 * @AuthorizationRequired
 * public String endpointRequireLogin() {
 *     return "success";
 * }
 * // 不带token/token错误 -> 403
 * // 带token -> success
 * }</pre>
 * </p>
 *
 * @see Auth
 * @see #level()
 * @see #force()
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizationRequired {
    /**
     * 用于控制需要的用户等级，默认为普通用户
     * <p>代码样例：
     * <pre>{@code
     * @GetMapping("/api/user/info")
     * public String getUserInfo(@AuthorizationRequired(level = Auth.admin) UserVo user) {
     *     return user.getName();
     * }
     * // 不带token/token错误 -> 403
     * // 非管理员用户访问 -> 403
     * // 管理员用户访问 -> 用户名
     * }
     * }</pre>
     * </p>
     *
     * @return 最低需要的用户等级
     * @see Auth
     */
    Auth level() default Auth.user;

    /**
     * 用于控制是否强制为该用户等级，默认为{@code false}
     * <p>代码样例：
     * <pre>{@code
     * // 不使用force
     * @GetMapping("/api/user/info")
     * public String getUserInfo(@AuthorizationRequired(level = Auth.user) UserVo user) {
     *     return user.getName();
     * }
     * // 不带token/token错误 -> 403
     * // 非管理员用户访问 -> 用户名
     * // 管理员用户访问 -> 用户名
     *
     * // 使用force
     * @GetMapping("/api/user/nonAdmin-info")
     * public String getNonAdminInfo(@AuthorizationRequired(level = Auth.user, force = true) UserVo user) {
     *     return user.getName();
     * }
     * // 不带token/token错误 -> 403
     * // 管理员用户访问 -> 403
     * // 非管理员用户访问 -> 用户名
     * }
     * }</pre>
     * </p>
     *
     * @return 最低需要的用户等级
     * @see Auth
     */
    boolean force() default false;
}