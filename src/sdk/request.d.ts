import type { UserAuth } from "./entities";

/**
 * 用户登录请求体
 */
export interface UserLoginRequest {
  /**
   * 用户名
   */
  username: string;
  /**
   * 密码（不需要加密，在发送请求时sdk会加密这个密码）
   */
  password: string;
}

/**
 * 用户注册请求体（这个类继承了UserLoginRequest）
 */
export interface UserRegisterRequest extends UserLoginRequest {
  /**
   * 用户类型
   * @see UserAuth
   */
  auth: UserAuth;
  /**
   * 管理员用户的邀请码（Auth为user时不用填）
   */
  inviteCode?: string;
}
