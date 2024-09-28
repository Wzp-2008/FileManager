package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;

import java.util.Date;

public record StatisticsVo(int actor, Actions action, String params, Date time) {
}