package com.nan.daymarkbackend.model.vo;

import com.nan.daymarkbackend.model.entity.TeamMember;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 我发出的邀请
 */
@Data
public class TeamInviteSentVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 被邀请人id
     */
    private Long userId;

    /**
     * 被邀请人名称
     */
    private String userName;

    /**
     * 被邀请人头像
     */
    private String userAvatar;

    /**
     * 邀请人角色
     */
    private String role;

    /**
     * 状态
     */
    private String status;

    /**
     * 留言
     */
    private String message;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static TeamInviteSentVO ObjToVO(TeamMember member) {
        TeamInviteSentVO vo = new TeamInviteSentVO();
        vo.setId(member.getId());
        vo.setTeamId(member.getTeamId());
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        vo.setStatus(member.getStatus());
        vo.setMessage(member.getApplyReason());
        vo.setCreateTime(member.getCreateTime());
        vo.setUpdateTime(member.getUpdateTime());

        return vo;
    }
}