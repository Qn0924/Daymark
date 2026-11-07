-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/vip',
    points       int          default 0                 not null comment '当前积分余额',
    continueDays  int          default 0                 not null comment '连续打卡天数',
    lastCheckinDate date                                   null comment '上次有效打卡日期',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 打卡任务表
create table if not exists task_record
(
    id              bigint auto_increment comment '打卡记录id' primary key,
    taskId          bigint                                 not null comment '任务id（原task.id）',
    teamId          bigint                                 not null comment '所属团队id',
    creatorUserId   bigint                                 not null comment '任务创建人用户id',
    taskName        varchar(256)                           not null comment '任务名称',
    taskCategory    varchar(32)  default '其他'            not null comment '任务分类',
    description     varchar(512)                           null comment '任务说明',
    requireDuration int                                    not null comment '要求时长，分钟',
    requirePhoto tinyint default 1 not null comment '是否需要照片：1是 0否',
    startDate       date                                   not null comment '任务开始日期',
    endDate         date                                   not null comment '任务结束日期',
    taskStatus      varchar(32)  default 'open'            not null comment '任务状态：pending/open/close',
    auditTime       datetime                               null comment '审核时间',
    taskCreateTime  datetime     default CURRENT_TIMESTAMP not null comment '任务创建时间',
    userId          bigint                                 not null comment '打卡用户id',
    checkinDate     date                                   not null comment '打卡日期',
    checkinTime     datetime     default CURRENT_TIMESTAMP not null comment '打卡时间',
    duration        int                                    not null comment '本次学习时长，分钟',
    photoUrl        varchar(512)                           null comment '照片路径',
    recordStatus    varchar(32)  default 'normal'          not null comment 'normal正常/repair补卡',
    points          int          default 0                 not null comment '本次积分变动',

    createTime      datetime     default CURRENT_TIMESTAMP not null comment '打卡记录创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint      default 0                 not null comment '该行是否删除',

    UNIQUE KEY uk_task_user_date (taskId, userId, checkinDate, isDelete),
    INDEX idx_taskId        (taskId),
    INDEX idx_teamId        (teamId),
    INDEX idx_userId_date   (userId, checkinDate),
    INDEX idx_team_status   (teamId, taskStatus)
    ) comment '任务与打卡合并宽表' collate = utf8mb4_unicode_ci;