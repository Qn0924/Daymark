package com.nan.daymarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.mapper.TeamMapper;
import com.nan.daymarkbackend.model.dto.team.TeamEditRequest;
import com.nan.daymarkbackend.model.entity.Team;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.TeamService;
import org.springframework.stereotype.Service;

/**
* @author 21289
* @description 针对表【team(团队)】的数据库操作Service实现
* @createDate 2026-11-08 17:20:47
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{


    /**
     * 修改团队信息
     * @param teamEditRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean editTeam(TeamEditRequest teamEditRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(teamEditRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //团队是否存在
        Team oldTeam = this.getById(teamEditRequest.getId());
        ThrowUtils.throwIf(oldTeam == null, ErrorCode.NOT_FOUND_ERROR, "团队不存在");

        //团队负责人才可以修改
        ThrowUtils.throwIf(!oldTeam.getOwnerUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR, "只有团队负责人可以修改团队信息");

        //更新
        Team updateTeam = new Team();
        updateTeam.setId(oldTeam.getId());
        updateTeam.setTeamName(teamEditRequest.getTeamName());
        updateTeam.setDescription(teamEditRequest.getDescription());
        boolean result = this.updateById(updateTeam);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "修改失败，请稍后重试");
        return true;
    }
}




