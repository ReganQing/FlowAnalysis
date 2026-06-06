-- 会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id            TEXT PRIMARY KEY,
    title         TEXT NOT NULL,
    model_name    TEXT NOT NULL,
    created_at    TEXT NOT NULL,
    updated_at    TEXT NOT NULL
);

-- 消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id    TEXT NOT NULL,
    role          TEXT NOT NULL,
    content       TEXT NOT NULL,
    model_name    TEXT,
    tool_name     TEXT,
    tool_input    TEXT,
    tool_duration INTEGER,
    created_at    TEXT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
);

-- 设置表
CREATE TABLE IF NOT EXISTS app_settings (
    key           TEXT PRIMARY KEY,
    value         TEXT NOT NULL,
    updated_at    TEXT NOT NULL
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_message_session ON chat_message(session_id, created_at);
CREATE INDEX IF NOT EXISTS idx_session_updated ON chat_session(updated_at DESC);

-- 默认设置
INSERT OR IGNORE INTO app_settings (key, value, updated_at) VALUES
    ('default_model', 'qwen-max-latest', strftime('%Y-%m-%dT%H:%M:%S', 'now')),
    ('theme', 'dark', strftime('%Y-%m-%dT%H:%M:%S', 'now'));
