package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.constant.UserConstant;
import com.nan.daymarkbackend.exception.BusinessException;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.team.*;
import com.nan.daymarkbackend.model.entity.*;
import com.nan.daymarkbackend.model.enums.*;
import com.nan.daymarkbackend.model.vo.TeamInviteSentVO;
import com.nan.daymarkbackend.model.vo.TeamInviteVO;
import com.nan.daymarkbackend.model.vo.TeamMemberApplyVO;
import com.nan.daymarkbackend.model.vo.TeamMemberVO;
import com.nan.daymarkbackend.service.*;
import com.nan.daymarkbackend.mapper.TeamMemberMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private UserService userService;

    @Resource
    private StudytimeService studytimeService;

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
        ThrowUtils.throwIf(teamJoinApplyRequest == null, ErrorCode.PARAMS_ERROR);
        //登录校验
        ThrowUtils.throwIf(loginUser == null , ErrorCode.NOT_LOGIN_ERROR);

        //查任务
        Task task = taskService.lambdaQuery()
                .eq(Task::getTaskId, teamJoinApplyRequest.getTaskId())
                .isNull(Task::getUserId)
                .one();
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        ThrowUtils.throwIf(!TaskStatusEnum.OPEN.getValue().equals(task.getTaskStatus()), ErrorCode.OPERATION_ERROR, "任务当前不可加入");

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
                String existMemberRole = existMember.getRole();

                // 正在审批中，直接拦截
                if (pendingStatus.equals(currentStatus)) {
                    if (TeamMemberRoleEnum.MENTOR.getValue().equals(existMemberRole)) {
                        ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "您正在被邀请为监督者，请同意或拒绝");
                    }else{
                        ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "已有待处理的申请或邀请");
                    }
                }

                // 已经加入团队了
                if (TeamMemberStatusEnum.ACTIVE.getValue().equals(currentStatus)) {
                    if (TeamMemberRoleEnum.MENTOR.getValue().equals(existMemberRole)) {
                        ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "已是监督者，不可申请了！");
                    } else {
                        ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "已加入该团队，不可重复申请！");
                    }
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
                        .set(TeamMember::getRole, teamJoinApplyRequest.getRole())
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
            member.setRole(teamJoinApplyRequest.getRole());
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
        ThrowUtils.throwIf(!TaskStatusEnum.OPEN.getValue().equals(task.getTaskStatus()), ErrorCode.OPERATION_ERROR, "任务当前不开放");

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

    /**
     * 审批列表
     * @param teamQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<TeamMemberApplyVO> listAuditByPage(TeamQueryRequest teamQueryRequest, User loginUser) {
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        String status;
        if (StrUtil.isBlank(teamQueryRequest.getStatus())) {
            status = TeamMemberStatusEnum.PENDING.getValue();
        } else {
            status = teamQueryRequest.getStatus();
        }

        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("ownerUserId", loginUser.getId());
        List<Team> myTeamList = teamService.list(teamQueryWrapper);

        if (CollUtil.isEmpty(myTeamList)) {
            return buildPage(new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize()), new ArrayList<>());
        }

        List<Long> myTeamIds = new ArrayList<>();
        for (Team team : myTeamList) {
            myTeamIds.add(team.getId());
        }

        QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("teamId", myTeamIds);
        queryWrapper.eq("joinType", TeamJoinTypeEnum.APPLY.getValue());
        queryWrapper.eq("status", status);
        queryWrapper.orderByAsc("createTime");

        Page<TeamMember> page = this.page(new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize()), queryWrapper);

        List<TeamMemberApplyVO> voList = new ArrayList<>();
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<Long> teamIds = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            for (TeamMember member : page.getRecords()) {
                teamIds.add(member.getTeamId());
                userIds.add(member.getUserId());
            }

            Map<Long, List<Team>> teamMap = teamService
                    .listByIds(teamIds)
                    .stream()
                    .collect(Collectors.groupingBy(Team::getId));
            Map<Long, List<User>> userMap = userService
                    .listByIds(userIds)
                    .stream()
                    .collect(Collectors.groupingBy(User::getId));

            for (TeamMember member : page.getRecords()) {
                TeamMemberApplyVO vo = TeamMemberApplyVO.ObjToVO(member);

                if (teamMap.containsKey(member.getTeamId())) {
                    vo.setTeamName(teamMap.get(member.getTeamId()).get(0).getTeamName());
                }
                if (userMap.containsKey(member.getUserId())) {
                    User user = userMap.get(member.getUserId()).get(0);
                    vo.setUserName(user.getUserName());
                    vo.setUserAvatar(user.getUserAvatar());
                }
                voList.add(vo);
            }
        }
        return buildPage(page, voList);
    }

    /**
     * 我的申请加团列表
     * @param teamQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<TeamMemberApplyVO> listMyApplyByPage(TeamQueryRequest teamQueryRequest, User loginUser) {
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("joinType", TeamJoinTypeEnum.APPLY.getValue());
        queryWrapper.orderByDesc("createTime");

        Page<TeamMember> page = this.page(new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize()), queryWrapper);

        List<TeamMemberApplyVO> voList = new ArrayList<>();

        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<Long> teamIds = new ArrayList<>();
            for (TeamMember member : page.getRecords()) {
                teamIds.add(member.getTeamId());
            }

            Map<Long, List<Team>> teamMap = teamService
                    .listByIds(teamIds)
                    .stream()
                    .collect(Collectors.groupingBy(Team::getId));

            for (TeamMember member : page.getRecords()) {
                TeamMemberApplyVO vo = TeamMemberApplyVO.ObjToVO(member);

                if (teamMap.containsKey(member.getTeamId())) {
                    vo.setTeamName(teamMap.get(member.getTeamId()).get(0).getTeamName());
                }

                vo.setUserName(loginUser.getUserName());
                vo.setUserAvatar(loginUser.getUserAvatar());

                voList.add(vo);
            }
        }

        return buildPage(page, voList);

    }

    /**
     * 邀请成员
     *
     * @param teamInviteAddRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean inviteMember(TeamInviteAddRequest teamInviteAddRequest, User loginUser) {
        ThrowUtils.throwIf(teamInviteAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //邀请加入团队
        Long teamId = teamInviteAddRequest.getTeamId();
        Team team = teamService.getById(teamId);
        ThrowUtils.throwIf(team==null,ErrorCode.PARAMS_ERROR);
        //团队负责人才可邀请
        if (!team.getOwnerUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有团队负责人可以邀请成员！");
        }

        //邀请谁
        Long targetUserId = teamInviteAddRequest.getTargetUserId();
        User targetUser = userService.getById(targetUserId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.PARAMS_ERROR, "指定用户不存在！");

        TeamMember member = this.lambdaQuery()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, targetUserId)
                .one();
        if (member != null) {
            String memberRole = member.getRole();

            if (TeamMemberStatusEnum.ACTIVE.getValue().equals(member.getStatus())) {
                if (TeamMemberRoleEnum.MENTOR.getValue().equals(memberRole)) {
                    ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "该用户已经是监督者！");
                } else {
                    ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "该用户已在团队中");
                }
            }

            if (TeamMemberStatusEnum.PENDING.getValue().equals(member.getStatus())) {
                if (TeamMemberRoleEnum.MENTOR.getValue().equals(memberRole)) {
                    ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "该用户已经申请监督者，请审批！");
                } else {
                    ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "该用户已有待处理的申请或邀请");
                }
            }

            boolean updated = this.lambdaUpdate()
                    .eq(TeamMember::getId, member.getId())
                    .in(TeamMember::getStatus, Arrays.asList(
                            TeamMemberStatusEnum.REJECTED.getValue(),
                            TeamMemberStatusEnum.LEFT.getValue(),
                            TeamMemberStatusEnum.REMOVED.getValue(),
                            TeamMemberStatusEnum.CANCELLED.getValue()))
                    .set(TeamMember::getStatus, TeamMemberStatusEnum.PENDING.getValue())
                    .set(TeamMember::getRole, teamInviteAddRequest.getRole())
                    .set(TeamMember::getJoinType, TeamJoinTypeEnum.INVITE.getValue())
                    .set(TeamMember::getInviterUserId, loginUser.getId())
                    .set(TeamMember::getApplyReason, teamInviteAddRequest.getMessage())
                    .set(TeamMember::getJoinTime, null)
                    .set(TeamMember::getReviewerUserId, null)
                    .set(TeamMember::getReviewTime, null)
                    .set(TeamMember::getReviewRemark, null)
                    .set(TeamMember::getLeaveTime, null)
                    .set(TeamMember::getLeaveReason, null)
                    .set(TeamMember::getLeaveUserId, null)
                    .update();
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "邀请失败，请稍后重试");
            return true;
        }

        TeamMember newMember = new TeamMember();
        newMember.setTeamId(team.getId());
        newMember.setUserId(targetUserId);
        newMember.setRole(teamInviteAddRequest.getRole());
        newMember.setStatus(TeamMemberStatusEnum.PENDING.getValue());
        newMember.setJoinType(TeamJoinTypeEnum.INVITE.getValue());
        newMember.setInviterUserId(loginUser.getId());
        newMember.setApplyReason(teamInviteAddRequest.getMessage());

        try {
            ThrowUtils.throwIf(!this.save(newMember), ErrorCode.OPERATION_ERROR, "邀请失败，请稍后重试");
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复邀请");
        }
        return true;


    }

    /**
     * 成员处理邀请
     *
     * @param teamInviteHandleRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean handleInvite(TeamInviteHandleRequest teamInviteHandleRequest, User loginUser) {
        ThrowUtils.throwIf(teamInviteHandleRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        Long memberId = teamInviteHandleRequest.getMemberId();
        TeamMember teamMember = this.getById(memberId);
        ThrowUtils.throwIf(teamMember==null,ErrorCode.PARAMS_ERROR,"记录不存在！");

        //必须是邀请记录
        ThrowUtils.throwIf(!TeamJoinTypeEnum.INVITE.getValue().equals(teamMember.getJoinType()), ErrorCode.OPERATION_ERROR, "该记录不是邀请");
        // 只有被邀请人自己能处理
        ThrowUtils.throwIf(!teamMember.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "只能处理发给自己的邀请");
        // 只能处理一次
        ThrowUtils.throwIf(!TeamMemberStatusEnum.PENDING.getValue().equals(teamMember.getStatus()), ErrorCode.OPERATION_ERROR, "该邀请已处理");


        Boolean accepted = teamInviteHandleRequest.getAccepted();
        String remark = teamInviteHandleRequest.getRemark();

        Date now = new Date();
        String finalStatus;
        Date finalJoinTime;

        if (accepted) {
            // 接受前再确认团队仍可加入
            Team team = teamService.getById(teamMember.getTeamId());
            ThrowUtils.throwIf(team == null, ErrorCode.NOT_FOUND_ERROR, "团队不存在");
            ThrowUtils.throwIf(!TeamStatusEnum.ACTIVE.getValue().equals(team.getStatus()), ErrorCode.OPERATION_ERROR, "团队已解散，无法加入");
            finalStatus = TeamMemberStatusEnum.ACTIVE.getValue();
            finalJoinTime = now;
        } else {
            finalStatus = TeamMemberStatusEnum.REJECTED.getValue();
            finalJoinTime = null;
        }

        boolean updated = this.lambdaUpdate()
                .eq(TeamMember::getId, teamMember.getId())
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.PENDING.getValue())
                .set(TeamMember::getStatus, finalStatus)
                .set(TeamMember::getReviewerUserId, loginUser.getId())
                .set(TeamMember::getReviewTime, now)
                .set(TeamMember::getReviewRemark, remark)
                .set(TeamMember::getJoinTime, finalJoinTime)
                .update();
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "邀请已被处理，请刷新后重试");
        return true;
    }

    /**
     * 我收到的邀请列表
     *
     * @param teamQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<TeamInviteVO> listMyInviteByPage(TeamQueryRequest teamQueryRequest, User loginUser) {
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);

        String status = teamQueryRequest.getStatus();
        if (StrUtil.isBlank(status)) {
            status = TeamMemberStatusEnum.PENDING.getValue();
        }

        QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("joinType", TeamJoinTypeEnum.INVITE.getValue());
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("createTime");

        Page<TeamMember> page = this.page(new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize()), queryWrapper);

        List<TeamInviteVO> voList = new ArrayList<>();

        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<Long> teamIds = new ArrayList<>();
            List<Long> inviterIds = new ArrayList<>();
            for (TeamMember member : page.getRecords()) {
                teamIds.add(member.getTeamId());
                if (member.getInviterUserId() != null) {
                    inviterIds.add(member.getInviterUserId());
                }
            }

            Map<Long, List<Team>> teamMap = teamService
                    .listByIds(teamIds)
                    .stream()
                    .collect(Collectors.groupingBy(Team::getId));

            Map<Long, List<User>> userMap;
            if (inviterIds.isEmpty()) {
                userMap = new HashMap<>();
            } else {
                List<User> users = userService.listByIds(inviterIds);
                userMap = users.stream().collect(Collectors.groupingBy(User::getId));
            }

            for (TeamMember member : page.getRecords()) {
                TeamInviteVO vo = TeamInviteVO.ObjToVO(member);

                if (teamMap.containsKey(member.getTeamId())) {
                    vo.setTeamName(teamMap.get(member.getTeamId()).get(0).getTeamName());
                }
                if (member.getInviterUserId() != null && userMap.containsKey(member.getInviterUserId())) {
                    User inviter = userMap.get(member.getInviterUserId()).get(0);
                    vo.setInviterName(inviter.getUserName());
                    vo.setInviterAvatar(inviter.getUserAvatar());
                }
                voList.add(vo);
            }
        }

        return buildPage(page, voList);
    }

    /**
     * 我发出的邀请
     * @param teamQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<TeamInviteSentVO> listMySentInviteByPage(TeamQueryRequest teamQueryRequest, User loginUser) {
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);

        QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
        // 谁发出的邀请
        queryWrapper.eq("inviterUserId", loginUser.getId());
        queryWrapper.eq("joinType", TeamJoinTypeEnum.INVITE.getValue());
        queryWrapper.eq(StrUtil.isNotBlank(teamQueryRequest.getStatus()), "status", teamQueryRequest.getStatus());
        queryWrapper.orderByDesc("createTime");

        Page<TeamMember> page = this.page(new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize()), queryWrapper);

        List<TeamInviteSentVO> voList = new ArrayList<>();

        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<Long> teamIds = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            for (TeamMember member : page.getRecords()) {
                teamIds.add(member.getTeamId());
                userIds.add(member.getUserId());
            }

            Map<Long, List<Team>> teamMap = teamService
                    .listByIds(teamIds)
                    .stream()
                    .collect(Collectors.groupingBy(Team::getId));
            Map<Long, List<User>> userMap = userService
                    .listByIds(userIds)
                    .stream()
                    .collect(Collectors.groupingBy(User::getId));

            for (TeamMember member : page.getRecords()) {
                TeamInviteSentVO vo = TeamInviteSentVO.ObjToVO(member);

                if (teamMap.containsKey(member.getTeamId())) {
                    vo.setTeamName(teamMap.get(member.getTeamId()).get(0).getTeamName());
                }
                if (userMap.containsKey(member.getUserId())) {
                    User invitee = userMap.get(member.getUserId()).get(0);
                    vo.setUserName(invitee.getUserName());
                    vo.setUserAvatar(invitee.getUserAvatar());
                }
                voList.add(vo);
            }
        }

        return buildPage(page, voList);


    }

    /**
     * 退出团队
     * @param teamMemberLeaveRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean leaveTeam(TeamMemberLeaveRequest teamMemberLeaveRequest, User loginUser) {
        //校验
        ThrowUtils.throwIf(teamMemberLeaveRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //是否是该团队
        Long teamId = teamMemberLeaveRequest.getTeamId();
        TeamMember member = this.lambdaQuery()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, loginUser.getId())
                .one();
        ThrowUtils.throwIf(member == null, ErrorCode.NOT_FOUND_ERROR, "你不是该团队成员");

        //正常状态才可退出
        ThrowUtils.throwIf(!TeamMemberStatusEnum.ACTIVE.getValue().equals(member.getStatus()), ErrorCode.OPERATION_ERROR, "当前状态不可退出");

        //创建人不可退出，只可解散团队
        ThrowUtils.throwIf(TeamMemberRoleEnum.OWNER.getValue().equals(member.getRole()), ErrorCode.NO_AUTH_ERROR, "团队负责人不能主动退出，只可解散团队");

        Date now = new Date();
        boolean updated = this.lambdaUpdate()
                .eq(TeamMember::getId, member.getId())
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.ACTIVE.getValue())
                .set(TeamMember::getStatus, TeamMemberStatusEnum.LEFT.getValue())
                .set(TeamMember::getLeaveTime, now)
                .set(TeamMember::getLeaveUserId, loginUser.getId())
                .update();

        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "退出失败，请刷新后重试");
        return true;
    }

    /**
     * 授予监督者
     *
     * @param teamMemberGrantRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminGrantMentor(TeamMemberGrantRequest teamMemberGrantRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(teamMemberGrantRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //校验团队
        Long teamId = teamMemberGrantRequest.getTeamId();
        Team team = teamService.getById(teamId);
        ThrowUtils.throwIf(team == null, ErrorCode.NOT_FOUND_ERROR, "团队不存在");
        ThrowUtils.throwIf(!TeamStatusEnum.ACTIVE.getValue().equals(team.getStatus()),ErrorCode.PARAMS_ERROR,"团队未开放");

        //查目标用户
        Long targetUserId = teamMemberGrantRequest.getTargetUserId();
        User targetUser = userService.getById(targetUserId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");

        //查是否已是团队成员
        TeamMember existMember = this.lambdaQuery()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, targetUserId)
                .one();

        if (existMember != null) {
            //是团队成员
            if (TeamMemberStatusEnum.ACTIVE.getValue().equals(existMember.getStatus())) {
                ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "该用户已是团队成员或监督者，不能直接授予");
            }
            //在待审核
            if (TeamMemberStatusEnum.PENDING.getValue().equals(existMember.getStatus())) {
                ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "该用户已有待处理的申请或邀请");
            }

            boolean updated = this.lambdaUpdate()
                    .eq(TeamMember::getId, existMember.getId())
                    .in(TeamMember::getStatus, Arrays.asList(
                            TeamMemberStatusEnum.REJECTED.getValue(),
                            TeamMemberStatusEnum.LEFT.getValue(),
                            TeamMemberStatusEnum.REMOVED.getValue(),
                            TeamMemberStatusEnum.CANCELLED.getValue()))
                    .set(TeamMember::getStatus, TeamMemberStatusEnum.ACTIVE.getValue())
                    .set(TeamMember::getRole, TeamMemberRoleEnum.MENTOR.getValue())
                    .set(TeamMember::getJoinType, TeamJoinTypeEnum.ADMIN.getValue())
                    .set(TeamMember::getInviterUserId, loginUser.getId())
                    .set(TeamMember::getJoinTime, new Date())
                    // 清空历史退出信息
                    .set(TeamMember::getLeaveTime, null)
                    .set(TeamMember::getLeaveReason, null)
                    .set(TeamMember::getLeaveUserId, null)
                    .update();
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "授予失败，请稍后重试");
            return true;
        }

        TeamMember mentor = new TeamMember();
        mentor.setTeamId(teamId);
        mentor.setUserId(targetUserId);
        mentor.setRole(TeamMemberRoleEnum.MENTOR.getValue());
        mentor.setStatus(TeamMemberStatusEnum.ACTIVE.getValue());
        mentor.setJoinType(TeamJoinTypeEnum.ADMIN.getValue());
        mentor.setInviterUserId(loginUser.getId());
        mentor.setJoinTime(new Date());

        try {
            ThrowUtils.throwIf(!this.save(mentor), ErrorCode.OPERATION_ERROR, "授予失败");
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作冲突，请刷新后重试");
        }
        return true;

    }

    /**
     * 移除成员
     *
     * @param teamMemberRemoveRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeMember(TeamMemberRemoveRequest teamMemberRemoveRequest, User loginUser) {
        //校验
        ThrowUtils.throwIf(teamMemberRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //查团队
        Long teamId = teamMemberRemoveRequest.getTeamId();
        Team team = teamService.getById(teamId);
        ThrowUtils.throwIf(team == null, ErrorCode.NOT_FOUND_ERROR, "团队不存在");

        //查记录
        Long memberId = teamMemberRemoveRequest.getMemberId();
        TeamMember target = this.getById(memberId);
        ThrowUtils.throwIf(target == null, ErrorCode.NOT_FOUND_ERROR, "目标成员不存在");
        ThrowUtils.throwIf(!target.getTeamId().equals(teamId), ErrorCode.PARAMS_ERROR, "该成员不属于此团队");
        ThrowUtils.throwIf(!TeamMemberStatusEnum.ACTIVE.getValue().equals(target.getStatus()), ErrorCode.OPERATION_ERROR, "该成员不在团队中");

        //操作人身份是否是管理员
        boolean isAdmin = userService.isAdmin(loginUser);

        TeamMember myself = this.lambdaQuery()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, loginUser.getId())
                .eq(TeamMember::getStatus, "active")
                .one();
        String myRole=null;
        if(myself!=null){
            myRole=myself.getRole();
        }
        if(TeamMemberRoleEnum.OWNER.getValue().equals(myRole)||isAdmin){
            ThrowUtils.throwIf(TeamMemberRoleEnum.OWNER.getValue().equals(target.getRole()), ErrorCode.NO_AUTH_ERROR, "不能移除团队负责人，如负责人违规，直接解散队伍！");
        }else if(TeamMemberRoleEnum.MENTOR.getValue().equals(myRole)){
            ThrowUtils.throwIf(TeamMemberRoleEnum.OWNER.getValue().equals(target.getRole()), ErrorCode.NO_AUTH_ERROR, "不能移除团队负责人");
            ThrowUtils.throwIf(TeamMemberRoleEnum.MENTOR.getValue().equals(target.getRole()), ErrorCode.NO_AUTH_ERROR, "监督者不能移除其他监督者");
        }else {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您没有移除成员的权限");
        }
        boolean updated = this.lambdaUpdate()
                .eq(TeamMember::getId, teamMemberRemoveRequest.getMemberId())
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.ACTIVE.getValue())
                .set(TeamMember::getStatus, TeamMemberStatusEnum.REMOVED.getValue())
                .set(TeamMember::getLeaveTime, new Date())
                .set(TeamMember::getLeaveUserId, loginUser.getId())
                .update();

        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "移除失败，请刷新后重试");

        studytimeService.lambdaUpdate()
                .eq(Studytime::getTeamId, teamId)
                .eq(Studytime::getUserId, target.getUserId())
                .eq(Studytime::getStatus, "running")
                .set(Studytime::getStatus, "expired")
                .set(Studytime::getEndTime, new Date())
                .update();

        return true;
    }

    /**
     * 成员列表
     * @param teamPageQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<TeamMemberVO> listTeamMembers(TeamPageQueryRequest teamPageQueryRequest, User loginUser) {
        //校验
        ThrowUtils.throwIf(teamPageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //获取查询请求
        Long teamId = teamPageQueryRequest.getTeamId();
        int current = teamPageQueryRequest.getCurrent();
        int pageSize = teamPageQueryRequest.getPageSize();

        //是否是管理员
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        //是否是团队成员
        TeamMember myself = this.lambdaQuery()
                .eq(TeamMember::getUserId, loginUser.getId())
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, "active")
                .one();
        ThrowUtils.throwIf(!isAdmin && myself == null, ErrorCode.NO_AUTH_ERROR);

        // 再判断是不是 worker
        boolean isWorker=false;
        if(!isAdmin&&myself!=null) {
             isWorker= "worker".equals(myself.getRole());
        }

        //查当前页成员记录
        Page<TeamMember> memberPage = this.lambdaQuery()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, "active")
                .page(new Page<>(current, pageSize));

        //遍历当前成员赋值
        List<TeamMember> members = memberPage.getRecords();
        List<TeamMemberVO> voList = new ArrayList<>();

        // 当前页有成员才做批量查询，否则直接返回空分页
        if (!members.isEmpty()) {
            //批量查用户，按 id 分组
            Set<Long> userIds = members
                    .stream()
                    .map(TeamMember::getUserId)
                    .collect(Collectors.toSet());

            //按userId分用户组
            Map<Long, List<User>> userMap = userService
                    .listByIds(userIds)
                    .stream()
                    .collect(Collectors.groupingBy(User::getId));

            //批量查任务，按 userId 分组
            Map<Long, List<Task>> taskMap;
            //是普通成员，不查
            if (isWorker) {
                taskMap = Collections.emptyMap();
            } else {
                taskMap = taskService.lambdaQuery()
                        .eq(Task::getTeamId, teamId)
                        .in(Task::getUserId, userIds)
                        .list()
                        .stream()
                        .collect(Collectors.groupingBy(Task::getUserId));
            }

            //循环内赋值从内存查不查数据库快
            for (TeamMember member : members) {
                Long userId = member.getUserId();

                TeamMemberVO vo = new TeamMemberVO();
                vo.setId(member.getId());
                vo.setUserId(userId);
                vo.setRole(member.getRole());

                // 用户信息
                if (userMap.containsKey(userId)) {
                    User user = userMap.get(userId).get(0);
                    vo.setUserName(user.getUserName());
                    vo.setUserAvatar(user.getUserAvatar());
                }

                // 不是普通成员进行任务统计
                if (!isWorker) {
                    List<Task> records = taskMap.getOrDefault(userId, Collections.emptyList());
                    int total = 0;
                    for (Task record : records) {
                        if (record.getDuration() != null) {
                            total += record.getDuration();
                        }
                    }
                    vo.setCheckinCount(records.size());
                    vo.setTotalMinutes(total);
                }
                voList.add(vo);
            }
        }

        //分页转换
        return buildPage(memberPage, voList);
    }


    /**
     * 分页转换
     * @param sourcePage
     * @param records
     * @return
     * @param <T>
     */
    private <T> Page<T> buildPage(Page<?> sourcePage, List<T> records) {
        //新建一个 VO 的分页对象，直接继承原分页的页码、条数、总数
        Page<T> voPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());

        //如果传进来的列表是 null，就塞一个空数组，防止前端收到 null 报错
        if(records==null){
            records=new ArrayList<>();
        }
        voPage.setRecords(records);
        return voPage;
    }



}




