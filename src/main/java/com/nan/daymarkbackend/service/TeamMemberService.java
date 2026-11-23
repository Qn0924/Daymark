package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.dto.team.TeamJoinApplyRequest;
import com.nan.daymarkbackend.model.dto.team.TeamJoinAuditRequest;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;

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
}
