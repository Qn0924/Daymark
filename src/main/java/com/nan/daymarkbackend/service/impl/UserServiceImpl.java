package com.nan.daymarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.UserService;
import com.nan.daymarkbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 21289
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-11-06 14:28:18
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




