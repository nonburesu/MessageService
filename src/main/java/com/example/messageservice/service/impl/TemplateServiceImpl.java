package com.example.messageservice.service.impl;

import com.example.messageservice.entity.Template;
import com.example.messageservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Template createTemplate(Template template) {
        template.setTemplateId("temp_" + UUID.randomUUID().toString().substring(0, 8));
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdateAt(LocalDateTime.now());
        
        String sql = "INSERT INTO templates (template_id, template_name, template_title, template_content, " +
                    "permission_level, is_active, template_variables, created_at, update_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            template.getTemplateId(),
            template.getTempleName(),
            template.getTemplateTitle(),
            template.getTemplateContent(),
            template.getPermissionLevel(),
            template.getIsActive(),
            template.getTemplateVariables(),
            template.getCreatedAt(),
            template.getUpdateAt()
        );
        
        return template;
    }

    @Override
    public List<Template> listTemplates() {
        String sql = "SELECT * FROM templates WHERE is_active = true";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Template template = new Template();
            template.setTemplateId(rs.getString("template_id"));
            template.setTempleName(rs.getString("template_name"));
            template.setTemplateTitle(rs.getString("template_title"));
            template.setTemplateContent(rs.getString("template_content"));
            template.setPermissionLevel(rs.getInt("permission_level"));
            template.setIsActive(rs.getBoolean("is_active"));
            template.setTemplateVariables(rs.getString("template_variables"));
            template.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            template.setUpdateAt(rs.getTimestamp("update_at").toLocalDateTime());
            return template;
        });
    }

    @Override
    public Template updateTemplate(Template template) {
        template.setUpdateAt(LocalDateTime.now());
        
        String sql = "UPDATE templates SET template_name = ?, template_title = ?, template_content = ?, " +
                    "permission_level = ?, is_active = ?, template_variables = ?, update_at = ? " +
                    "WHERE template_id = ?";
        
        jdbcTemplate.update(sql,
            template.getTempleName(),
            template.getTemplateTitle(),
            template.getTemplateContent(),
            template.getPermissionLevel(),
            template.getIsActive(),
            template.getTemplateVariables(),
            template.getUpdateAt(),
            template.getTemplateId()
        );
        
        return template;
    }

    @Override
    public void deleteTemplate(String templateId) {
        String sql = "UPDATE templates SET is_active = false WHERE template_id = ?";
        jdbcTemplate.update(sql, templateId);
    }

    @Override
    public Template getTemplateById(String templateId) {
        String sql = "SELECT * FROM templates WHERE template_id = ? AND is_active = true";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Template template = new Template();
            template.setTemplateId(rs.getString("template_id"));
            template.setTempleName(rs.getString("template_name"));
            template.setTemplateTitle(rs.getString("template_title"));
            template.setTemplateContent(rs.getString("template_content"));
            template.setPermissionLevel(rs.getInt("permission_level"));
            template.setIsActive(rs.getBoolean("is_active"));
            template.setTemplateVariables(rs.getString("template_variables"));
            template.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            template.setUpdateAt(rs.getTimestamp("update_at").toLocalDateTime());
            return template;
        }, templateId);
    }
} 