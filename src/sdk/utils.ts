import { hash } from "spark-md5";
import { cloneDeep } from "lodash-es";
import type { RawFileType, User } from "./entities";

/**
 * 对密码进行md5加密
 * @param obj 拥有密码（password）属性的对象
 * @return 一个新对象，其中的密码（password）属性为加密后的
 * @example hashPassword({password: "123456"}) -> {password: "e10adc3949ba59abbe56e057f20f883e"}
 */
export const hashPassword = <T extends { password: string }>(obj: T): T => {
  const newObj = cloneDeep(obj);
  newObj.password = hash(obj.password);
  return newObj;
};

/**
 * 被包裹的值（在ref一个列表时可以通过修改current的值实现响应式）
 */
export interface WrapValue<T> {
  /**
   * 当前值
   */
  current: T;
}

/**
 * 包裹一个值
 * @see WrapValue
 * @param value 原始值
 */
export const wrap = <T>(value: T): WrapValue<T> => {
  return { current: value };
};
/**
 * 将一个字节为单位的大小数据加上单位
 * @param size 以字节(B)为单位的文件大小
 * @return 大小+单位，保留两位小鼠
 * @example
 * humanitySize(1023) -> "1023B"
 * humanitySize(1024) -> "1.00KB"
 */
export const humanitySize = (size: number): string => {
  if (size < 1024) {
    return `${size}B`;
  }
  if (size < 1024 ** 2) {
    return `${(size / 1024).toFixed(2)}KB`;
  }
  if (size < 1024 ** 3) {
    return `${(size / 1024 ** 2).toFixed(2)}MB`;
  }
  if (size < 1024 ** 4) {
    return `${(size / 1024 ** 3).toFixed(2)}GB`;
  }
  return `${(size / 1024 ** 4).toFixed(2)}TB`;
};

export const canIDelete = (
  user: User | null,
  file: { type: RawFileType; owner: number },
): boolean => {
  return !!(
    user &&
    (file.type === "FOLDER"
      ? user.auth === "admin"
      : file.owner === user.id || user.auth === "admin")
  );
};

export const getFingerprint = async (): Promise<string> => {
  const FingerprintJS = await import("@fingerprintjs/fingerprintjs");
  const fJs = await FingerprintJS.load();
  const result = await fJs.get();
  return result.visitorId;
};
