package com.example.messageservice.task;

import com.example.messageservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final MessageService messageService;

    @Autowired
    public ScheduledTasks(MessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanOldTasks() {
        messageService.cleanOldTasks();
    }
}