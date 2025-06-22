package com.example.messageservice.service;

import com.example.messageservice.dto.MessageSendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScheduledMessageService {

    private final MessageService messageService;
    private final TaskScheduler taskScheduler;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, Map<String, Object>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public ScheduledMessageService(MessageService messageService, TaskScheduler taskScheduler) {
        this.messageService = messageService;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 发送自定义通知（立即发送）
     */
    public void sendCustomNotice(String[] to, String subject, String content, String username, 
                               String actionLink, String actionText) {
        sendCustomNoticeInternal(to, subject, content, username, actionLink, actionText);
    }

    /**
     * 预约定时发送通知
     * @param scheduledTime 计划发送时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    public String scheduleCustomNotice(String[] to, String subject, String content, String username, 
                                   String actionLink, String actionText, String scheduledTime) {
        try {
            LocalDateTime sendTime = LocalDateTime.parse(scheduledTime, formatter);
            if (sendTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("计划发送时间不能早于当前时间");
            }

            String taskId = "scheduled-" + System.currentTimeMillis();
            
            // 保存任务信息
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("to", to);
            taskInfo.put("subject", subject);
            taskInfo.put("content", content);
            taskInfo.put("username", username);
            taskInfo.put("actionLink", actionLink);
            taskInfo.put("actionText", actionText);
            taskInfo.put("scheduledTime", scheduledTime);
            scheduledTasks.put(taskId, taskInfo);

            // 调度任务
            Date scheduledDate = Date.from(sendTime.atZone(ZoneId.systemDefault()).toInstant());
            taskScheduler.schedule(() -> {
                try {
                    sendCustomNoticeInternal(to, subject, content, username, actionLink, actionText);
                    scheduledTasks.remove(taskId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, scheduledDate);

            return taskId;
        } catch (Exception e) {
            throw new RuntimeException("设置定时发送失败：" + e.getMessage());
        }
    }

    /**
     * 获取定时任务信息
     */
    public Map<String, Object> getScheduledTaskInfo(String taskId) {
        return scheduledTasks.get(taskId);
    }

    /**
     * 取消定时任务
     */
    public boolean cancelScheduledTask(String taskId) {
        return scheduledTasks.remove(taskId) != null;
    }

    private void sendCustomNoticeInternal(String[] to, String subject, String content, String username, 
                                        String actionLink, String actionText) {
        String messageId = "custom-" + System.currentTimeMillis();
        String currentTime = LocalDateTime.now().format(formatter);
        
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("username", username);
        templateVars.put("subject", subject);
        templateVars.put("content", content);
        templateVars.put("sendTime", currentTime);
        
        boolean hasAction = actionLink != null && actionText != null;
        templateVars.put("hasAction", hasAction);
        if (hasAction) {
            templateVars.put("actionLink", actionLink);
            templateVars.put("actionText", actionText);
        }

        Map<String, Object> request = Map.of(
            "messageId", messageId,
            "idempotencyKey", messageId,
            "subject", subject,
            "to", to,
            "templateId", "scheduled-notice",
            "templateVariables", templateVars
        );
        
        messageService.sendInstantMessage(new MessageSendRequest(request));
    }

    // 每天17:00执行的系统通知示例
    @Scheduled(cron = "0 0 17 * * ?")
    public void sendDailyNotice() {
        sendCustomNotice(
            new String[]{"2629827042@qq.com"},
            "每日系统通知",
            "这是一条系统自动发送的每日通知。<br><br>" +
            "今日要点：<ul>" +
            "<li>系统运行正常</li>" +
            "<li>数据备份完成</li>" +
            "<li>所有服务正常运行</li>" +
            "</ul>",
            "管理员",
            "https://example.com/daily-report",
            "查看详细报告"
        );
    }

    // // 每5分钟执行一次（用于测试）
    // @Scheduled(fixedRate = 300000)
    public void sendTestNotice() {
        String messageId = "test-" + System.currentTimeMillis();
        String currentTime = LocalDateTime.now().format(formatter);
        
        Map<String, Object> request = Map.of(
            "messageId", messageId,
            "idempotencyKey", messageId,
            "subject", "测试定时通知",
            "to", new String[]{"2629827042@qq.com"},
            "templateId", "scheduled-notice",
            "templateVariables", Map.of(
                "username", "张三",
                "noticeType", "测试通知",
                "sendTime", currentTime,
                "message", "这是一条测试定时通知。当前时间：" + currentTime,
                "hasAction", false
            )
        );
        
        messageService.sendInstantMessage(new MessageSendRequest(request));
    }
} 