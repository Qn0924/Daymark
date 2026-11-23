package com.nan.daymarkbackend.controller;

import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.team.TeamJoinApplyRequest;
import com.nan.daymarkbackend.model.dto.team.TeamJoinAuditRequest;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.TeamMemberService;
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

}
