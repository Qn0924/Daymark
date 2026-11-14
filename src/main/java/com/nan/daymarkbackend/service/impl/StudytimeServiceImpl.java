package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.constant.StudytimeConstant;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.entity.Studytime;
import com.nan.daymarkbackend.model.entity.Task;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.enums.StudyTimeStatusEnum;
import com.nan.daymarkbackend.model.enums.TaskStatusEnum;
import com.nan.daymarkbackend.model.enums.TeamMemberRoleEnum;
import com.nan.daymarkbackend.model.enums.TeamMemberStatusEnum;
import com.nan.daymarkbackend.model.vo.StudyTimeVO;
import com.nan.daymarkbackend.service.StudytimeService;
import com.nan.daymarkbackend.mapper.StudytimeMapper;
import com.nan.daymarkbackend.service.TaskService;
import com.nan.daymarkbackend.service.TeamMemberService;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author 21289
* @description 针对表【studytime(学习计时)】的数据库操作Service实现
* @createDate 2026-11-13 11:05:35
*/
@Service
public class StudytimeServiceImpl extends ServiceImpl<StudytimeMapper, Studytime>
    implements StudytimeService{

    @Resource
    private TaskService taskService;

    @Resource
    private TeamMemberService teamMemberService;

    @Resource
    private UserService userService;

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

        //同一个用户只能处理一个请求
        userService.lambdaQuery()
                .eq(User::getId, loginUser.getId())
                .last("FOR UPDATE")
                .one();

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
                .in(TeamMember::getRole,
                        TeamMemberRoleEnum.OWNER.getValue(),
                        TeamMemberRoleEnum.WORKER.getValue())
                .count();
        ThrowUtils.throwIf(memberCount == null || memberCount <= 0, ErrorCode.NO_AUTH_ERROR, "你不是该打卡团成员，无法计时");

        // 只能有一个计时：先把关浏览器后残留的 running 结算掉
        this.lambdaQuery()
                .eq(Studytime::getUserId, loginUser.getId())
                .eq(Studytime::getStatus, StudyTimeStatusEnum.RUNNING.getValue())
                .list()
                .forEach(this::autoCloseIfTimeout);
        Long runningCount = this.lambdaQuery()
                .eq(Studytime::getUserId, loginUser.getId())
                .eq(Studytime::getStatus, StudyTimeStatusEnum.RUNNING.getValue())
                .count();
        ThrowUtils.throwIf(runningCount != null && runningCount > 0, ErrorCode.OPERATION_ERROR, "请先结束当前学习");

        // 插入开始记录
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
     * 结束计时
     * @param studyTimeId
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTimeVO endStudy(Long studyTimeId, User loginUser) {
        ThrowUtils.throwIf(studyTimeId == null || studyTimeId <= 0, ErrorCode.PARAMS_ERROR, "计时id不合法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //查要结束计时的任务
        Studytime studyTime = this.getById(studyTimeId);
        ThrowUtils.throwIf(studyTime == null, ErrorCode.NOT_FOUND_ERROR, "计时记录不存在");
        ThrowUtils.throwIf(!studyTime.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作该计时");


        // 心跳中断超2分钟的先自动结算，按最后一次心跳截断时长
        studyTime = autoCloseIfTimeout(studyTime);
        if (!StudyTimeStatusEnum.RUNNING.getValue().equals(studyTime.getStatus())) {
            return toVO(studyTime);
        }

        ThrowUtils.throwIf(!StudyTimeStatusEnum.RUNNING.getValue().equals(studyTime.getStatus()), ErrorCode.OPERATION_ERROR, "计时已结束");

        //计时持续时间
        Date now = new Date();
        long minutes = (now.getTime() - studyTime.getStartTime().getTime()) / 60000;

        //插入
        studyTime.setEndTime(now);
        studyTime.setDuration((int)minutes);
        studyTime.setStatus(StudyTimeStatusEnum.FINISHED.getValue());
        ThrowUtils.throwIf(!this.updateById(studyTime), ErrorCode.OPERATION_ERROR, "结束计时失败");

        return toVO(studyTime);

    }

    /**
     * 计时心跳
     * @param studyTimeId
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTimeVO heartbeat(Long studyTimeId, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(studyTimeId == null || studyTimeId <= 0, ErrorCode.PARAMS_ERROR, "计时id不合法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //获取计时记录
        Studytime studyTime = this.getById(studyTimeId);
        ThrowUtils.throwIf(studyTime == null, ErrorCode.NOT_FOUND_ERROR, "计时记录不存在");
        ThrowUtils.throwIf(!studyTime.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作该计时");

        // 中断超过 2 分钟的，先自动结算
        studyTime = autoCloseIfTimeout(studyTime);
        if (!StudyTimeStatusEnum.RUNNING.getValue().equals(studyTime.getStatus())) {
            return toVO(studyTime);   // 已自动结算，直接把结束态返回给前端
        }

        Date now = new Date();
        boolean updated = this.lambdaUpdate()
                .eq(Studytime::getId, studyTimeId)
                .eq(Studytime::getStatus, StudyTimeStatusEnum.RUNNING.getValue())
                .set(Studytime::getLastHeartbeatTime, now)
                .update();
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "计时已结束");

        studyTime.setLastHeartbeatTime(now);
        return toVO(studyTime);
    }

    /**
     * 根据超时类型自动结束
     * @param studyTime
     * @return
     */
    private Studytime autoCloseIfTimeout(Studytime studyTime) {
        //必须是在计时的
        if (!StudyTimeStatusEnum.RUNNING.getValue().equals(studyTime.getStatus())) {
            return studyTime;
        }

        //赋值上一次心跳时间
        Date now = new Date();
        Date start = studyTime.getStartTime();
        Date heartbeat = studyTime.getLastHeartbeatTime();
        Date lastHeartbeat;
        if (heartbeat == null) {
            lastHeartbeat = start;
        } else {
            lastHeartbeat = heartbeat;
        }

        //心跳断了，浏览器已关闭，按最后一次心跳结束
        if (now.getTime() - lastHeartbeat.getTime() > StudytimeConstant.HEARTBEAT_TIMEOUT_MILLIS) {
            long minutes = (lastHeartbeat.getTime() - start.getTime())/ 60000;
            boolean update = this.lambdaUpdate()
                    .eq(Studytime::getId, studyTime.getId())
                    .eq(Studytime::getStatus, StudyTimeStatusEnum.RUNNING.getValue())
                    .set(Studytime::getEndTime, lastHeartbeat)
                    .set(Studytime::getDuration, (int) minutes)
                    .set(Studytime::getStatus, StudyTimeStatusEnum.FINISHED.getValue())
                    .update();
            //ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"数据库操作失败");
            return this.getById(studyTime.getId());
        }

        //心跳还在但超过 24 小时,作废
        if (now.getTime() - start.getTime() > StudytimeConstant.MAX_RUNNING_MILLIS) {
            this.lambdaUpdate()
                    .eq(Studytime::getId, studyTime.getId())
                    .eq(Studytime::getStatus, StudyTimeStatusEnum.RUNNING.getValue())
                    .set(Studytime::getEndTime, now)
                    .set(Studytime::getDuration, (now.getTime() - start.getTime()) / 60000)
                    .set(Studytime::getStatus, StudyTimeStatusEnum.EXPIRED.getValue())
                    .update();
            return this.getById(studyTime.getId());
        }
        return studyTime;
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
                .last("LIMIT 1")
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




