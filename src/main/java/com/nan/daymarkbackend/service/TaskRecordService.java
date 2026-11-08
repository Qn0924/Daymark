package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.dto.task.TaskAddRequest;
import com.nan.daymarkbackend.model.entity.TaskRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;

/**
* @author 21289
* @description 针对表【task_record(任务与打卡合并宽表)】的数据库操作Service
* @createDate 2026-11-08 17:08:34
*/
public interface TaskRecordService extends IService<TaskRecord> {

    /**
     * 创建任务
     * @param taskAddRequest
     * @param loginUser
     * @return
     */
    Long AddTask(TaskAddRequest taskAddRequest, User loginUser);

}
