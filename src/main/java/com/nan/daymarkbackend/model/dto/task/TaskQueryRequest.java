package com.nan.daymarkbackend.model.dto.task;

import com.nan.daymarkbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQueryRequest extends PageRequest {
    /**
     * 模糊搜索
     */
    private String taskName;

    /**
     * 精确匹配
     */
    private String taskCategory;

    /**
     * 任务状态
     */
    private String taskStatus;
}