package com.nan.daymarkbackend.model.dto.point;

import com.nan.daymarkbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PointRecordQueryRequest extends PageRequest {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 记录id
     */
    private Long recordId;

    /**
     * 来源
     */
    private String source;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;
}