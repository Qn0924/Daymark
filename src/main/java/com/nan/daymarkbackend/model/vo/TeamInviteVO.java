package com.nan.daymarkbackend.model.vo;

import com.nan.daymarkbackend.model.entity.TeamMember;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 我收到的邀请
 */
@Data
public class TeamInviteVO implements Serializable {

    /**
     * 邀请记录id
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
     * 邀请人id
     */
    private Long inviterUserId;

    /**
     * 邀请人名称
     */
    private String inviterName;

    /**
     * 邀请人头像
     */
    private String inviterAvatar;

    /**
     * 状态
     */
    private String status;

    /**
     * 角色
     */
    private String role;

    /**
     * 邀请留言
     */
    private String message;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

    public static TeamInviteVO ObjToVO(TeamMember member) {
        TeamInviteVO vo = new TeamInviteVO();
        vo.setId(member.getId());
        vo.setTeamId(member.getTeamId());
        vo.setRole(member.getRole());
        vo.setInviterUserId(member.getInviterUserId());
        vo.setStatus(member.getStatus());
        vo.setMessage(member.getApplyReason());
        vo.setCreateTime(member.getCreateTime());
        return vo;
    }
}