-- 既存DBを「秘密の質問方式」へ移行するための ALTER スクリプト
-- 実行前に必ずバックアップを取ってください。
--
-- 例:
--   mysql -u root -p timecard_db < alter_users_add_secret_question.sql

-- 1) 追加（まずNULL許容で追加）
ALTER TABLE users
  ADD COLUMN secret_question VARCHAR(255) NULL AFTER password_hash,
  ADD COLUMN secret_answer_hash VARCHAR(255) NULL AFTER secret_question,
  ADD COLUMN bio TEXT NULL AFTER name,
  ADD COLUMN dm_allowed TINYINT(1) NOT NULL DEFAULT 1 AFTER bio;

-- 2) 既存ユーザーへ暫定値を入れる（必要に応じて変更してください）
UPDATE users
SET secret_question = '好きな食べ物は？',
    secret_answer_hash = 'ramen'
WHERE secret_question IS NULL OR secret_question = '';

-- 3) 必須化
ALTER TABLE users
  MODIFY secret_question VARCHAR(255) NOT NULL,
  MODIFY secret_answer_hash VARCHAR(255) NOT NULL;

-- 既読管理/DM用テーブル（存在しなければ作成）
CREATE TABLE IF NOT EXISTS group_last_read (
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    last_read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
    INDEX idx_group_last_read_group (group_id, last_read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dm_threads (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_dm_pair (user1_id, user2_id),
    INDEX idx_dm_user1 (user1_id),
    INDEX idx_dm_user2 (user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dm_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    thread_id INT NOT NULL,
    sender_user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (thread_id) REFERENCES dm_threads(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_dm_thread_created (thread_id, created_at),
    INDEX idx_dm_sender (sender_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dm_message_attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message_id INT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES dm_messages(id) ON DELETE CASCADE,
    INDEX idx_dm_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dm_last_read (
    user_id INT NOT NULL,
    thread_id INT NOT NULL,
    last_read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, thread_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES dm_threads(id) ON DELETE CASCADE,
    INDEX idx_dm_last_read_thread (thread_id, last_read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) もし過去に email 系カラムを追加していた場合は削除（存在しないとエラーになるので注意）
--    先にDESCRIBE users; で確認してから必要な行だけ実行してください。
-- ALTER TABLE users
--   DROP INDEX uk_users_email,
--   DROP INDEX idx_email,
--   DROP INDEX idx_email_verify_token_hash,
--   DROP INDEX idx_password_reset_token_hash,
--   DROP COLUMN password_reset_expires_at,
--   DROP COLUMN password_reset_token_hash,
--   DROP COLUMN email_verify_expires_at,
--   DROP COLUMN email_verify_token_hash,
--   DROP COLUMN email_verified,
--   DROP COLUMN email;


