package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 处理邀请请求被邀请人操作
 */
@Data
public class TeamInviteHandleRequest implements Serializable {

    /**
     * 邀请记录id
     */
    private Long memberId;

    /**
     * 接受/拒绝
     */
    private Boolean accepted;

    /**
     * 拒绝理由
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}