package com.nan.daymarkbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 计时枚举类
 */
@Getter
public enum StudyTimeStatusEnum {

    RUNNING("进行中", "running"),
    FINISHED("已结束", "finished"),
    ABANDONED("已放弃", "abandoned"),
    EXPIRED("已作废", "expired");

    private final String text;
    private final String value;

    StudyTimeStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static StudyTimeStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (StudyTimeStatusEnum anEnum : StudyTimeStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}