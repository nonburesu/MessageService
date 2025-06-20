package com.example.messageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class MessageSendRequest {
    @NotBlank
    private String messageId;
    @NotBlank
    private String idempotencyKey;
    @NotBlank
    private String subject;
    private String content;
    private List<@Email String> to;
    private String templateId;
    private Map<String, String> templateVariables;
}
