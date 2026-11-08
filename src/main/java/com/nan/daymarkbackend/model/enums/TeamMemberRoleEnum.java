package com.nan.daymarkbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 团队成员枚举类
 */
@Getter
public enum TeamMemberRoleEnum {

    OWNER("负责人", "owner"),
    WORKER("普通成员", "worker"),
    MENTOR("监督者", "mentor");

    private final String text;

    private final String value;

    TeamMemberRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static TeamMemberRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (TeamMemberRoleEnum anEnum : TeamMemberRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}