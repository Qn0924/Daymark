package com.nan.daymarkbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum TeamMemberStatusEnum {

    PENDING("待审批", "pending"),
    ACTIVE("正常", "active"),
    REJECTED("已拒绝", "rejected"),
    LEFT("已退出", "left"),
    REMOVED("已移除", "removed"),
    CANCELLED("已取消", "cancelled");

    private final String text;

    private final String value;

    TeamMemberStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static TeamMemberStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (TeamMemberStatusEnum anEnum : TeamMemberStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}