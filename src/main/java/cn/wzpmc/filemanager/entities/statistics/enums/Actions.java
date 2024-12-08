package cn.wzpmc.filemanager.entities.statistics.enums;

import com.alibaba.fastjson2.annotation.JSONCompiled;

@JSONCompiled
public enum Actions {
    /**
     * 文件上传事件
     */
    UPLOAD,
    /**
     * 文件删除事件
     */
    DELETE,
    /**
     * 访问页面事件
     */
    ACCESS,
    /**
     * 文件下载事件
     */
    DOWNLOAD,
    /**
     * 搜索文件事件
     */
    SEARCH,
    /**
     * 登录事件
     */
    LOGIN,
    /**
     * 获取邀请码事件
     */
    INVITE,
    /**
     * 注册事件
     */
    REGISTER
}