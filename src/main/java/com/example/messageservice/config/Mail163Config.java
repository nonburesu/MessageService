package com.example.messageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.mail.163")
public class Mail163Config {
    private String host = "smtp.163.com";
    private String username;
    private String password; // 授权码
    private String protocol = "smtp";
    private String defaultEncoding = "UTF-8";
    private String senderName = "系统通知";
    private Boolean auth = true;
    private Boolean debug = false;
} 