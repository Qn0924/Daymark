package com.nan.daymarkbackend.model.dto.studytime;

import lombok.Data;

import java.io.Serializable;

/**
 * 结束计时请求
 */
@Data
public class StudyFinishRequest implements Serializable {

    /** 计时记录id */
    private Long studyTimeId;

    /** 结束方式：end 正常结束 / abandon 放弃 */
    private String action;

    private static final long serialVersionUID = 1L;
}