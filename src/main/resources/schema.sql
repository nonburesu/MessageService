DROP TABLE IF EXISTS message_task;

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