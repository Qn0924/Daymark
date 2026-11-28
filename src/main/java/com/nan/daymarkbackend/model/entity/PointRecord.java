package com.nan.daymarkbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 积分流水
 * @TableName point_record
 */
@TableName(value ="point_record")
@Data
public class PointRecord implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 打卡记录id
     */
    private Long recordId;

    /**
     * 积分变动值，正数增加，负数扣减
     */
    private Integer points;

    /**
     * 来源：checkin打卡/makeup补卡/break断卡
     */
    private String source;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}