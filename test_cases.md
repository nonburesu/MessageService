# 消息服务测试用例集合

本文档包含了消息服务系统的各类测试用例，包括普通邮件发送、模板创建、模板使用以及邮件服务源切换等测试场景。

## 1. 普通邮件发送测试

### 1.1 发送自定义欢迎通知
```bash
curl -X POST http://localhost:8080/message/send/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "2629827042@qq.com",      # 收件人邮箱地址
    "subject": "欢迎通知",          # 邮件主题
    "content": "欢迎加入我们的平台！", # 邮件内容
    "username": "小明",             # 收件人用户名
    "actionLink": "https://example.com/welcome", # 操作按钮链接
    "actionText": "开始使用"        # 操作按钮文本
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题
- `content`: 邮件正文内容
- `username`: 收件人用户名（可选）
- `actionLink`: 操作按钮链接（可选）
- `actionText`: 操作按钮文本（可选）

### 1.2 基础消息发送
```bash
curl -X POST http://localhost:8080/message/send \
-H "Content-Type: application/json" \
-d '{
    "messageId": "msg-001",         # 消息唯一标识
    "idempotencyKey": "key-001",    # 幂等性键
    "subject": "邮件主题",          # 邮件主题
    "content": "邮件内容",          # 邮件内容
    "to": ["example@example.com"],  # 收件人列表
    "templateId": null,             # 模板ID（不使用模板时为null）
    "templateVariables": null       # 模板变量（不使用模板时为null）
}'
```

参数说明：
- `messageId`: 消息的唯一标识符
- `idempotencyKey`: 用于防止重复发送的幂等性键
- `subject`: 邮件主题
- `content`: 邮件正文内容
- `to`: 收件人邮箱地址列表
- `templateId`: 模板ID，不使用模板时为null
- `templateVariables`: 模板变量，不使用模板时为null

## 2. 创建模板测试

### 2.1 使用自定义创建的模板发送测试
```bash
# 假设创建模板后返回的模板ID为 "temp_7c9af17b"
curl -X POST http://localhost:8080/message/send/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "subject": "测试标题",          # 邮件主题
    "templateId": "temp_7c9af17b",  # 上一步创建的模板ID
    "templateVariables": {
        "username": "张三"          # 对应模板中的 ${username} 变量
    }
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题（可选，如果不提供则使用模板标题）
- `templateId`: 使用的模板ID（创建模板时返回的ID）
- `templateVariables`: 模板变量值
  - `username`: 用户名（对应模板内容中的 ${username} 变量）

响应示例：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "messageId": "msg_abc123",
        "status": "SENT"
    }
}
```

### 2.2 查询自定义模板列表
```bash
curl -X GET http://localhost:8080/template/list
```

响应示例：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "templates": [
            {
                "templateId": "temp_7c9af17b",
                "templeName": "测试模板",
                "templateTitle": "测试标题",
                "permissionLevel": 1,
                "isActive": true,
                "createdAt": "2024-03-21 15:30:00",
                "updateAt": "2024-03-21 15:30:00"
            }
        ]
    }
}
```

## 3. 使用模板发送测试

### 3.1 验证码模板发送
```bash
curl -X POST http://localhost:8080/message/send/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "subject": "验证码",            # 邮件主题
    "templateId": "verification-code", # 验证码模板ID
    "templateVariables": {
        "code": "123456",           # 验证码
        "expireTime": "5分钟"       # 过期时间
    }
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题
- `templateId`: 使用的模板ID
- `templateVariables`: 模板变量值
  - `code`: 验证码
  - `expireTime`: 验证码的有效期

### 3.2 密码重置模板发送
```bash
curl -X POST http://localhost:8080/message/send/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "subject": "密码重置",          # 邮件主题
    "templateId": "reset-password", # 密码重置模板ID
    "templateVariables": {
        "username": "张三",         # 用户名
        "resetLink": "https://example.com/reset?token=abc123", # 重置链接
        "expireTime": "24小时"      # 链接有效期
    }
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题
- `templateId`: 使用的模板ID
- `templateVariables`: 模板变量值
  - `username`: 用户名
  - `resetLink`: 密码重置链接
  - `expireTime`: 重置链接的有效期

