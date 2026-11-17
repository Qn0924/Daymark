package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.exception.BusinessException;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.point.PointRecordQueryRequest;
import com.nan.daymarkbackend.model.entity.PointRecord;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.PointRecordService;
import com.nan.daymarkbackend.mapper.PointRecordMapper;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author 21289
* @description 针对表【point_record(积分流水)】的数据库操作Service实现
* @createDate 2026-11-14 16:35:23
*/
@Service
public class PointRecordServiceImpl extends ServiceImpl<PointRecordMapper, PointRecord>
    implements PointRecordService{

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int normalGivePoints(User user, Long recordId) {
        Date today = DateUtil.beginOfDay(new Date());
        Date lastDay = user.getLastCheckinDate();

        int days = 0;
        if (user.getContinueDays() != null) {
            days = user.getContinueDays();
        }

        int points = 0;
        if (user.getPoints() != null) {
            points = user.getPoints();
        }

        //判断是否断卡
        boolean isBreak = isBreak(user);

        // 断卡扣 10 分，连续天数清零
        if (isBreak && days > 0) {
            int deduct = Math.min(10, points);   // 扣 10，但不超过余额
            points = points - deduct;
            days = 0;                            // 连续天数清零
            if (deduct > 0) {
                addRecord(user.getId(), recordId, -deduct, "break", "断卡扣分");
            }
        }

        //今天是否是第一次打卡
        boolean firstToday = false;
        if (lastDay != null && DateUtil.isSameDay(lastDay, today)) {
            firstToday = true;
        }
        if (!firstToday) {
            days = days + 1;
        }

        //按连续天数发积分
        int add;
        if (days >= 7) {
            add = 20;        // 连续满 7 天及以上
        } else {
            add = 10;        // 连续不足 7 天
        }
        points = points + add;
        addRecord(user.getId(), recordId, add, "checkin", "打卡奖励，连续 " + days + " 天");

        //保存用户
        user.setContinueDays(days);
        user.setLastCheckinDate(today);
        user.setPoints(points);
        userService.updateById(user);

        return add;
    }

    /**
     * 补卡
     * @param user
     * @param recordId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int makeupPoints(User user, Long recordId) {
        int points;
        if(user.getPoints() == null ){
            points=0;
        }else{
            points=user.getPoints();
        }

        if (points < 20) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足 20，无法补卡");
        }
        // 只扣分，不改连续天数
        user.setPoints(points - 20);
        userService.updateById(user);
        addRecord(user.getId(), recordId, -20, "makeup", "补打卡消耗");
        return -20;
    }

    /**
     * 分页查询我的积分流水
     *
     * @param pointRecordQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<PointRecord> listMyPointRecord(PointRecordQueryRequest pointRecordQueryRequest, User loginUser) {
        ThrowUtils.throwIf(pointRecordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        long current = pointRecordQueryRequest.getCurrent();
        long pageSize = pointRecordQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "分页参数不合法");

        LambdaQueryWrapper<PointRecord> queryWrapper = new LambdaQueryWrapper<PointRecord>()
                // 关键：user 一律取登录态，绝不能用前端传的 userId
                .eq(PointRecord::getUserId, loginUser.getId())
                .eq(pointRecordQueryRequest.getRecordId() != null, PointRecord::getRecordId, pointRecordQueryRequest.getRecordId())
                .eq(StrUtil.isNotBlank(pointRecordQueryRequest.getSource()), PointRecord::getSource, pointRecordQueryRequest.getSource())
                .ge(pointRecordQueryRequest.getStartTime() != null, PointRecord::getCreateTime, pointRecordQueryRequest.getStartTime())
                .le(pointRecordQueryRequest.getEndTime() != null, PointRecord::getCreateTime, pointRecordQueryRequest.getEndTime())
                .orderByDesc(PointRecord::getCreateTime)
                .orderByDesc(PointRecord::getId);
        return this.page(new Page<>(current, pageSize), queryWrapper);

    }

    /**
     * 查询所有积分流水
     * @param pointRecordQueryRequest
     * @return
     */
    @Override
    public Page<PointRecord> listPointRecordByPage(PointRecordQueryRequest pointRecordQueryRequest) {
        ThrowUtils.throwIf(pointRecordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pointRecordQueryRequest.getCurrent();
        long pageSize = pointRecordQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "分页参数不合法");

        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<PointRecord>()
                .eq(pointRecordQueryRequest.getUserId() != null, PointRecord::getUserId, pointRecordQueryRequest.getUserId())
                .eq(pointRecordQueryRequest.getRecordId() != null, PointRecord::getRecordId, pointRecordQueryRequest.getRecordId())
                .eq(StrUtil.isNotBlank(pointRecordQueryRequest.getSource()), PointRecord::getSource, pointRecordQueryRequest.getSource())
                .ge(pointRecordQueryRequest.getStartTime() != null, PointRecord::getCreateTime, pointRecordQueryRequest.getStartTime())
                .le(pointRecordQueryRequest.getEndTime() != null, PointRecord::getCreateTime, pointRecordQueryRequest.getEndTime())
                .orderByDesc(PointRecord::getCreateTime)
                .orderByDesc(PointRecord::getId);
        return this.page(new Page<>(current, pageSize), wrapper);
    }

    /**
     * 判断是否断卡
     * @param user
     * @return
     */
    private boolean isBreak(User user) {
        Date lastDay = user.getLastCheckinDate();
        if (lastDay == null) {
            return true;
        }
        Date yesterday = DateUtil.offsetDay(DateUtil.beginOfDay(new Date()), -1);
        return DateUtil.beginOfDay(lastDay).before(yesterday);
    }

    /**
     * 积分流水
     * @param userId
     * @param recordId
     * @param points
     * @param source
     * @param description
     */
    private void addRecord(Long userId, Long recordId, int points, String source, String description) {
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setRecordId(recordId);
        record.setPoints(points);
        record.setSource(source);
        record.setDescription(description);
        this.save(record);
    }
}




