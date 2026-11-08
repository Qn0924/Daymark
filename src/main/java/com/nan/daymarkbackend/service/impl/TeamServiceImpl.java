package com.nan.daymarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.model.entity.Team;
import com.nan.daymarkbackend.service.TeamService;
import com.nan.daymarkbackend.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 21289
* @description 针对表【team(团队)】的数据库操作Service实现
* @createDate 2026-11-08 17:20:47
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




