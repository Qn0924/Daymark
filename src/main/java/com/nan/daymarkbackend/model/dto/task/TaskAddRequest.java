package com.nan.daymarkbackend.model.dto.task;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加任务
 */
@Data
public class TaskAddRequest implements Serializable {
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务分类
     */
    private String taskCategory;

    /**
     * 任务说明
     */
    private String description;

    /**
     * 要求时长，分钟
     */
    private Integer requireDuration;

    /**
     * 是否需要照片：1是 0否
     */
    private Integer requirePhoto;

    /**
     * 任务开始日期
     */
    private Date startDate;

    /**
     * 任务结束日期
     */
    private Date endDate;


    private static final long serialVersionUID = 1L;
}