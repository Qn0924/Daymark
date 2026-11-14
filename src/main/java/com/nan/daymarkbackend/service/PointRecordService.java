package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.entity.PointRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;

/**
* @author 21289
* @description 针对表【point_record(积分流水)】的数据库操作Service
* @createDate 2026-11-14 16:35:23
*/
public interface PointRecordService extends IService<PointRecord> {

    /**
     * 正常打卡发放积分
     * @param user
     * @param recordId
     * @return
     */
    int normalGivePoints(User user, Long recordId);

    /**
     * 补卡扣积分
     * @param user
     * @param recordId
     * @return
     */
    int makeupPoints(User user, Long recordId);
}
