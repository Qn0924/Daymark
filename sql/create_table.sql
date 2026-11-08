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
create table task
(
    id              bigint auto_increment comment '打卡记录id'
        primary key,
    taskId          bigint                                not null comment '任务id（原task.id）',
    teamId          bigint                                not null comment '所属团队id',
    creatorUserId   bigint                                not null comment '任务创建人用户id',
    taskName        varchar(256)                          not null comment '任务名称',
    taskCategory    varchar(32) default '其他'              not null comment '任务分类',
    description     varchar(512)                          null comment '任务说明',
    requireDuration int                                   not null comment '要求时长，分钟',
    requirePhoto    tinyint     default 1                 not null comment '是否需要照片：1是 0否',
    startDate       date                                  not null comment '任务开始日期',
    endDate         date                                  not null comment '任务结束日期',
    taskStatus      varchar(32) default 'open'            not null comment '任务状态：pending/open/rejected/close',
    auditTime       datetime                              null comment '审核时间',
    taskCreateTime  datetime    default CURRENT_TIMESTAMP not null comment '任务创建时间',
    userId          bigint                                null comment '打卡用户id，任务定义行为null',
    checkinDate     date                                  null comment '打卡日期，任务定义行为null',
    checkinTime     datetime                              null comment '打卡时间，任务定义行为null',
    duration        int                                   null comment '本次学习时长，分钟，任务定义行为null',
    photoUrl        varchar(512)                          null comment '照片路径',
    recordStatus    varchar(32) default 'normal'          not null comment 'normal正常/repair补卡',
    points          int         default 0                 not null comment '本次积分变动',
    createTime      datetime    default CURRENT_TIMESTAMP not null comment '打卡记录创建时间',
    updateTime      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint     default 0                 not null comment '该行是否删除',
    constraint uk_task_user_date
        unique (taskId, userId, checkinDate, isDelete)
)
    comment '任务与打卡合并宽表' collate = utf8mb4_unicode_ci;

create index idx_taskId
    on task (taskId);

create index idx_teamId
    on task (teamId);

create index idx_team_status
    on task (teamId, taskStatus);

create index idx_userId_date
    on task (userId, checkinDate);

-- 学习计时表
create table if not exists studyTime
(
    id                bigint auto_increment comment 'id' primary key,
    userId            bigint                                 not null comment '用户id',
    taskId            bigint                                 not null comment '打卡任务id',
    teamId            bigint                                 not null comment '所属打卡团id',
    startTime         datetime                               not null comment '开始学习时间',
    endTime           datetime                               null comment '结束学习时间',
    duration          int                                    null comment '计算出的时长，单位分钟',
    status            varchar(32)  default 'running'         not null comment '状态：running进行中/finished已结束/abandoned已放弃/expired已作废',
    lastHeartbeatTime datetime                               null comment '最后心跳时间',
    createTime        datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime        datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete          tinyint      default 0                 not null comment '是否删除',
    INDEX idx_userId_status (userId, status),
    INDEX idx_taskId (taskId)
    ) comment '学习计时' collate = utf8mb4_unicode_ci;

-- 积分流水表
create table if not exists point_record
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                                 not null comment '用户id',
    recordId    bigint                                 null comment '打卡记录id',
    points      int                                    not null comment '积分变动值，正数增加，负数扣减',
    source      varchar(32)                            not null comment '来源：checkin打卡/makeup补卡/break断卡',
    description varchar(512)                           null comment '描述',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete    tinyint      default 0                 null comment '是否删除',
    INDEX idx_userId (userId),
    INDEX idx_recordId (recordId)
    ) comment '积分流水' collate = utf8mb4_unicode_ci;

-- 团队表
create table if not exists team
(
    id          bigint auto_increment comment 'id' primary key,
    teamName    varchar(256)                           not null comment '团队名称',
    description varchar(512)                           null comment '团队简介',
    ownerUserId bigint                                 not null comment '负责人用户id，关联 user.id',
    status      varchar(32)  default 'pending'         not null comment '状态：pending待审核/active正常/rejected已驳回/closed已解散',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                 not null comment '是否删除',
    INDEX idx_ownerUserId (ownerUserId),
    INDEX idx_status (status)
    ) comment '团队' collate = utf8mb4_unicode_ci;

-- 团队成员表
create table if not exists team_member
(
    id             bigint auto_increment comment 'id' primary key,
    teamId         bigint                                 not null comment '团队id',
    userId         bigint                                 not null comment '用户id',

    role           varchar(32)  default 'worker'          not null comment '角色：owner负责人/worker普通成员/mentor监督者',
    status         varchar(32)  default 'pending'         not null comment '状态：pending待审批/active正常/rejected已拒绝/left已退出/removed已移除/cancelled已取消',
    joinType       varchar(32)  default 'apply'           not null comment '加入方式：create创建/apply申请/invite邀请/admin管理员授予',

    applyReason    varchar(512)                           null comment '申请理由或邀请留言',
    inviterUserId  bigint                                 null comment '邀请人/授予人用户id',
    reviewerUserId bigint                                 null comment '审批人用户id',
    reviewTime     datetime                               null comment '审批时间',
    reviewRemark   varchar(512)                           null comment '审批意见',

    joinTime       datetime                               null comment '正式加入/授予时间',
    leaveTime      datetime                               null comment '退出/移除时间',
    leaveReason    varchar(512)                           null comment '退出/移除原因',
    leaveUserId    bigint                                 null comment '退出/移除操作人id',

    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除',

    UNIQUE KEY uk_team_user (teamId, userId),
    INDEX idx_teamId_status (teamId, status, role),
    INDEX idx_userId_status (userId, status, role)
    ) comment '团队成员' collate = utf8mb4_unicode_ci;

