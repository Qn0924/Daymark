package com.nan.daymarkbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamMemberVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 学习计时次数
     */
    private Integer checkinCount;

    /**
     * 持续时间
     */
    private Integer totalMinutes;

    /**
     * 打卡次数
     */
    private Integer taskCount;
}