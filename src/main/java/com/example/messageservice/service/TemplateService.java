package com.example.messageservice.service;

import com.example.messageservice.entity.Template;
import java.util.List;

public interface TemplateService {
    Template createTemplate(Template template);
    List<Template> listTemplates();
    Template updateTemplate(Template template);
    void deleteTemplate(String templateId);
    Template getTemplateById(String templateId);
} 