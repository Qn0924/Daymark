package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;
import java.io.Serializable;

/**
 * 移除/踢出团队成员请求
 */
@Data
public class TeamMemberRemoveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队id
     */
    private Long teamId;


    /**
     * 记录id
     */
    private Long memberId;

}