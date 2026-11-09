package com.nan.daymarkbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TaskResubmitRequest implements Serializable {
    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务说明
     */
    private String description;

    /**
     * 要求时长，分钟
     */
    private Integer requireDuration;

    /**
     * 任务结束日期
     */
    private Date endDate;

    private static final long serialVersionUID = 1L;
}