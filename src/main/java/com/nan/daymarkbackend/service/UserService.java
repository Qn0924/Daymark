package com.nan.daymarkbackend.service;

import com.nan.daymarkbackend.model.dto.UserEditRequest;
import com.nan.daymarkbackend.model.dto.UserUpdateRequest;
import com.nan.daymarkbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 21289
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-11-06 14:28:18
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获得脱敏后的用户登录信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 修改个人信息
     * @param userUpdateRequest
     * @param loginUser
     * @return
     */
    Long updateUser(UserUpdateRequest userUpdateRequest, User loginUser);

    /**
     * 编辑个人信息
     * @param userEditRequest
     * @param loginUser
     * @return
     */
    Long editUser(UserEditRequest userEditRequest, User loginUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

}
