package com.example.messageservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.messageservice.entity.MessageTask;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface MessageTaskMapper extends BaseMapper<MessageTask> {
    @Delete("DELETE FROM message_task WHERE created_at < #{threshold}")
    int deleteOldTasks(@Param("threshold") LocalDateTime threshold);
}