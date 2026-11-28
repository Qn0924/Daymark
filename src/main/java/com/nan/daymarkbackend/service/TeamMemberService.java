package com.nan.daymarkbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nan.daymarkbackend.model.dto.team.*;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.vo.TeamInviteSentVO;
import com.nan.daymarkbackend.model.vo.TeamInviteVO;
import com.nan.daymarkbackend.model.vo.TeamMemberApplyVO;
import com.nan.daymarkbackend.model.vo.TeamMemberVO;

import java.util.List;

/**
* @author 21289
* @description 针对表【team_member(团队成员)】的数据库操作Service
* @createDate 2026-11-08 17:45:38
*/
public interface TeamMemberService extends IService<TeamMember> {

    /**
     * 申请加入团队
     * @param teamJoinApplyRequest   申请请求
     * @param loginUser 当前登录用户
     * @return
     */
    Boolean applyJoin(TeamJoinApplyRequest teamJoinApplyRequest, User loginUser);

    /**
     * 审核入团申请（仅团队负责人）
     *
     * @param teamJoinAuditRequest 审核请求
     * @param loginUser            当前登录用户
     * @return 是否审核成功
     */
    Boolean auditJoin(TeamJoinAuditRequest teamJoinAuditRequest, User loginUser);

    /**
     * 审批列表
     * @param teamApplyQueryRequest
     * @param loginUser
     * @return
     */
    Page<TeamMemberApplyVO> listAuditByPage(TeamQueryRequest teamApplyQueryRequest, User loginUser);

    /**
     * 我的申请列表
     * @param teamApplyQueryRequest
     * @param loginUser
     * @return
     */
    Page<TeamMemberApplyVO> listMyApplyByPage(TeamQueryRequest teamApplyQueryRequest, User loginUser);

    /**
     * 邀请成员
     *
     * @param teamInviteAddRequest
     * @param loginUser
     * @return
     */
    Boolean inviteMember(TeamInviteAddRequest teamInviteAddRequest, User loginUser);

    /**
     * 成员处理邀请
     *
     * @param teamInviteHandleRequest
     * @param loginUser
     * @return
     */
    Boolean handleInvite(TeamInviteHandleRequest teamInviteHandleRequest, User loginUser);

    /**
     * 我收到的邀请列表
     *
     * @param teamApplyQueryRequest
     * @param loginUser
     * @return
     */
    Page<TeamInviteVO> listMyInviteByPage(TeamQueryRequest teamApplyQueryRequest, User loginUser);

    /**
     * 我发出的邀请列表
     */
    Page<TeamInviteSentVO> listMySentInviteByPage(TeamQueryRequest teamApplyQueryRequest, User loginUser);

    /**
     * 退出团队
     * @param teamMemberLeaveRequest
     * @param loginUser
     * @return
     */
    Boolean leaveTeam(TeamMemberLeaveRequest teamMemberLeaveRequest, User loginUser);

    /**
     * 授予监督者
     * @param teamMemberGrantRequest
     * @param loginUser
     * @return
     */
    Boolean adminGrantMentor(TeamMemberGrantRequest teamMemberGrantRequest, User loginUser);

    /**
     * 移除成员
     * @param teamMemberRemoveRequest
     * @param loginUser
     * @return
     */
    Boolean removeMember(TeamMemberRemoveRequest teamMemberRemoveRequest, User loginUser);

    /**
     * 成员列表
     *
     * @param teamPageQueryRequest
     * @param loginUser
     * @return
     */
    Page<TeamMemberVO> listTeamMembers(TeamPageQueryRequest teamPageQueryRequest, User loginUser);
}
