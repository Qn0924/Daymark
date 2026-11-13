package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.exception.BusinessException;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.task.TaskAddRequest;
import com.nan.daymarkbackend.model.dto.task.TaskAuditRequest;
import com.nan.daymarkbackend.model.dto.task.TaskQueryRequest;
import com.nan.daymarkbackend.model.dto.task.TaskResubmitRequest;
import com.nan.daymarkbackend.model.entity.Task;
import com.nan.daymarkbackend.model.entity.Team;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.enums.*;
import com.nan.daymarkbackend.service.TaskService;
import com.nan.daymarkbackend.mapper.TaskMapper;
import com.nan.daymarkbackend.service.TeamMemberService;
import com.nan.daymarkbackend.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;

/**
* @author 21289
* @description 针对表【task_record(任务与打卡合并宽表)】的数据库操作Service实现
* @createDate 2026-11-08 17:08:34
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService {

    @Resource
    private TeamService teamService;

    @Resource
    private TeamMemberService teamMemberService;

    /**
     * 添加任务
     * @param taskAddRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTask(TaskAddRequest taskAddRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(taskAddRequest == null, ErrorCode.PARAMS_ERROR, "请求数据为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        String taskName = StrUtil.trim(taskAddRequest.getTaskName());
        ThrowUtils.throwIf(StrUtil.isBlank(taskName), ErrorCode.PARAMS_ERROR, "任务名不能为空");
        ThrowUtils.throwIf(taskName.length() > 250, ErrorCode.PARAMS_ERROR, "任务名不能超过 250 个字符");

        String description = taskAddRequest.getDescription();
        ThrowUtils.throwIf(description!=null&&description.length() > 512, ErrorCode.PARAMS_ERROR, "任务说明不能超过 512 个字符");

        Integer requireDuration = taskAddRequest.getRequireDuration();
        ThrowUtils.throwIf(requireDuration == null || requireDuration <= 0 || requireDuration > 480, ErrorCode.PARAMS_ERROR, "要求时长必须在 1~480 分钟之间");
        ThrowUtils.throwIf(taskAddRequest.getRequirePhoto() != null && taskAddRequest.getRequirePhoto() != 0 && taskAddRequest.getRequirePhoto() != 1, ErrorCode.PARAMS_ERROR, "是否需要照片只能为 0 或 1");

        Date startDate = taskAddRequest.getStartDate();
        Date endDate = taskAddRequest.getEndDate();
        ThrowUtils.throwIf(startDate == null || endDate == null, ErrorCode.PARAMS_ERROR, "任务起止时间不能为空");
        ThrowUtils.throwIf(!endDate.after(startDate), ErrorCode.PARAMS_ERROR, "任务结束日期必须晚于开始日期");

        Date tomorrowStart = DateUtil.beginOfDay(DateUtil.tomorrow());
        ThrowUtils.throwIf(endDate.before(tomorrowStart), ErrorCode.PARAMS_ERROR, "任务结束日期不满足要求，无法通过审核");

        //创建打卡团队
        Team team = new Team();
        team.setTeamName(taskName + "打卡团");
        team.setDescription(description);
        team.setOwnerUserId(loginUser.getId());
        team.setStatus(TeamStatusEnum.PENDING.getValue());
        ThrowUtils.throwIf(!teamService.save(team), ErrorCode.OPERATION_ERROR, "创建打卡团失败");

        //创建人自动为团长（团成员）
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(loginUser.getId());
        teamMember.setRole(TeamMemberRoleEnum.OWNER.getValue());
        teamMember.setStatus(TeamMemberStatusEnum.ACTIVE.getValue());
        teamMember.setJoinType(TeamJoinTypeEnum.CREATE.getValue());
        teamMember.setJoinTime(new Date());
        ThrowUtils.throwIf(!teamMemberService.save(teamMember), ErrorCode.OPERATION_ERROR, "创建团长失败");

        //创建任务
        Long taskId = IdWorker.getId();
        Task taskRecord = new Task();
        taskRecord.setTaskId(taskId);
        taskRecord.setTeamId(team.getId());
        taskRecord.setCreatorUserId(loginUser.getId());
        taskRecord.setTaskName(taskName);
        taskRecord.setTaskCategory(taskAddRequest.getTaskCategory());
        taskRecord.setDescription(description);
        taskRecord.setRequireDuration(requireDuration);
        taskRecord.setRequirePhoto(taskAddRequest.getRequirePhoto());
        taskRecord.setStartDate(startDate);
        taskRecord.setEndDate(endDate);
        taskRecord.setTaskStatus(TaskStatusEnum.PENDING.getValue());
        taskRecord.setTaskCreateTime(new Date());
        taskRecord.setRecordStatus("normal");
        taskRecord.setPoints(0);
        ThrowUtils.throwIf(!this.save(taskRecord), ErrorCode.OPERATION_ERROR, "创建任务失败");

        return taskRecord.getTaskId();

    }

    /**
     * 管理员审核任务
     * @param taskAuditRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean auditTask(TaskAuditRequest taskAuditRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(taskAuditRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        String newTaskStatus;
        Boolean approved = taskAuditRequest.getApproved();
        ThrowUtils.throwIf(approved==null,ErrorCode.PARAMS_ERROR,"未给审核结果");

        //查任务定义行
        Task task=this.lambdaQuery()
                .eq(Task::getTaskId,taskAuditRequest.getTaskId())
                .isNull(Task::getUserId)
                .one();
        if(task==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"任务不存在");
        }
        if(!TaskStatusEnum.PENDING.getValue().equals(task.getTaskStatus())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"任务已审核过");
        }

        //操作数据库更新status值
        if(!approved){
            newTaskStatus=TaskStatusEnum.REJECTED.getValue();
        }else {
            Date tomorrowStart = DateUtil.beginOfDay(DateUtil.tomorrow());
            ThrowUtils.throwIf(task.getEndDate() == null || task.getEndDate().before(tomorrowStart), ErrorCode.PARAMS_ERROR, "任务已过期，无法通过，请驳回让创建者修改");
            newTaskStatus = TaskStatusEnum.OPEN.getValue();
        }
        boolean taskResult = this.lambdaUpdate()
                .eq(Task::getTaskId, taskAuditRequest.getTaskId())
                .isNull(Task::getUserId)
                .eq(Task::getTaskStatus, TaskStatusEnum.PENDING.getValue())
                .set(Task::getTaskStatus, newTaskStatus)
                .set(Task::getAuditTime, new Date())
                .update();
        ThrowUtils.throwIf(!taskResult,ErrorCode.OPERATION_ERROR,"数据库操作失败");

        //更新团队status值
        String newTeamStatus;
        if(!approved){
            newTeamStatus=TeamStatusEnum.REJECTED.getValue();
        }else {
            newTeamStatus = TeamStatusEnum.ACTIVE.getValue();
        }

        boolean teamResult = teamService.lambdaUpdate()
                .eq(Team::getId, task.getTeamId())
                .eq(Team::getStatus, TeamStatusEnum.PENDING.getValue())
                .set(Team::getStatus, newTeamStatus)
                .set(Team::getTeamName,task.getTaskName()+"打卡团")
                .update();
        ThrowUtils.throwIf(!teamResult, ErrorCode.OPERATION_ERROR, "打卡团状态异常");

        return true;
    }

    /**
     * 重新提交任务
     * @param taskResubmitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resubmitTask(TaskResubmitRequest taskResubmitRequest, User loginUser) {
        ThrowUtils.throwIf(taskResubmitRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"未登录");
        //校验参数
        String taskName = StrUtil.trim(taskResubmitRequest.getTaskName());
        ThrowUtils.throwIf(StrUtil.isBlank(taskName), ErrorCode.PARAMS_ERROR, "任务名不能为空");
        ThrowUtils.throwIf(taskName.length() > 250, ErrorCode.PARAMS_ERROR, "任务名不能超过 250 个字符");

        String description = taskResubmitRequest.getDescription();
        ThrowUtils.throwIf(description!=null&&description.length() > 512, ErrorCode.PARAMS_ERROR, "任务说明不能超过 512 个字符");

        Integer requireDuration = taskResubmitRequest.getRequireDuration();
        ThrowUtils.throwIf(requireDuration == null || requireDuration <= 0 || requireDuration > 480, ErrorCode.PARAMS_ERROR, "要求时长必须在 1~480 分钟之间");

        Date tomorrowStart = DateUtil.beginOfDay(DateUtil.tomorrow());
        ThrowUtils.throwIf(taskResubmitRequest.getEndDate() == null || taskResubmitRequest.getEndDate().before(tomorrowStart), ErrorCode.PARAMS_ERROR, "任务结束日期不满足要求，无法通过审核");

        //查询查询提交修改的任务
        Task task = this.lambdaQuery()
                .eq(Task::getTaskId, taskResubmitRequest.getTaskId())
                .isNull(Task::getUserId)
                .one();
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");

        ThrowUtils.throwIf(!task.getCreatorUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "只有任务创建者可以重新提交");

        ThrowUtils.throwIf(!TaskStatusEnum.PENDING.getValue().equals(task.getTaskStatus()) && !TaskStatusEnum.REJECTED.getValue().equals(task.getTaskStatus()),
                ErrorCode.OPERATION_ERROR, "当前状态不允许修改任务信息");

        Date endDate = taskResubmitRequest.getEndDate();
        ThrowUtils.throwIf(task.getStartDate() != null && !endDate.after(task.getStartDate()), ErrorCode.PARAMS_ERROR, "结束日期不能早于开始日期");

        //回到待审核
        boolean taskResult = this.lambdaUpdate()
                .eq(Task::getTaskId, taskResubmitRequest.getTaskId())
                .isNull(Task::getUserId)
                .in(Task::getTaskStatus, Arrays.asList(
                        TaskStatusEnum.PENDING.getValue(),
                        TaskStatusEnum.REJECTED.getValue()))
                .set(Task::getTaskName, taskName)
                .set(Task::getDescription, description)
                .set(Task::getRequireDuration, requireDuration)
                .set(Task::getEndDate, endDate)
                .set(Task::getTaskStatus, TaskStatusEnum.PENDING.getValue())
                .set(Task::getAuditTime, null)
                .update();
        ThrowUtils.throwIf(!taskResult, ErrorCode.OPERATION_ERROR, "数据库任务更新失败");

        //团队回到待审核
        boolean teamUpdated = teamService.lambdaUpdate()
                .eq(Team::getId, task.getTeamId())
                .in(Team::getStatus, Arrays.asList(
                        TeamStatusEnum.PENDING.getValue(),
                        TeamStatusEnum.REJECTED.getValue()))
                .set(Team::getStatus, TeamStatusEnum.PENDING.getValue())
                .set(Team::getTeamName,taskName+"打卡团")
                .update();
        ThrowUtils.throwIf(!teamUpdated, ErrorCode.OPERATION_ERROR, "数据库团队更新失败");

        return true;
    }

    /**
     * 查询我创建的任务
     * @param taskQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<Task> listMyTask(TaskQueryRequest taskQueryRequest, User loginUser) {
        ThrowUtils.throwIf(taskQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NOT_LOGIN_ERROR);

        long current = taskQueryRequest.getCurrent();
        long pageSize = taskQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页参数不合法");

        QueryWrapper<Task> queryWrapper = new QueryWrapper<Task>()
                .eq("creatorUserId", loginUser.getId())
                .isNull("userId")
                .eq(StrUtil.isNotBlank(taskQueryRequest.getTaskCategory()), "taskCategory", taskQueryRequest.getTaskCategory())
                .like(StrUtil.isNotBlank(taskQueryRequest.getTaskName()), "taskName", StrUtil.trim(taskQueryRequest.getTaskName()));

        if (StrUtil.isNotBlank(taskQueryRequest.getTaskStatus())) {
            // 用枚举校验，防止传入任意字符串
            ThrowUtils.throwIf(TaskStatusEnum.getEnumByValue(taskQueryRequest.getTaskStatus()) == null, ErrorCode.PARAMS_ERROR, "任务状态不合法");
            queryWrapper.eq("taskStatus", taskQueryRequest.getTaskStatus());
        }

        queryWrapper.orderByDesc("taskCreateTime");
        return this.page(new Page<>(current, pageSize), queryWrapper);
    }

    /**
     * 展示任务
     * @param taskQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<Task> listTaskByPage(TaskQueryRequest taskQueryRequest,User loginUser) {
        ThrowUtils.throwIf(taskQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NOT_LOGIN_ERROR);

        long current = taskQueryRequest.getCurrent();
        long pageSize = taskQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页参数不合法");

        String taskName = StrUtil.trim(taskQueryRequest.getTaskName());
        QueryWrapper<Task> queryWrapper = new QueryWrapper<Task>()
                .isNull("userId")
                .eq("taskStatus", TaskStatusEnum.OPEN.getValue())
                .like(StrUtil.isNotBlank(taskName), "taskName", taskName)
                .orderByDesc("taskCreateTime");

        return this.page(new Page<>(current, pageSize), queryWrapper);
    }
}




