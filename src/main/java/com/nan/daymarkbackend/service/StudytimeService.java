package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.entity.Studytime;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.vo.StudyTimeVO;

/**
* @author 21289
* @description 针对表【studytime(学习计时)】的数据库操作Service
* @createDate 2026-11-12 14:42:39
*/
public interface StudytimeService extends IService<Studytime> {

    /**
     * 开始计时
     *
     * @param taskId
     * @param loginUser
     * @return
     */
    StudyTimeVO startStudy(Long taskId, User loginUser);
}
