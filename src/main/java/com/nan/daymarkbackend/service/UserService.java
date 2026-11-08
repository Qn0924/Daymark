package com.nan.daymarkbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nan.daymarkbackend.model.dto.user.UserEditRequest;
import com.nan.daymarkbackend.model.dto.user.UserPasswordUpdateRequest;
import com.nan.daymarkbackend.model.dto.user.UserQueryRequest;
import com.nan.daymarkbackend.model.dto.user.UserUpdateRequest;
import com.nan.daymarkbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nan.daymarkbackend.model.vo.LoginUserVO;
import com.nan.daymarkbackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 查询参数
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 获取单个脱敏后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 修改密码
     *
     * @param userPasswordUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateUserPassword(UserPasswordUpdateRequest userPasswordUpdateRequest, User loginUser);

}
