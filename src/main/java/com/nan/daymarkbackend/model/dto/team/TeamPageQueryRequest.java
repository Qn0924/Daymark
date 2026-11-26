package com.nan.daymarkbackend.model.dto.team;

import com.nan.daymarkbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 申请查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamPageQueryRequest extends PageRequest {
    /**
     * 状态，不传默认 pending
     */
    private Long TeamId;
}