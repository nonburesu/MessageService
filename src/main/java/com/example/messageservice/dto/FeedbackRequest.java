package com.example.messageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
public class FeedbackRequest {
    private String messageId;
    private boolean status;
    private String reason;
}