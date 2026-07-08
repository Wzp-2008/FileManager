package cn.wzpmc.filemanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入用户地址（优先级：X-RealIP头 -> X-Forwarded-For头 -> 连接地址）
 * <p>代码样例：
 * <pre>{@code
 * @GetMapping("/api/user/ip")
 * public String getAddress(@Address String address) {
 *     return address;
 * }
 * // 访问 -> 用户访问IP
 * }</pre>
 * </p>
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Address {
}