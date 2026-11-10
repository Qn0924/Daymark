package com.nan.daymarkbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nan.daymarkbackend.annotation.AuthCheck;
import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.PageRequest;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.constant.UserConstant;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.task.TaskAddRequest;
import com.nan.daymarkbackend.model.dto.task.TaskAuditRequest;
import com.nan.daymarkbackend.model.dto.task.TaskQueryRequest;
import com.nan.daymarkbackend.model.dto.task.TaskResubmitRequest;
import com.nan.daymarkbackend.model.entity.Task;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.enums.TaskStatusEnum;
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
    private TaskService taskService;

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
        Long taskId = taskService.addTask(taskAddRequest, loginUser);
        return ResultUtils.success(taskId);
    }

    /**
     * 审核任务（仅管理员）
     */
    @PostMapping("/audit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> auditTask(@RequestBody TaskAuditRequest taskAuditRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(taskAuditRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(taskService.auditTask(taskAuditRequest, loginUser));
    }

    /**
     * 待审核任务分页列表（仅管理员）
     */
    @PostMapping("/audit/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Task>> listPendingTask(@RequestBody PageRequest pageRequest) {
        ThrowUtils.throwIf(pageRequest == null, ErrorCode.PARAMS_ERROR);
        Page<Task> page = taskService.page(
                new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()),
                new LambdaQueryWrapper<Task>()
                        .isNull(Task::getUserId)
                        .eq(Task::getTaskStatus, TaskStatusEnum.PENDING.getValue())
                        .orderByAsc(Task::getTaskCreateTime));
        return ResultUtils.success(page);
    }

    /**
     * 重新提交任务
     */
    @PostMapping("/resubmit")
    public BaseResponse<Boolean> resubmitTask(@RequestBody TaskResubmitRequest taskResubmitRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(taskResubmitRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(taskService.resubmitTask(taskResubmitRequest, loginUser));
    }

    /**
     * 我创建的任务接口
     * @param taskQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Task>> listMyTask(@RequestBody TaskQueryRequest taskQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(taskQueryRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(taskService.listMyTask(taskQueryRequest, loginUser));
    }

}
