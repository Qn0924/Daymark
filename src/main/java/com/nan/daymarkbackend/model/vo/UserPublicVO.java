package com.nan.daymarkbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPublicVO implements Serializable {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;
}