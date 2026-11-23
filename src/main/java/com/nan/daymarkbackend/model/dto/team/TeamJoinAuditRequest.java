package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核入团申请请求
 */
@Data
public class TeamJoinAuditRequest implements Serializable {

    /**
     * 申请记录id
     */
    private Long memberId;

    /**
     * 审批结果true 通过 / false 驳回
     */
    private Boolean approved;

    /**
     * 审批意见
     */
    private String reviewRemark;

    private static final long serialVersionUID = 1L;
}