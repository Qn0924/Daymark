package com.nan.daymarkbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 学习计时
 * @TableName studytime
 */
@TableName(value ="studytime")
@Data
public class Studytime implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 打卡任务id
     */
    private Long taskId;

    /**
     * 所属打卡团id
     */
    private Long teamId;

    /**
     * 开始学习时间
     */
    private Date startTime;

    /**
     * 结束学习时间
     */
    private Date endTime;

    /**
     * 计算出的时长，单位分钟
     */
    private Integer duration;

    /**
     * 状态：running进行中/finished已结束/abandoned已放弃/expired已作废
     */
    private String status;

    /**
     * 最后心跳时间
     */
    private Date lastHeartbeatTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}