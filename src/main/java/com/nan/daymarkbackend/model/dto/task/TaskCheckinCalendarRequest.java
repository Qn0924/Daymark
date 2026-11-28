package com.nan.daymarkbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 打卡日历查询请求
 */
@Data
public class TaskCheckinCalendarRequest implements Serializable {

    /**
     * 查询开始日期，格式 yyyy-MM-dd
     */
    private Date startDate;

    /**
     * 查询结束日期，格式 yyyy-MM-dd
     */
    private Date endDate;

    private static final long serialVersionUID = 1L;
}