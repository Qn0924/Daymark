package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.dto.team.TeamJoinApplyRequest;
import com.nan.daymarkbackend.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.entity.User;

/**
* @author 21289
* @description 针对表【team(团队)】的数据库操作Service
* @createDate 2026-11-08 17:20:47
*/
public interface TeamService extends IService<Team> {

    /**
     * 申请加入团队
     *
     * @param teamJoinApplyRequest   申请请求
     * @param loginUser 当前登录用户
     * @return 是否申请成功
     */
    Boolean applyJoin(TeamJoinApplyRequest teamJoinApplyRequest, User loginUser);

}
