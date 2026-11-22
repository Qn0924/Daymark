package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 申请加入团队请求
 */
@Data
public class TeamJoinApplyRequest implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;


    private static final long serialVersionUID = 1L;
}