package com.example.messageservice.controller;

import com.example.messageservice.dto.MessageSendRequest;
import com.example.messageservice.service.MessageService;
import com.example.messageservice.service.ScheduledMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;
    private final ScheduledMessageService scheduledMessageService;

    @Autowired
    public MessageController(MessageService messageService, ScheduledMessageService scheduledMessageService) {
        this.messageService = messageService;
        this.scheduledMessageService = scheduledMessageService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageSendRequest request) {
        return ResponseEntity.ok(messageService.sendInstantMessage(request));
    }

    @GetMapping("/status/{messageId}")
    public ResponseEntity<?> getStatus(@PathVariable String messageId) {
        return ResponseEntity.ok(messageService.getTaskStatus(messageId));
    }

    // 发送自定义通知（立即发送）
    @PostMapping("/send/custom")
    public ResponseEntity<?> sendCustomNotice(@RequestBody Map<String, Object> request) {
        String[] to;
        Object toObj = request.get("to");
        if (toObj instanceof String) {
            to = ((String) toObj).split(",");
        } else if (toObj instanceof java.util.List) {
            java.util.List<?> toList = (java.util.List<?>) toObj;
            to = toList.stream()
                    .map(Object::toString)
                    .toArray(String[]::new);
        } else {
            throw new IllegalArgumentException("收件人格式不正确");
        }
        
        String subject = (String) request.get("subject");
        String content = (String) request.get("content");
        String username = (String) request.getOrDefault("username", "用户");
        String actionLink = (String) request.get("actionLink");
        String actionText = (String) request.get("actionText");

        scheduledMessageService.sendCustomNotice(to, subject, content, username, actionLink, actionText);
        return ResponseEntity.ok(Map.of("status", "通知发送成功"));
    }

    // 预约定时发送通知
    @PostMapping("/schedule/custom")
    public ResponseEntity<?> scheduleCustomNotice(@RequestBody Map<String, Object> request) {
        String[] to = ((String) request.get("to")).split(",");
        String subject = (String) request.get("subject");
        String content = (String) request.get("content");
        String username = (String) request.getOrDefault("username", "用户");
        String actionLink = (String) request.get("actionLink");
        String actionText = (String) request.get("actionText");
        String scheduledTime = (String) request.get("scheduledTime");

        String taskId = scheduledMessageService.scheduleCustomNotice(
            to, subject, content, username, actionLink, actionText, scheduledTime);
        return ResponseEntity.ok(Map.of(
            "status", "定时发送任务创建成功",
            "taskId", taskId
        ));
    }

    // 获取定时任务信息
    @GetMapping("/schedule/{taskId}")
    public ResponseEntity<?> getScheduledTask(@PathVariable String taskId) {
        Map<String, Object> taskInfo = scheduledMessageService.getScheduledTaskInfo(taskId);
        if (taskInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskInfo);
    }

    // 取消定时任务
    @DeleteMapping("/schedule/{taskId}")
    public ResponseEntity<?> cancelScheduledTask(@PathVariable String taskId) {
        boolean cancelled = scheduledMessageService.cancelScheduledTask(taskId);
        if (!cancelled) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("status", "定时任务已取消"));
    }

    // 测试每日通知
    @PostMapping("/test/daily")
    public ResponseEntity<Map<String, String>> testDailyNotice() {
        scheduledMessageService.sendDailyNotice();
        return ResponseEntity.ok(Map.of("status", "已触发每日通知发送"));
    }

    // 测试定时通知
    @PostMapping("/test/scheduled")
    public ResponseEntity<Map<String, String>> testScheduledNotice() {
        scheduledMessageService.sendTestNotice();
        return ResponseEntity.ok(Map.of("status", "已触发定时通知发送"));
    }
}