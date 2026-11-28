package com.nan.daymarkbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskDeleteRequest implements Serializable {
    /**
     * 任务id
     */
    private Long taskId;
}