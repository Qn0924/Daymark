package com.nan.daymarkbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求
 */
@Data
public class TaskAuditRequest implements Serializable {
    /**
     * 任务id
     */
    private Long taskId;

    /**
     * true 通过 / false 驳回
     */
    private Boolean approved;

    private static final long serialVersionUID = 1L;
}