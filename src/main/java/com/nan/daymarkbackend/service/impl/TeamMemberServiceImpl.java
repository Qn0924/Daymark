package com.nan.daymarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.exception.BusinessException;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.team.TeamJoinApplyRequest;
import com.nan.daymarkbackend.model.dto.team.TeamJoinAuditRequest;
import com.nan.daymarkbackend.model.entity.Task;
import com.nan.daymarkbackend.model.entity.Team;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.enums.*;
import com.nan.daymarkbackend.service.TaskService;
import com.nan.daymarkbackend.service.TeamMemberService;
import com.nan.daymarkbackend.mapper.TeamMemberMapper;
import com.nan.daymarkbackend.service.TeamService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;

/**
* @author 21289
* @description 针对表【team_member(团队成员)】的数据库操作Service实现
* @createDate 2026-11-08 17:45:38
*/
@Service
public class TeamMemberServiceImpl extends ServiceImpl<TeamMemberMapper, TeamMember>
    implements TeamMemberService{

    @Lazy
    @Resource
    private TaskService taskService;


    @Resource
    private TeamService teamService;

    /**
     * 申请加入团队
     * @param teamJoinApplyRequest   申请请求
     * @param loginUser 当前登录用户
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyJoin(TeamJoinApplyRequest teamJoinApplyRequest, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(teamJoinApplyRequest == null || teamJoinApplyRequest.getTaskId() == null || teamJoinApplyRequest.getTaskId() <= 0, ErrorCode.PARAMS_ERROR);
        //登录校验
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        //查任务
        Task task = taskService.lambdaQuery()
                .eq(Task::getTaskId, teamJoinApplyRequest.getTaskId())
                .isNull(Task::getUserId)
                .one();
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        ThrowUtils.throwIf(!TaskStatusEnum.OPEN.getValue().equals(task.getTaskStatus()), ErrorCode.OPERATION_ERROR, "任务已结束，无法加入");

        //查团队状态
        Team team = teamService.getById(task.getTeamId());
        ThrowUtils.throwIf(team == null, ErrorCode.NOT_FOUND_ERROR, "团队不存在");
        ThrowUtils.throwIf(!TeamStatusEnum.ACTIVE.getValue().equals(team.getStatus()), ErrorCode.OPERATION_ERROR, "团队当前不可加入");

        //查是否有该成员
        TeamMember existMember = this.lambdaQuery()
                .eq(TeamMember::getTeamId, team.getId())
                .eq(TeamMember::getUserId, loginUser.getId())
                .one();

            if (existMember != null) {
                String currentStatus = existMember.getStatus();
                String pendingStatus = TeamMemberStatusEnum.PENDING.getValue();

                // 正在审批中，直接拦截
                if (pendingStatus.equals(currentStatus)) {
                    ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "已提交申请，请等待审批");
                }

                // 已经加入团队了
                if (TeamMemberStatusEnum.ACTIVE.getValue().equals(currentStatus)) {
                    ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "已加入该团队，不可重复申请！");
                }

                // 如果是被拒绝、退出、被移除等状态，执行更新复用旧记录操作
                boolean updated = this.lambdaUpdate()
                        .eq(TeamMember::getId, existMember.getId())
                        // 加 in 条件防止并发脏更新
                        .in(TeamMember::getStatus, Arrays.asList(
                                TeamMemberStatusEnum.REJECTED.getValue(),
                                TeamMemberStatusEnum.LEFT.getValue(),
                                TeamMemberStatusEnum.REMOVED.getValue(),
                                TeamMemberStatusEnum.CANCELLED.getValue()))
                        .set(TeamMember::getStatus, pendingStatus)
                        .set(TeamMember::getRole, TeamMemberRoleEnum.WORKER.getValue())
                        .set(TeamMember::getJoinType, TeamJoinTypeEnum.APPLY.getValue())
                        .set(TeamMember::getApplyReason, teamJoinApplyRequest.getApplyReason())
                        // 清空历史数据
                        .set(TeamMember::getJoinTime, null)
                        .set(TeamMember::getReviewerUserId, null)
                        .set(TeamMember::getReviewTime, null)
                        .set(TeamMember::getReviewRemark, null)
                        .set(TeamMember::getLeaveTime, null)
                        .set(TeamMember::getLeaveReason, null)
                        .set(TeamMember::getLeaveUserId, null)
                        .update();

                // 如果更新失败，抛出异常
                ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "申请失败，请稍后重试");
                return true;
            }

            String pendingStatus = TeamMemberStatusEnum.PENDING.getValue();

            //若无，则赋值
            TeamMember member = new TeamMember();
            member.setTeamId(team.getId());
            member.setUserId(loginUser.getId());
            member.setRole(TeamMemberRoleEnum.WORKER.getValue());
            member.setStatus(pendingStatus);
            member.setJoinType(TeamJoinTypeEnum.APPLY.getValue());
            member.setApplyReason(teamJoinApplyRequest.getApplyReason());

        try {
            ThrowUtils.throwIf(!this.save(member), ErrorCode.OPERATION_ERROR, "申请加入团队失败，请稍后重试");
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复提交申请");
        }
        return true;

        }

    /**
     * 审核团队成员
     * @param teamJoinAuditRequest 审核请求
     * @param loginUser            当前登录用户
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean auditJoin(TeamJoinAuditRequest teamJoinAuditRequest, User loginUser) {
        ThrowUtils.throwIf(teamJoinAuditRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);

        //查谁申请
        TeamMember member = this.getById(teamJoinAuditRequest.getMemberId());
        ThrowUtils.throwIf(member == null, ErrorCode.NOT_FOUND_ERROR, "申请记录不存在");
        ThrowUtils.throwIf(!TeamMemberStatusEnum.PENDING.getValue().equals(member.getStatus()), ErrorCode.OPERATION_ERROR, "该申请已处理，请勿重复操作");

        //查团队负责人
        Team team = teamService.getById(member.getTeamId());
        ThrowUtils.throwIf(team == null, ErrorCode.NOT_FOUND_ERROR, "团队不存在");
        ThrowUtils.throwIf(!team.getOwnerUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "只有团队负责人可以审批");

        //查团队
        ThrowUtils.throwIf(!TeamStatusEnum.ACTIVE.getValue().equals(team.getStatus()), ErrorCode.OPERATION_ERROR, "团队当前不可审批");

        //查任务
        Task task = taskService.lambdaQuery()
                .eq(Task::getTeamId, team.getId())
                .isNull(Task::getUserId)
                .one();
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        ThrowUtils.throwIf(!TaskStatusEnum.OPEN.getValue().equals(task.getTaskStatus()), ErrorCode.OPERATION_ERROR, "任务已结束，无法通过申请");

        Date now = new Date();
        String finalStatus;
        Date finalJoinTime;

        Boolean approved = teamJoinAuditRequest.getApproved();
        if (approved) {
            finalStatus = TeamMemberStatusEnum.ACTIVE.getValue();
            finalJoinTime = now; // 审批通过，入团时间为当前时间
        } else {
            finalStatus = TeamMemberStatusEnum.REJECTED.getValue();
            finalJoinTime = null; // 审批拒绝，入团时间为空
        }

        boolean updated = this.lambdaUpdate()
                .eq(TeamMember::getId, member.getId())
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.PENDING.getValue())
                .set(TeamMember::getStatus, finalStatus)
                .set(TeamMember::getReviewerUserId, loginUser.getId())
                .set(TeamMember::getReviewTime, now)
                .set(TeamMember::getReviewRemark, teamJoinAuditRequest.getReviewRemark())
                .set(TeamMember::getJoinTime, finalJoinTime)
                .update();

        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "该申请已被处理，请刷新后重试");
        return true;
    }
}




