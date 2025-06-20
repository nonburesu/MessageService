package com.example.messageservice.controller;

import com.example.messageservice.dto.MessageSendRequest;
import com.example.messageservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageSendRequest request) {
        return ResponseEntity.ok(messageService.sendInstantMessage(request));
    }

    @GetMapping("/status/{messageId}")
    public ResponseEntity<?> getStatus(@PathVariable String messageId) {
        return ResponseEntity.ok(messageService.getTaskStatus(messageId));
    }
}