### 3.3 安全警告模板发送
```bash
curl -X POST http://localhost:8080/message/send/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "subject": "安全警告",          # 邮件主题
    "templateId": "security-alert", # 安全警告模板ID
    "templateVariables": {
        "username": "张三",         # 用户名
        "loginTime": "2024-03-21 14:30:00", # 登录时间
        "location": "北京",         # 登录地点
        "device": "iPhone 15",      # 设备信息
        "ipAddress": "192.168.1.1", # IP地址
        "securityLink": "https://example.com/security" # 安全设置链接
    }
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题
- `templateId`: 使用的模板ID
- `templateVariables`: 模板变量值
  - `username`: 用户名
  - `loginTime`: 登录时间
  - `location`: 登录地点
  - `device`: 登录设备
  - `ipAddress`: 登录IP地址
  - `securityLink`: 安全设置页面链接

### 3.4 通用通知模板发送
```bash
curl -X POST http://localhost:8080/message/send/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "subject": "会议通知",          # 邮件主题
    "templateId": "scheduled-notice", # 通用通知模板ID
    "templateVariables": {
        "username": "张三",         # 用户名
        "subject": "项目进度会议",   # 通知主题
        "content": "<p>请于明天下午2点参加项目进度讨论会议。</p><p>会议地点：线上会议室</p>", # 通知内容
        "sendTime": "2024-03-21 10:00:00", # 发送时间
        "hasAction": true,          # 是否包含操作按钮
        "actionLink": "https://meeting.example.com/join/123456", # 操作链接
        "actionText": "加入会议"    # 操作按钮文本
    }
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题
- `templateId`: 使用的模板ID
- `templateVariables`: 模板变量值
  - `username`: 用户名
  - `subject`: 通知主题
  - `content`: 通知内容（支持HTML格式）
  - `sendTime`: 发送时间
  - `hasAction`: 是否显示操作按钮
  - `actionLink`: 操作按钮链接
  - `actionText`: 操作按钮文本

## 4. 切换邮件发送源测试

### 4.1 切换到163邮箱服务
```bash
curl -X POST http://localhost:8080/message/config/switch-mail-service \
-H "Content-Type: application/json" \
-d '{
    "serviceType": "163"    # 邮件服务类型
}'
```

参数说明：
- `serviceType`: 邮件服务类型，可选值：
  - `163`: 使用163邮箱服务
  - `qq`: 使用QQ邮箱服务

### 4.2 切换到QQ邮箱服务
```bash
curl -X POST http://localhost:8080/message/config/switch-mail-service \
-H "Content-Type: application/json" \
-d '{
    "serviceType": "qq"     # 邮件服务类型
}'
```

### 4.3 启动时指定邮件服务类型
```bash
java -Dmail.service.type=163 -jar message-service-1.0.0.jar
```

参数说明：
- `mail.service.type`: 指定启动时使用的邮件服务类型（163或qq）

## 5. 定时发送测试

