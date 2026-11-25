package com.nan.daymarkbackend.model.vo;

import com.nan.daymarkbackend.model.entity.TeamMember;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamMemberApplyVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 团队名字
     */
    private String teamName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名字
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 状态
     */
    private String status;

    /**
     * 角色
     */
    private String role;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 审批建议
     */
    private String reviewRemark;

    private static final long serialVersionUID = 1L;

    public static TeamMemberApplyVO ObjToVO(TeamMember member) {
        TeamMemberApplyVO vo = new TeamMemberApplyVO();
        vo.setId(member.getId());
        vo.setTeamId(member.getTeamId());
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        vo.setStatus(member.getStatus());
        vo.setApplyReason(member.getApplyReason());
        vo.setCreateTime(member.getCreateTime());
        vo.setReviewRemark(member.getReviewRemark());
        return vo;
    }
}