package com.example.messageservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_task")
public class MessageTask {
    @TableId(type = IdType.ASSIGN_UUID)
    private String messageId;
    private String subject;
    private String content;
    private String toList;
    private String templateId;
    private String templateVariables;
    private String status = "PENDING";
    private Integer retryCount = 0;
    private LocalDateTime nextRetryTime;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}