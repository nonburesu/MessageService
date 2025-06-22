package com.example.messageservice.service;

import com.example.messageservice.config.Mail163Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

@Service
public class Mail163Service {

    @Autowired
    private Mail163Config mail163Config;

    private Session createSession() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", mail163Config.getHost());
        props.setProperty("mail.smtp.auth", mail163Config.getAuth().toString());
        props.setProperty("mail.transport.protocol", mail163Config.getProtocol());
        
        // 添加SSL配置
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtp.port", "465");
        
        Session session = Session.getInstance(props);
        session.setDebug(mail163Config.getDebug());
        return session;
    }

    public void sendEmail(String to, String subject, String content) throws Exception {
        Session session = createSession();
        MimeMessage message = createMimeMessage(session, to, subject, content);
        
        Transport transport = session.getTransport();
        transport.connect(mail163Config.getUsername(), mail163Config.getPassword());
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    private MimeMessage createMimeMessage(Session session, String to, String subject, String content) throws Exception {
        MimeMessage message = new MimeMessage(session);
        
        // 设置发件人
        message.setFrom(new InternetAddress(mail163Config.getUsername(), mail163Config.getSenderName(), mail163Config.getDefaultEncoding()));
        // 设置收件人
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        // 设置邮件主题
        message.setSubject(subject, mail163Config.getDefaultEncoding());
        // 设置邮件正文
        message.setContent(content, "text/html;charset=" + mail163Config.getDefaultEncoding());
        // 设置发送时间
        message.setSentDate(new Date());
        message.saveChanges();
        
        return message;
    }
} 