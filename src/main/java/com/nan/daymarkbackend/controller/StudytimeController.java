package com.nan.daymarkbackend.controller;

import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.studytime.StudyFinishRequest;
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
     * @param taskId
     * @param httpRequest
     * @return
     */
    @PostMapping("/start")
    public BaseResponse<StudyTimeVO> start(Long taskId, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(studytimeService.startStudy(taskId, loginUser));
    }

    /**
     * 结束计时
     * @param studyTimeId
     * @param httpRequest
     * @return
     */
    @PostMapping("/end")
    public BaseResponse<StudyTimeVO> end(Long studyTimeId, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(studyTimeId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(studytimeService.endStudy(studyTimeId, loginUser));
    }

    /**
     * 计时心跳
     */
    @PostMapping("/heartbeat")
    public BaseResponse<StudyTimeVO> heartbeat(Long studyTimeId, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(studyTimeId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(studytimeService.heartbeat(studyTimeId, loginUser));
    }


}