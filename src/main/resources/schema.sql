DROP TABLE IF EXISTS message_task;
DROP TABLE IF EXISTS message_template;

CREATE TABLE message_template (
    template_id VARCHAR(50) PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    template_content TEXT NOT NULL,
    permission_level INT DEFAULT 1,
    template_variables TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE message_task (
    message_id VARCHAR(50) PRIMARY KEY,
    subject VARCHAR(255),
    content TEXT,
    to_list TEXT,
    template_id VARCHAR(50),
    template_variables TEXT,
    status VARCHAR(20),
    retry_count INT,
    next_retry_time TIMESTAMP,
    idempotency_key VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS templates (
    template_id VARCHAR(20) PRIMARY KEY,
    template_name VARCHAR(50) NOT NULL,
    template_title VARCHAR(50) NOT NULL,
    template_content TEXT NOT NULL,
    permission_level INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    template_variables TEXT,
    created_at DATETIME NOT NULL,
    update_at DATETIME NOT NULL
);