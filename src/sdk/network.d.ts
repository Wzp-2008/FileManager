/**
 * 分页请求返回体
 */
export interface Pager<T> {
  /**
   * 数据总数
   */
  total: number;

  /**
   * 此页数据
   */
  data: T[];
}

/**
 * 请求返回体
 */
export interface Result<T> {
  /**
   * 请求返回状态码（若正确则为200）
   */
  status: number;
  /**
   * 请求返回消息（若错误则为错误信息）
   */
  msg: string;
  /**
   * 请求返回的数据
   */
  data: T;
  /**
   * 请求体创建的时间戳
   */
  timestamp: number;
}
