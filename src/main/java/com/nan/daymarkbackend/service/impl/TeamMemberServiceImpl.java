package com.nan.daymarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.model.entity.TeamMember;
import com.nan.daymarkbackend.service.TeamMemberService;
import com.nan.daymarkbackend.mapper.TeamMemberMapper;
import org.springframework.stereotype.Service;

/**
* @author 21289
* @description 针对表【team_member(团队成员)】的数据库操作Service实现
* @createDate 2026-11-08 17:45:38
*/
@Service
public class TeamMemberServiceImpl extends ServiceImpl<TeamMemberMapper, TeamMember>
    implements TeamMemberService{

}




