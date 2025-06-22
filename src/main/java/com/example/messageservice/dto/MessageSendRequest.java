package com.example.messageservice.dto;

import java.util.List;
import java.util.Map;

public class MessageSendRequest {
    private String messageId;
    private String idempotencyKey;
    private String subject;
    private String content;
    private List<String> to;
    private String templateId;
    private Map<String, String> templateVariables;

    public MessageSendRequest() {
    }

    @SuppressWarnings("unchecked")
    public MessageSendRequest(Map<String, Object> map) {
        this.messageId = (String) map.get("messageId");
        this.idempotencyKey = (String) map.get("idempotencyKey");
        this.subject = (String) map.get("subject");
        this.content = (String) map.get("content");
        this.to = List.of((String[]) map.get("to"));
        this.templateId = (String) map.get("templateId");
        this.templateVariables = (Map<String, String>) map.get("templateVariables");
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, String> getTemplateVariables() {
        return templateVariables;
    }

    public void setTemplateVariables(Map<String, String> templateVariables) {
        this.templateVariables = templateVariables;
    }
}
