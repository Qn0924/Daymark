package com.nan.daymarkbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nan.daymarkbackend.annotation.AuthCheck;
import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.constant.UserConstant;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.task.TaskDeleteRequest;
import com.nan.daymarkbackend.model.dto.team.*;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.vo.TeamInviteSentVO;
import com.nan.daymarkbackend.model.vo.TeamInviteVO;
import com.nan.daymarkbackend.model.vo.TeamMemberApplyVO;
import com.nan.daymarkbackend.service.TeamMemberService;
import com.nan.daymarkbackend.service.TeamService;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 团队接口
 */
@RestController
@RequestMapping("/team")
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamMemberService teamMemberService;

    @Resource
    private TeamService teamService;

    /**
     * 申请加入团队
     *
     * @param teamJoinApplyRequest     申请请求
     * @param httpRequest HTTP 请求
     * @return 是否申请成功
     */
    @PostMapping("/join/apply")
    public BaseResponse<Boolean> applyJoin(@RequestBody TeamJoinApplyRequest teamJoinApplyRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamJoinApplyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.applyJoin(teamJoinApplyRequest, loginUser));
    }

    /**
     * 审核入团申请
     *
     * @param teamJoinAuditRequest 审核请求
     * @param httpRequest          HTTP 请求
     * @return 是否审核成功
     */
    @PostMapping("/join/audit")
    public BaseResponse<Boolean> auditJoin(@RequestBody TeamJoinAuditRequest teamJoinAuditRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamJoinAuditRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.auditJoin(teamJoinAuditRequest, loginUser));
    }

    /**
     * 审批列表
     * @param teamApplyQueryRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/join/list/page")
    public BaseResponse<Page<TeamMemberApplyVO>> listAudiByPage(@RequestBody TeamQueryRequest teamApplyQueryRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamApplyQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.listAuditByPage(teamApplyQueryRequest, loginUser));
    }

    /**
     * 我的申请入团列表
     * @param teamApplyQueryRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/join/my/list/page")
    public BaseResponse<Page<TeamMemberApplyVO>> listMyApplyByPage(@RequestBody TeamQueryRequest teamApplyQueryRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamApplyQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.listMyApplyByPage(teamApplyQueryRequest, loginUser));
    }

    /**
     * 负责人发起邀请
     *
     * @param teamInviteAddRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/invite/add")
    public BaseResponse<Boolean> inviteMember(@RequestBody TeamInviteAddRequest teamInviteAddRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamInviteAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.inviteMember(teamInviteAddRequest, loginUser));
    }

    /**
     * 被邀请人接受或拒绝
     *
     * @param teamInviteHandleRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/invite/handle")
    public BaseResponse<Boolean> handleInvite(@RequestBody TeamInviteHandleRequest teamInviteHandleRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamInviteHandleRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.handleInvite(teamInviteHandleRequest, loginUser));
    }

    /**
     * 我收到的邀请
     *
     * @param request
     * @param httpRequest
     * @return
     */
    @PostMapping("/invite/my/list/page")
    public BaseResponse<Page<TeamInviteVO>> listMyInviteByPage(@RequestBody TeamQueryRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.listMyInviteByPage(request, loginUser));
    }

    /**
     * 我发送的邀请列表
     * @param teamQueryRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/invite/sent/list/page")
    public BaseResponse<Page<TeamInviteSentVO>> listMySentInviteByPage(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.listMySentInviteByPage(teamQueryRequest, loginUser));
    }

    /**
     * 退出团队
     * @param teamMemberLeaveRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/member/leave")
    public BaseResponse<Boolean> leaveTeam(@RequestBody TeamMemberLeaveRequest teamMemberLeaveRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamMemberLeaveRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.leaveTeam(teamMemberLeaveRequest, loginUser));
    }

    /**
     * 修改团队信息
     * @param teamEditRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editTeam(@RequestBody TeamEditRequest teamEditRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamEditRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamService.editTeam(teamEditRequest, loginUser));
    }

    /**
     * 管理员直接授予监督者
     *
     * @param teamMemberGrantRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/member/grant/mentor")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminGrantMentor(@RequestBody TeamMemberGrantRequest teamMemberGrantRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamMemberGrantRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.adminGrantMentor(teamMemberGrantRequest, loginUser));
    }

    /**
     * 移除成员
     *
     * @param teamMemberRemoveRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/member/remove")
    public BaseResponse<Boolean> removeMember(@RequestBody TeamMemberRemoveRequest teamMemberRemoveRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(teamMemberRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(teamMemberService.removeMember(teamMemberRemoveRequest, loginUser));
    }

}
