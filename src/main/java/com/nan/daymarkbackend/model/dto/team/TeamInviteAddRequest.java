package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 邀请成员请求（只有团队负责人可调用）
 */
@Data
public class TeamInviteAddRequest implements Serializable {

    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 目标用户id
     */
    private Long targetUserId;

    /**
     * 邀请留言
     */
    private String message;

    private static final long serialVersionUID = 1L;
}