package com.nan.daymarkbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.annotation.AuthCheck;
import com.nan.daymarkbackend.constant.UserConstant;
import com.nan.daymarkbackend.exception.BusinessException;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.dto.UserEditRequest;
import com.nan.daymarkbackend.model.dto.UserQueryRequest;
import com.nan.daymarkbackend.model.dto.UserUpdateRequest;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.model.enums.UserRoleEnum;
import com.nan.daymarkbackend.model.vo.LoginUserVO;
import com.nan.daymarkbackend.model.vo.UserVO;
import com.nan.daymarkbackend.service.UserService;
import com.nan.daymarkbackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.nan.daymarkbackend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 21289
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-11-06 14:28:18
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //2.检查用户是否和数据库已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //3.密码要加密
        String encryptPassword = getEncryptPassword(userPassword);
        //4.将数据插入到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult;
        try {
            saveResult = this.save(user);
        } catch (Exception e) {
            log.error("注册失败，保存用户异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，系统异常");
        }
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        //2.对用户传入的密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3.查询数据库中用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //4.不存在，抛异常
        if (user == null) {
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //5.保存用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);

    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;

    }

    /**
     * 修改个人信息
     *
     * @param userUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Long updateUser(UserUpdateRequest userUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(userUpdateRequest==null,ErrorCode.PARAMS_ERROR);

        //判断要修改的用户是否存在
        Long oldUserId = userUpdateRequest.getId();
        ThrowUtils.throwIf(oldUserId==null,ErrorCode.PARAMS_ERROR);
        User oldUser = this.getById(oldUserId);
        ThrowUtils.throwIf(oldUser==null,ErrorCode.PARAMS_ERROR);

        //修改
        User newUser=new User();
        BeanUtils.copyProperties(userUpdateRequest,newUser);
        //newUser.setUpdateTime(new Date());

        //操作数据库
        boolean result = this.updateById(newUser);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据库操作失败");

        //返回结果
        return oldUserId;
    }

    /**
     * 编辑用户
     * @param userEditRequest
     * @param loginUser
     * @return
     */
    @Override
    public Long editUser(UserEditRequest userEditRequest, User loginUser) {
        ThrowUtils.throwIf(userEditRequest==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);
        long loginUserId = loginUser.getId();
        if(loginUserId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员，允许更新用户
        if(loginUserId!=userEditRequest.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //修改
        User newUser=new User();
        BeanUtils.copyProperties(userEditRequest,newUser);
        newUser.setEditTime(new Date());
        //操作数据库
        boolean result = this.updateById(newUser);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据库操作失败");

        return userEditRequest.getId();
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 查询参数
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userAvatar = userQueryRequest.getUserAvatar();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        Integer points = userQueryRequest.getPoints();
        Integer continueDays = userQueryRequest.getContinueDays();
        Date lastCheckinDate = userQueryRequest.getLastCheckinDate();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(points), "points", points);
        queryWrapper.eq(ObjUtil.isNotNull(continueDays), "continueDays", continueDays);
        queryWrapper.eq(lastCheckinDate != null, "lastCheckinDate", lastCheckinDate);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userAvatar), "userAvatar", userAvatar);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    /**
     * 加盐加密
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "nan";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获得脱敏后的用户登录信息
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获取单个脱敏后的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

}




