package com.nan.daymarkbackend.model.dto.team;

import com.nan.daymarkbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamApplyQueryRequest extends PageRequest {

    /**
     * 团队id（负责人列表必传，我的申请不传）
     */
    private Long teamId;

    /**
     * 状态，不传默认 pending
     */
    private String status;
}