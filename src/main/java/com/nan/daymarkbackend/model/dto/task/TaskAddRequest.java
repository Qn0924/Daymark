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
     * 打卡记录id
     */
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 所属团队id
     */
    private Long teamId;

    /**
     * 任务创建人用户id
     */
    private Long creatorUserId;

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

    /**
     * 任务状态：pending/open/close
     */
    private String taskStatus;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 任务创建时间
     */
    private Date taskCreateTime;

    /**
     * 打卡用户id，任务定义行为null
     */
    private Long userId;

    /**
     * 打卡日期，任务定义行为null
     */
    private Date checkinDate;

    /**
     * 打卡时间，任务定义行为null
     */
    private Date checkinTime;

    /**
     * 本次学习时长，分钟，任务定义行为null
     */
    private Integer duration;

    /**
     * 照片路径
     */
    private String photoUrl;

    /**
     * normal正常/repair补卡
     */
    private String recordStatus;

    /**
     * 本次积分变动
     */
    private Integer points;

    /**
     * 打卡记录创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}