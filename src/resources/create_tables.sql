-- 勤怠管理サイト データベーススキーマ
-- データベース作成（必要に応じて）
-- CREATE DATABASE IF NOT EXISTS timecard_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE timecard_db;

-- users テーブル（ユーザー情報）
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- attendance テーブル（勤怠情報）
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, work_date),
    INDEX idx_user_id (user_id),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- テスト用ユーザー（パスワード: test123）
-- パスワードは平文で保存（本来はハッシュ化推奨）
INSERT INTO users (login_id, password_hash, name) VALUES
('test_user', 'test123', 'テストユーザー')
ON DUPLICATE KEY UPDATE name=name;

