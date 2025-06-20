package com.example.messageservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.messageservice.config.AppConfig;
import com.example.messageservice.dto.FeedbackRequest;
import com.example.messageservice.dto.MessageSendRequest;
import com.example.messageservice.entity.MessageTask;
import com.example.messageservice.mapper.MessageTaskMapper;
import com.example.messageservice.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MessageService {

    private static final int MAX_RETRIES = 5;
    private static final String IDEMPOTENCY_LOCK_PREFIX = "msg:lock:";

    private final MessageTaskMapper taskMapper;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final TaskScheduler taskScheduler;
    private final ObjectMapper objectMapper;

    @Value("${topbiz.feedback-url}")
    private String feedbackUrl;

    @Autowired
    public MessageService(
            MessageTaskMapper taskMapper,
            TemplateEngine templateEngine,
            JavaMailSender javaMailSender,
            TaskScheduler taskScheduler,
            ObjectMapper objectMapper
    ) {
        this.taskMapper = taskMapper;
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
        this.taskScheduler = taskScheduler;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> sendInstantMessage(MessageSendRequest request) {
        try {
            String lockKey = IDEMPOTENCY_LOCK_PREFIX + request.getIdempotencyKey();

            // 检查是否已存在任务
            MessageTask existingTask = taskMapper.selectById(request.getMessageId());
            if (existingTask != null) {
                return Map.of("messageId", existingTask.getMessageId(), "status", existingTask.getStatus());
            }

            // 创建新任务
            MessageTask task = createTaskFromRequest(request);
            taskMapper.insert(task);

            // 异步处理任务
            taskScheduler.schedule(() -> processMessageTask(task), Instant.now());

            return Map.of("messageId", task.getMessageId(), "status", task.getStatus());
        } catch (Exception e) {
            throw new RuntimeException("发送请求处理失败", e);
        }
    }

    public Map<String, Object> getTaskStatus(String messageId) {
        MessageTask task = taskMapper.selectById(messageId);
        if (task == null) {
            return Map.of("status", "NOT_FOUND");
        }
        return Map.of(
                "messageId", task.getMessageId(),
                "status", task.getStatus(),
                "retryCount", task.getRetryCount(),
                "updatedAt", task.getUpdatedAt()
        );
    }

    private MessageTask createTaskFromRequest(MessageSendRequest request) {
        MessageTask task = new MessageTask();
        task.setMessageId(request.getMessageId());
        task.setSubject(request.getSubject());
        task.setTemplateId(request.getTemplateId());
        task.setIdempotencyKey(request.getIdempotencyKey());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        try {
            task.setToList(objectMapper.writeValueAsString(request.getTo()));
            if (request.getTemplateVariables() != null) {
                task.setTemplateVariables(objectMapper.writeValueAsString(request.getTemplateVariables()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }

        // 渲染模板
        if (request.getTemplateId() != null && request.getTemplateVariables() != null) {
            task.setContent(renderTemplate(request.getTemplateId(), request.getTemplateVariables()));
        } else {
            task.setContent(request.getContent());
        }

        return task;
    }

    private void processMessageTask(MessageTask task) {
        try {
            // 获取最新任务状态
            MessageTask currentTask = taskMapper.selectById(task.getMessageId());
            if (currentTask == null ||
                    !("PENDING".equals(currentTask.getStatus()) &&
                            !"RETRYING".equals(currentTask.getStatus()))) {
                return;
            }

            // 发送邮件
            sendEmail(currentTask);

            // 更新状态
            currentTask.setStatus("SUCCESS");
            currentTask.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(currentTask);

            // 发送反馈
            sendFeedbackToTopBiz(currentTask, true);
        } catch (Exception e) {
            handleSendFailure(task, e);
        }
    }

    private void handleSendFailure(MessageTask task, Exception e) {
        // 获取最新任务状态
        MessageTask currentTask = taskMapper.selectById(task.getMessageId());
        if (currentTask == null) return;

        if (currentTask.getRetryCount() >= MAX_RETRIES) {
            // 超过最大重试次数
            currentTask.setStatus("FAILED");
            currentTask.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(currentTask);
            sendFeedbackToTopBiz(currentTask, false);
            return;
        }

        // 计算下一次重试时间（指数退避）
        int delaySeconds = (int) Math.pow(2, currentTask.getRetryCount());
        currentTask.setStatus("RETRYING");
        currentTask.setRetryCount(currentTask.getRetryCount() + 1);
        currentTask.setNextRetryTime(LocalDateTime.now().plusSeconds(delaySeconds));
        currentTask.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(currentTask);

        // 调度重试任务
        taskScheduler.schedule(
                () -> processMessageTask(currentTask),
                Instant.now().plusSeconds(delaySeconds)
        );
    }

    private void sendEmail(MessageTask task) throws MessagingException, JsonProcessingException {
        List<String> toList = objectMapper.readValue(task.getToList(), new TypeReference<List<String>>() {});

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toList.toArray(new String[0]));
        helper.setSubject(task.getSubject());
        helper.setText(task.getContent(), true);

        javaMailSender.send(message);
    }

    private void sendFeedbackToTopBiz(MessageTask task, boolean success) {
        try {
            FeedbackRequest feedback = new FeedbackRequest(
                    task.getMessageId(),
                    success,
                    success ? null : "发送失败"
            );

            // 在实际应用中，这里会调用TopBiz的反馈接口
            System.out.println("发送反馈到TopBiz: " + feedback);
        } catch (Exception e) {
            System.err.println("反馈发送失败: " + e.getMessage());
        }
    }

    private String renderTemplate(String templateId, Map<String, String> variables) {
        Context context = new Context();
        context.setVariables(Collections.unmodifiableMap(variables));
        return templateEngine.process(templateId, context);
    }

    public void cleanOldTasks() {
        LocalDateTime threshold = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        taskMapper.deleteOldTasks(threshold);
        System.out.println("清理了30天前的旧任务");
    }
}