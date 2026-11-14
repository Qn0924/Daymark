package com.nan.daymarkbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class StudyTimeVO implements Serializable {

    /**
     * 计时记录id
     */
    private Long id;

    /**
     * 打卡任务id
     */
    private Long taskId;

    /**
     * 所属打卡团id
     */
    private Long teamId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 时长
     */
    private Integer duration;

    /**
     * 计时状态
     */
    private String status;

    private static final long serialVersionUID = 1L;
}