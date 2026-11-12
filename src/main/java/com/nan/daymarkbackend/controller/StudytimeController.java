package com.nan.daymarkbackend.controller;

import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.studytime.StudyStartRequest;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.vo.StudyTimeVO;
import com.nan.daymarkbackend.service.StudytimeService;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/studytime")
public class StudytimeController {

    @Resource
    private UserService userService;

    @Resource
    private StudytimeService studytimeService;

    /**
     * 开始计时
     * @param studyStartRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/start")
    public BaseResponse<StudyTimeVO> start(@RequestBody StudyStartRequest studyStartRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(studyStartRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(studytimeService.startStudy(studyStartRequest.getTaskId(), loginUser));
    }


}