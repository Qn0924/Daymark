package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.task.TaskAddRequest;
import com.nan.daymarkbackend.model.entity.TaskRecord;
import com.nan.daymarkbackend.model.entity.Team;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.TaskRecordService;
import com.nan.daymarkbackend.mapper.TaskRecordMapper;
import com.nan.daymarkbackend.service.TeamMemberService;
import com.nan.daymarkbackend.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author 21289
* @description 针对表【task_record(任务与打卡合并宽表)】的数据库操作Service实现
* @createDate 2026-11-08 17:08:34
*/
@Service
public class TaskRecordServiceImpl extends ServiceImpl<TaskRecordMapper, TaskRecord>
    implements TaskRecordService{

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
        ThrowUtils.throwIf(description.length() > 512, ErrorCode.PARAMS_ERROR, "任务说明不能超过 512 个字符");

        Integer requireDuration = taskAddRequest.getRequireDuration();
        ThrowUtils.throwIf(requireDuration == null || requireDuration <= 0 || requireDuration > 480, ErrorCode.PARAMS_ERROR, "要求时长必须在 1~480 分钟之间");
        ThrowUtils.throwIf(taskAddRequest.getRequirePhoto() != null && taskAddRequest.getRequirePhoto() != 0 && taskAddRequest.getRequirePhoto() != 1, ErrorCode.PARAMS_ERROR, "是否需要照片只能为 0 或 1");

        Date startDate = taskAddRequest.getStartDate();
        Date endDate = taskAddRequest.getEndDate();
        ThrowUtils.throwIf(startDate == null || endDate == null, ErrorCode.PARAMS_ERROR, "任务起止时间不能为空");
        ThrowUtils.throwIf(endDate.before(startDate), ErrorCode.PARAMS_ERROR, "任务结束日期不能早于开始日期");

        //创建打卡团队
        Team team = new Team();
        team.setTeamName(taskName + "打卡团");
        team.setDescription(description);
        team.setOwnerUserId(loginUser.getId());
        team.setStatus("pending");
        ThrowUtils.throwIf(!teamService.save(team), ErrorCode.OPERATION_ERROR, "创建打卡团失败");

        //创建人自动为团长（团成员）
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(loginUser.getId());
        teamMember.setRole("owner");
        teamMember.setStatus("active");
        teamMember.setJoinType("create");
        teamMember.setJoinTime(new Date());
        ThrowUtils.throwIf(!teamMemberService.save(teamMember), ErrorCode.OPERATION_ERROR, "创建团长失败");

        //创建任务
        Long taskId = IdWorker.getId();
        TaskRecord taskRecord = new TaskRecord();
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
        taskRecord.setTaskStatus("pending");
        taskRecord.setTaskCreateTime(new Date());
        taskRecord.setRecordStatus("normal");
        taskRecord.setPoints(0);
        ThrowUtils.throwIf(!this.save(taskRecord), ErrorCode.OPERATION_ERROR, "创建任务失败");

        return taskRecord.getTaskId();

    }
}




