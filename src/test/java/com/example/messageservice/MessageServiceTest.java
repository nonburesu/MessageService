package com.example.messageservice;

import com.example.messageservice.dto.MessageSendRequest;
import com.example.messageservice.entity.MessageTask;
import com.example.messageservice.mapper.MessageTaskMapper;
import com.example.messageservice.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private MessageTaskMapper taskMapper;

    @Test
    void testSendInstantMessageSuccess() throws Exception {
        // 准备请求
        MessageSendRequest request = createRequest();

        // 模拟邮件发送成功
        when(javaMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));

        // 发送消息
        Map<String, Object> result = messageService.sendInstantMessage(request);
        assertEquals("PENDING", result.get("status"));

        // 等待异步处理完成
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            MessageTask task = taskMapper.selectById(request.getMessageId());
            return "SUCCESS".equals(task.getStatus());
        });

        // 验证状态
        MessageTask task = taskMapper.selectById(request.getMessageId());
        assertEquals("SUCCESS", task.getStatus());
        assertEquals(0, task.getRetryCount());
    }

    @Test
    void testRetryMechanism() throws Exception {
        // 准备请求
        MessageSendRequest request = createRequest();

        // 模拟第一次发送失败，第二次成功
        when(javaMailSender.createMimeMessage())
                .thenThrow(new RuntimeException("模拟邮件发送失败"))
                .thenReturn(Mockito.mock(MimeMessage.class));

        // 发送消息
        messageService.sendInstantMessage(request);

        // 等待重试成功
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            MessageTask task = taskMapper.selectById(request.getMessageId());
            return "SUCCESS".equals(task.getStatus());
        });

        // 验证重试次数
        MessageTask task = taskMapper.selectById(request.getMessageId());
        assertEquals("SUCCESS", task.getStatus());
        assertEquals(1, task.getRetryCount());
    }

    private MessageSendRequest createRequest() {
        MessageSendRequest request = new MessageSendRequest();
        request.setMessageId(UUID.randomUUID().toString());
        request.setIdempotencyKey(UUID.randomUUID().toString());
        request.setSubject("测试消息");
        request.setContent("这是一条测试消息内容");
        request.setTo(List.of("test@example.com"));
        return request;
    }
}