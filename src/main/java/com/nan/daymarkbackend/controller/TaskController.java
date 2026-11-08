package com.nan.daymarkbackend.controller;

import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.task.TaskAddRequest;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.TaskService;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private UserService userService;

    @Resource
    private TaskService taskRecordService;

    /**
     * 添加任务
     * @param taskAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTask(@RequestBody TaskAddRequest taskAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(taskAddRequest==null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long taskId = taskRecordService.addTask(taskAddRequest, loginUser);
        return ResultUtils.success(taskId);
    }

}
