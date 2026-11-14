package com.nan.daymarkbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPasswordUpdateRequest implements Serializable {

    private static final long serialVersionUID = 8263830818323124131L;

    /**
     * 修改用户id
     */
    private Long id;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
