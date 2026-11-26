package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出团队
 */
@Data
public class TeamMemberLeaveRequest implements Serializable {
    /**
     * 团队id
     */
    private Long teamId;

}