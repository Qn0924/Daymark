package com.nan.daymarkbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 团队成员
 * @TableName team_member
 */
@TableName(value ="team_member")
@Data
public class TeamMember implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色：owner负责人/worker普通成员/mentor监督者
     */
    private String role;

    /**
     * 状态：pending待审批/active正常/rejected已拒绝/left已退出/removed已移除/cancelled已取消
     */
    private String status;

    /**
     * 加入方式：create创建/apply申请/invite邀请/admin管理员授予
     */
    private String joinType;

    /**
     * 申请理由或邀请留言
     */
    private String applyReason;

    /**
     * 邀请人/授予人用户id（joinType=invite/admin 时填）
     */
    private Long inviterUserId;

    /**
     * 审批人用户id
     */
    private Long reviewerUserId;

    /**
     * 审批时间
     */
    private Date reviewTime;

    /**
     * 审批意见
     */
    private String reviewRemark;

    /**
     * 正式加入/授予时间
     */
    private Date joinTime;

    /**
     * 退出/移除时间
     */
    private Date leaveTime;

    /**
     * 退出/移除原因
     */
    private String leaveReason;

    /**
     * 退出/移除操作人id
     */
    private Long leaveUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}