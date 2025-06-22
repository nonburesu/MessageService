package com.example.messageservice.controller;

import com.example.messageservice.entity.Template;
import com.example.messageservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping("/create")
    public ResponseEntity<?> createTemplate(@RequestBody Template template) {
        Template created = templateService.createTemplate(template);
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success",
            "data", created
        ));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listTemplates() {
        List<Template> templates = templateService.listTemplates();
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success",
            "data", Map.of("templates", templates)
        ));
    }

    @PutMapping("/update/{templateId}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable String templateId,
            @RequestBody Template template) {
        template.setTemplateId(templateId);
        Template updated = templateService.updateTemplate(template);
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success",
            "data", updated
        ));
    }

    @DeleteMapping("/delete/{templateId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable String templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success"
        ));
    }
} 