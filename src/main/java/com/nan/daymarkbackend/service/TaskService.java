package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.dto.task.TaskAddRequest;
import com.nan.daymarkbackend.model.dto.task.TaskAuditRequest;
import com.nan.daymarkbackend.model.dto.task.TaskResubmitRequest;
import com.nan.daymarkbackend.model.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;

/**
* @author 21289
* @description 针对表【task_record(任务与打卡合并宽表)】的数据库操作Service
* @createDate 2026-11-08 17:08:34
*/
public interface TaskService extends IService<Task> {

    /**
     * 创建任务
     * @param taskAddRequest
     * @param loginUser
     * @return
     */
    Long addTask(TaskAddRequest taskAddRequest, User loginUser);

    /**
     * 管理员审核任务
     * @param taskAuditRequest
     * @param loginUser
     * @return
     */
    Boolean auditTask(TaskAuditRequest taskAuditRequest, User loginUser);

    /**
     * 重新提交任务
     */
    Boolean resubmitTask(TaskResubmitRequest taskResubmitRequest, User loginUser);

}
