package com.nan.daymarkbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.dto.team.TeamEditRequest;
import com.nan.daymarkbackend.model.entity.Team;
import com.nan.daymarkbackend.model.entity.User;

/**
* @author 21289
* @description 针对表【team(团队)】的数据库操作Service
* @createDate 2026-11-08 17:20:47
*/
public interface TeamService extends IService<Team> {

    /**
     * 修改团队信息
     * @param teamEditRequest
     * @param loginUser
     * @return
     */
    Boolean editTeam(TeamEditRequest teamEditRequest, User loginUser);
}
