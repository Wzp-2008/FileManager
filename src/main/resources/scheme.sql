CREATE TABLE IF NOT EXISTS `file`
(
    `id`          int                                                          NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `name`        varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名',
    `ext`         varchar(40) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '文件扩展名',
    `mime`        varchar(100) COLLATE utf8mb4_general_ci                      NOT NULL COMMENT '文件的MIME类型',
    `hash`        char(128) COLLATE utf8mb4_general_ci                         NOT NULL COMMENT '文件sha512',
    `uploader`    int                                                          NOT NULL COMMENT '文件上传者',
    `folder`      int                                                                   DEFAULT NULL COMMENT '所属文件夹ID',
    `size`        bigint                                                       NOT NULL,
    `upload_time` datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `path` (`name`, `ext`, `folder`),
    KEY `ext` (`ext`),
    KEY `sha1` (`hash`),
    KEY `upload_time` (`upload_time`),
    KEY `uploader` (`uploader`),
    KEY `name` (`name`, `ext`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2845
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件';
CREATE TABLE IF NOT EXISTS `folder`
(
    `id`          int                                                          NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
    `name`        varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件夹名称',
    `parent`      int                                                                   DEFAULT NULL COMMENT '父文件夹ID',
    `creator`     int                                                                   DEFAULT NULL COMMENT '创建者ID',
    `create_time` datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `path` (`name`, `parent`),
    KEY `name` (`name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3118
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件夹';
CREATE TABLE IF NOT EXISTS `statistics`
(
    `actor`            int                                                                                                                   DEFAULT NULL COMMENT '操作者',
    `action`           enum ('UPLOAD','DELETE','ACCESS','DOWNLOAD','SEARCH','LOGIN','INVITE','REGISTER') COLLATE utf8mb4_general_ci NOT NULL COMMENT '所做的操作',
    `params`           varchar(255) COLLATE utf8mb4_general_ci                                                                               DEFAULT NULL COMMENT '操作的参数（在ACCESS操作中为空）',
    `time`             datetime                                                                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作的时间',
    `download_file_id` int GENERATED ALWAYS AS (if((`action` = _utf8mb4'DOWNLOAD'),
                                                   json_unquote(json_extract(`params`, _utf8mb4'$.id')),
                                                   NULL)) VIRTUAL COMMENT '对于下载类型事件的文件ID',
    KEY `action` (`action`) COMMENT '操作类型索引',
    KEY `actor_index` (`actor`) COMMENT '操作者索引',
    KEY `time` (`time`) COMMENT '时间索引',
    KEY `download_file_id_index` (`download_file_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='统计信息';
CREATE TABLE IF NOT EXISTS `user`
(
    `id`       int                                                                    NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `name`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           NOT NULL COMMENT '用户名',
    `password` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           NOT NULL COMMENT '用户密码（MD5+SHA1）',
    `auth`     enum ('admin','user') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'user' COMMENT '用户类型',
    `banned`   tinyint(1)                                                             NOT NULL DEFAULT '0' COMMENT '用户是否被封禁',
    PRIMARY KEY (`id`) COMMENT 'ID索引',
    UNIQUE KEY `name_pk` (`name`),
    UNIQUE KEY `login_index` (`name`, `password`) COMMENT '用户名密码索引',
    UNIQUE KEY `id_index` (`id`) COMMENT 'ID索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户表';
CREATE OR REPLACE VIEW `raw_file` AS
select `file`.`id`                AS `id`,
       `file`.`name`              AS `name`,
       `file`.`ext`               AS `ext`,
       `file`.`size`              AS `size`,
       `file`.`folder`            AS `parent`,
       `file`.`uploader`          AS `owner`,
       `user`.`name`              AS `owner_name`,
       `file`.`upload_time`       AS `time`,
       count(`statistics`.`time`) AS `down_count`,
       'FILE'                     AS `type`
from ((`file` left join `user`
       on (((`user`.`id` = `file`.`uploader`) and (`user`.`banned` = 0)))) left join `statistics`
      on (((`statistics`.`action` = 'DOWNLOAD') and (`file`.`id` = `statistics`.`download_file_id`))))
group by `file`.`id`
union all
select `folder`.`id`          AS `id`,
       `folder`.`name`        AS `name`,
       NULL                   AS `ext`,
       -(1)                   AS `size`,
       `folder`.`parent`      AS `parent`,
       `folder`.`creator`     AS `owner`,
       `user`.`name`          AS `owner_name`,
       `folder`.`create_time` AS `time`,
       0                      AS `down_count`,
       'FOLDER'               AS `type`
from (`folder` left join `user` on (((`user`.`id` = `folder`.`creator`) and (`user`.`banned` = 0))));
