package com.nan.daymarkbackend.constant;

public interface StudytimeConstant {

    /**
     * 2分钟心跳
     */
    long HEARTBEAT_TIMEOUT_MILLIS = 2 * 60 * 1000L;

    /**
     * 计时最长24小时
     */
    long MAX_RUNNING_MILLIS = 24 * 60 * 60 * 1000L;

}