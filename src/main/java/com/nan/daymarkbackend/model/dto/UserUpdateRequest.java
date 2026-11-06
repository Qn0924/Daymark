package com.nan.daymarkbackend.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1618383174323152623L;
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/vip
     */
    private String userRole;

    /**
     * 当前积分余额
     */
    private Integer points;

    /**
     * 连续打卡天数
     */
    private Integer continueDays;

    /**
     * 上次有效打卡日期
     */
    private Date lastCheckinDate;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

}
