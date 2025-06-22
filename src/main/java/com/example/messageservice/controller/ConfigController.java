package com.example.messageservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/message/config")
public class ConfigController {

    @PostMapping("/switch-mail-service")
    public ResponseEntity<?> switchMailService(@RequestBody Map<String, String> request) {
        String serviceType = request.get("serviceType");
        if (serviceType != null) {
            System.setProperty("mail.service.type", serviceType);
            return ResponseEntity.ok(Map.of(
                "message", "邮件服务已切换到: " + serviceType,
                "status", "success"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
            "message", "serviceType不能为空",
            "status", "error"
        ));
    }
} 