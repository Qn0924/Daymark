package com.nan.daymarkbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum TeamStatusEnum {

    PENDING("待审核", "pending"),
    ACTIVE("正常", "active"),
    REJECTED("已驳回", "rejected"),
    CLOSED("已解散", "closed");

    private final String text;

    private final String value;

    TeamStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static TeamStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (TeamStatusEnum anEnum : TeamStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}