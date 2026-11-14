package com.nan.daymarkbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 加入团队方式枚举类
 */
@Getter
public enum TeamJoinTypeEnum {

    CREATE("创建", "create"),
    APPLY("申请", "apply"),
    INVITE("邀请", "invite"),
    ADMIN("管理员授予", "admin");

    private final String text;

    private final String value;

    TeamJoinTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static TeamJoinTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (TeamJoinTypeEnum anEnum : TeamJoinTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}