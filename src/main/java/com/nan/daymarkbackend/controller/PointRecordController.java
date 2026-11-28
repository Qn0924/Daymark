package com.nan.daymarkbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nan.daymarkbackend.annotation.AuthCheck;
import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.constant.UserConstant;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.point.PointRecordQueryRequest;
import com.nan.daymarkbackend.model.entity.PointRecord;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.PointRecordService;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/point")
public class PointRecordController {

    @Resource
    private PointRecordService pointRecordService;

    @Resource
    private UserService userService;

    /**
     * 我的积分流水
     * @param pointRecordQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<PointRecord>> listMyPointRecord(@RequestBody PointRecordQueryRequest pointRecordQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pointRecordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(pointRecordService.listMyPointRecord(pointRecordQueryRequest, loginUser));
    }

    /**
     * 所有用户积分流水
     * @param pointRecordQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PointRecord>> listPointRecordByPage(@RequestBody PointRecordQueryRequest pointRecordQueryRequest) {
        ThrowUtils.throwIf(pointRecordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pointRecordService.listPointRecordByPage(pointRecordQueryRequest));
    }
}