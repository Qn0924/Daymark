package com.nan.daymarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nan.daymarkbackend.model.entity.TaskRecord;
import com.nan.daymarkbackend.service.TaskRecordService;
import com.nan.daymarkbackend.mapper.TaskRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author 21289
* @description 针对表【task_record(任务与打卡合并宽表)】的数据库操作Service实现
* @createDate 2026-11-08 17:08:34
*/
@Service
public class TaskRecordServiceImpl extends ServiceImpl<TaskRecordMapper, TaskRecord>
    implements TaskRecordService{

}




