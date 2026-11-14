package com.nan.daymarkbackend.model.dto.team;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加队伍
 * @TableName team
 */
@Data
public class TeamAddRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 团队简介
     */
    private String description;

    /**
     * 负责人用户id，关联 user.id
     */
    private Long ownerUserId;

    /**
     * 状态：pending待审核/active正常/rejected已驳回/closed已解散
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;



    private static final long serialVersionUID = 1L;
}