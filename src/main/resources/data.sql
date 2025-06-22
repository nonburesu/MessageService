USE `messageservice`;

-- 插入模板数据 (如果使用模板功能)
INSERT INTO `message_template` (
    `template_id`, `template_name`, `template_content`,
    `permission_level`, `template_variables`
) VALUES
      ('welcome-001', '欢迎邮件',
       '<html><body><h1>欢迎{userName}加入系统</h1><p>亲爱的{userName}，欢迎您加入我们的系统！您的账号是{account}。</p></body></html>',
       1, '["userName", "account"]'),

      ('reset-pwd-002', '密码重置邮件',
       '<html><body><h1>密码重置通知</h1><p>尊敬的{userName}，您的密码已重置。新密码是：{newPassword}，请及时登录修改。</p></body></html>',
       2, '["userName", "newPassword"]');

-- 插入消息任务数据 (匹配MessageTask实体)
INSERT INTO `message_task` (
    `message_id`, `subject`, `content`, `to_list`,
    `template_id`, `template_variables`,
    `status`, `retry_count`, `idempotency_key`
) VALUES
-- 成功状态的消息 (使用模板)
('msg-001', '欢迎加入系统', '',
 '["user1@example.com", "user2@example.com"]',
 'welcome-001', '{"userName": "张三", "account": "zhangsan"}',
 'SUCCESS', 0, 'idemp-key-001'),

-- 成功状态的消息 (不使用模板)
('msg-002', '系统通知', '您的订单#20231001已处理完成',
 '["user3@example.com"]',
 NULL, NULL,
 'SUCCESS', 0, 'idemp-key-002'),

-- 待处理状态的消息
('msg-003', '账户激活提醒', '请尽快激活您的账户',
 '["newuser@example.com"]',
 NULL, NULL,
 'PENDING', 0, 'idemp-key-003'),

-- 重试中的消息
('msg-004', '密码重置通知', '您的密码重置请求正在处理',
 '["user4@example.com"]',
 'reset-pwd-002', '{"userName": "李四", "newPassword": "Temp@123"}',
 'RETRYING', 2, 'idemp-key-004'),

-- 失败状态的消息
('msg-005', '优惠活动通知', '双十一优惠活动开始啦！',
 '["invalid-email"]',
 NULL, NULL,
 'FAILED', 3, 'idemp-key-005');