### 5.1 创建定时发送任务
```bash
curl -X POST http://localhost:8080/message/schedule/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "subject": "会议提醒",          # 邮件主题
    "content": "项目进度会议将在下周一下午2点开始，请提前5分钟进入会议室。", # 邮件内容
    "username": "张经理",           # 收件人用户名
    "actionLink": "https://meeting.example.com/join/123456", # 操作按钮链接
    "actionText": "进入会议室",     # 操作按钮文本
    "scheduledTime": "2025-06-23 13:55:00" # 计划发送时间
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `subject`: 邮件主题
- `content`: 邮件正文内容
- `username`: 收件人用户名
- `actionLink`: 操作按钮链接（可选）
- `actionText`: 操作按钮文本（可选）
- `scheduledTime`: 计划发送时间（格式：yyyy-MM-dd HH:mm:ss）

响应示例：
```json
{
    "status": "定时发送任务创建成功",
    "taskId": "task_abc123"
}
```

### 5.2 使用模板创建定时任务
```bash
curl -X POST http://localhost:8080/message/schedule/custom \
-H "Content-Type: application/json" \
-d '{
    "to": "example@example.com",    # 收件人邮箱
    "templateId": "scheduled-notice", # 使用的模板ID
    "templateVariables": {
        "username": "张三",         # 用户名
        "subject": "项目周会",      # 通知主题
        "content": "请准时参加每周项目进度会议", # 通知内容
        "sendTime": "2024-03-22 09:00:00", # 发送时间
        "hasAction": true,          # 是否包含操作按钮
        "actionLink": "https://meeting.example.com/join/123456", # 操作链接
        "actionText": "加入会议"    # 操作按钮文本
    },
    "scheduledTime": "2024-03-22 08:45:00" # 计划发送时间（提前15分钟发送提醒）
}'
```

参数说明：
- `to`: 收件人邮箱地址
- `templateId`: 使用的模板ID
- `templateVariables`: 模板变量值
  - `username`: 用户名
  - `subject`: 通知主题
  - `content`: 通知内容
  - `sendTime`: 显示的发送时间
  - `hasAction`: 是否显示操作按钮
  - `actionLink`: 操作按钮链接
  - `actionText`: 操作按钮文本
- `scheduledTime`: 实际的计划发送时间

### 5.3 查看定时任务状态
```bash
curl http://localhost:8080/message/schedule/task_abc123
```

响应示例：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": "task_abc123",
        "status": "PENDING",        # PENDING, COMPLETED, CANCELLED, FAILED
        "scheduledTime": "2024-03-22 08:45:00",
        "createTime": "2024-03-21 10:00:00",
        "lastUpdateTime": "2024-03-21 10:00:00"
    }
}
```

### 5.4 取消定时任务
```bash
curl -X DELETE http://localhost:8080/message/schedule/task_abc123
```

响应示例：
```json
{
    "code": 200,
    "message": "定时任务已取消"
}
```

### 5.5 批量创建定时任务
```bash
curl -X POST http://localhost:8080/message/schedule/batch \
-H "Content-Type: application/json" \
-d '{
    "tasks": [
        {
            "to": ["user1@example.com"],
            "subject": "提醒1",
            "content": "这是第一个提醒",
            "scheduledTime": "2024-03-22 09:00:00"
        },
        {
            "to": ["user2@example.com"],
            "subject": "提醒2",
            "content": "这是第二个提醒",
            "scheduledTime": "2024-03-22 10:00:00"
        }
    ]
}'
```

参数说明：
- `tasks`: 任务数组，每个任务包含：
  - `to`: 收件人邮箱地址列表
  - `subject`: 邮件主题
  - `content`: 邮件内容
  - `scheduledTime`: 计划发送时间

响应示例：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskIds": ["task_001", "task_002"],
        "failedTasks": []
    }
}
```

### 5.6 定时发送注意事项

1. 时间格式要求：
   - 所有时间输入必须使用格式：`yyyy-MM-dd HH:mm:ss`
   - 时区默认使用系统时区
   - 计划发送时间必须大于当前时间

2. 定时任务限制：
   - 每个收件人每分钟最多接收3封邮件
   - 定时任务最长可预约7天后的时间点
   - 已经过期的时间点将被拒绝

3. 任务状态说明：
   - `PENDING`: 等待发送
   - `COMPLETED`: 发送完成
   - `CANCELLED`: 已取消
   - `FAILED`: 发送失败

4. 最佳实践：
   - 重要的定时消息建议设置提前量
   - 大批量定时任务建议错峰发送
   - 定期检查待发送的定时任务状态
   - 建议为重要通知设置失败回调通知

## 6. 测试注意事项

### 6.1 发送测试前请确保：
- 邮箱服务器配置正确
- 授权码已正确设置
- 模板ID存在且有效

### 6.2 测试建议：
- 首次使用前先发送测试邮件
- 检查所有变量是否正确渲染
- 验证邮件样式在不同邮件客户端的显示效果

### 6.3 错误处理：
- 发送失败会自动重试，最大重试次数为5次
- 可以通过状态查询接口监控发送状态
- 系统会自动处理SSL/TLS加密连接

### 6.4 安全提示：
- 请勿在代码中硬编码授权码
- 建议使用环境变量或配置文件管理敏感信息
- 定期更换授权码以提高安全性 