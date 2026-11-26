package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamMemberGrantRequest implements Serializable {
    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 目标用户id
     */
    private Long targetUserId;
}