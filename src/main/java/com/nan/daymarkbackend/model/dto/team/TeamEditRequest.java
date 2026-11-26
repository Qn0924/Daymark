package com.nan.daymarkbackend.model.dto.team;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamEditRequest implements Serializable {

    /**
     * 修改团队id
     */
    private Long id;

    /**
     * 团队名字
     */
    private String teamName;

    /**
     * 团队描述
     */
    private String description;
}