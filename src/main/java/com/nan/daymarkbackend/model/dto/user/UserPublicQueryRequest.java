package com.nan.daymarkbackend.model.dto.user;

import com.nan.daymarkbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPublicQueryRequest extends PageRequest {
    /**
     * 查询关键字
     */
    private String keyword;
}