package com.example.messageservice.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Template {
    private String templateId;
    private String templeName;
    private String templateTitle;
    private String templateContent;
    private Integer permissionLevel;
    private Boolean isActive;
    private String templateVariables;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
} 