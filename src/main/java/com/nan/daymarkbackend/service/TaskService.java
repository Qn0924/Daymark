package com.nan.daymarkbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nan.daymarkbackend.model.dto.task.*;
import com.nan.daymarkbackend.model.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;

import java.util.Date;
import java.util.List;

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
     *
     * @param taskResubmitRequest
     * @param loginUser
     * @return
     */
    Boolean resubmitTask(TaskResubmitRequest taskResubmitRequest, User loginUser);

    /**
     * 查询我的创建任务
     * @param taskQueryRequest
     * @param loginUser
     * @return
     */
    Page<Task> listMyTask(TaskQueryRequest taskQueryRequest, User loginUser);

    /**
     * 展示任务
     * @param taskQueryRequest
     * @param loginUser
     * @return
     */
    Page<Task> listTaskByPage(TaskQueryRequest taskQueryRequest,User loginUser);

    /**
     * 正常打卡
     */
    Long checkin(TaskCheckinRequest request, User loginUser);

    /**
     * 补卡
     */
    Long makeup(TaskCheckinRequest request, User loginUser);

    /**
     * 查询当前用户指定日期范围内的打卡记录
     *
     * @param taskCheckinCalendarRequest
     * @param loginUser
     * @return 打卡记录列表，按打卡日期升序
     */
    List<Task> listCheckinCalendar(TaskCheckinCalendarRequest taskCheckinCalendarRequest, User loginUser);


    /**
     * 删除任务，解散团队
     * @param taskDeleteRequest
     * @param loginUser
     * @return
     */
    Boolean deleteTask(TaskDeleteRequest taskDeleteRequest, User loginUser);
}
