package com.nan.daymarkbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 打卡/补卡请求
 */
@Data
public class TaskCheckinRequest implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 打卡照片url，任务要求照片时必传
     */
    private String photoUrl;

    /**
     * 打卡日期
     */
    private Date checkinDate;

    private static final long serialVersionUID = 1L;
}