package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.entity.Studytime;
import com.nan.daymarkbackend.model.entity.Task;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.enums.StudyTimeStatusEnum;
import com.nan.daymarkbackend.model.enums.TaskStatusEnum;
import com.nan.daymarkbackend.model.enums.TeamMemberStatusEnum;
import com.nan.daymarkbackend.model.vo.StudyTimeVO;
import com.nan.daymarkbackend.service.StudytimeService;
import com.nan.daymarkbackend.mapper.StudytimeMapper;
import com.nan.daymarkbackend.service.TaskService;
import com.nan.daymarkbackend.service.TeamMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author 21289
* @description 针对表【studytime(学习计时)】的数据库操作Service实现
* @createDate 2026-11-12 14:42:39
*/
@Service
public class StudytimeServiceImpl extends ServiceImpl<StudytimeMapper, Studytime>
    implements StudytimeService{

    @Resource
    private TaskService taskService;

    @Resource
    private TeamMemberService teamMemberService;

    /**
     * 开始计时
     *
     * @param taskId
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTimeVO startStudy(Long taskId, User loginUser) {
        ThrowUtils.throwIf(taskId == null || taskId <= 0, ErrorCode.PARAMS_ERROR, "任务id不合法");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        //查找要打卡的任务是否正常
        Task task = getTaskDefinition(taskId);
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        ThrowUtils.throwIf(!TaskStatusEnum.OPEN.getValue().equals(task.getTaskStatus()), ErrorCode.OPERATION_ERROR, "任务未开放，无法开始计时");

        //今天必须在任务起止日期内
        Date todayDate = new Date();
        ThrowUtils.throwIf(task.getStartDate() != null && todayDate.before(task.getStartDate()), ErrorCode.OPERATION_ERROR, "任务还未开始");
        ThrowUtils.throwIf(task.getEndDate() != null && todayDate.after(task.getEndDate()), ErrorCode.OPERATION_ERROR, "任务已结束");

        //是否是该团队成员
        Long memberCount = teamMemberService.lambdaQuery()
                .eq(TeamMember::getTeamId, task.getTeamId())
                .eq(TeamMember::getUserId, loginUser.getId())
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.ACTIVE.getValue())
                .count();
        ThrowUtils.throwIf(memberCount == null || memberCount <= 0, ErrorCode.NO_AUTH_ERROR, "你不是该打卡团成员，无法计时");

        //只能有一个计时
        Studytime running = this.lambdaQuery()
                .eq(Studytime::getUserId, loginUser.getId())
                .eq(Studytime::getStatus, StudyTimeStatusEnum.RUNNING.getValue())
                .one();
        ThrowUtils.throwIf(running != null, ErrorCode.OPERATION_ERROR, "请先结束当前学习");

        // 4. 插入开始记录
        Studytime studyTime = new Studytime();
        studyTime.setUserId(loginUser.getId());
        studyTime.setTaskId(taskId);
        studyTime.setTeamId(task.getTeamId());
        studyTime.setStartTime(new Date());
        studyTime.setStatus(StudyTimeStatusEnum.RUNNING.getValue());
        ThrowUtils.throwIf(!this.save(studyTime), ErrorCode.OPERATION_ERROR, "开始计时失败");

        return toVO(studyTime);

    }

    /**
     * 查找任务定义行
     *
     * @param taskId
     * @return
     */
    private Task getTaskDefinition(Long taskId) {
        return taskService.lambdaQuery()
                .eq(Task::getTaskId, taskId)
                .isNull(Task::getUserId)
                .one();
    }

    /**
     * 转换
     * @param studyTime
     * @return
     */
    private StudyTimeVO toVO(Studytime studyTime) {
        StudyTimeVO studyTimeVO = new StudyTimeVO();
        BeanUtil.copyProperties(studyTime,studyTimeVO);
        return studyTimeVO;
    }


}




