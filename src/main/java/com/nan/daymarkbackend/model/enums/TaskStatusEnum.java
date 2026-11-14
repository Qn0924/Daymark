package com.nan.daymarkbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 任务状态枚举类
 */
@Getter
public enum TaskStatusEnum {

    PENDING("待审核", "pending"),
    REJECTED("已驳回", "rejected"),
    OPEN("进行中", "open"),
    CLOSE("已关闭", "close");

    private final String text;

    private final String value;

    TaskStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static TaskStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (TaskStatusEnum anEnum : TaskStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}