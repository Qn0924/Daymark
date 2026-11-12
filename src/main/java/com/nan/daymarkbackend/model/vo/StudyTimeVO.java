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

    /**
     * 服务端计算的已学习秒数
     */
    private Long studyedSeconds;

    /**
     * 有效分钟数，封顶 480
     */
    private Integer effectiveMinutes;

    /**
     * 是否已超过单次 8 小时上限
     * */
    private Boolean capped;

    /**
     * 是否已满足任务要求时长
     */
    private Boolean reachRequire;

    /**
     * 服务端当前时间，前端用于校正
     */
    private Date serverTime;

    private static final long serialVersionUID = 1L;
}