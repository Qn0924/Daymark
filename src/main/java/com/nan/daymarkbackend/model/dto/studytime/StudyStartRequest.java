package com.nan.daymarkbackend.model.dto.studytime;

import lombok.Data;

import java.io.Serializable;

/**
 * 开始计时请求
 */
@Data
public class StudyStartRequest implements Serializable {

    /** 打卡任务id */
    private Long taskId;

    private static final long serialVersionUID = 1L;
}