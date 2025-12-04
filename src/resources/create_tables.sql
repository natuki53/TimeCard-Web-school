-- 勤怠管理サイト データベーススキーマ（テストデータ付き）
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

-- groups テーブル（グループ情報）
CREATE TABLE IF NOT EXISTS groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    admin_user_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_admin_user_id (admin_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- group_members テーブル（グループメンバー情報）
CREATE TABLE IF NOT EXISTS group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_group_user (group_id, user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- テスト用ユーザーデータ
-- ============================================
-- パスワードは平文で保存（本来はハッシュ化推奨）
INSERT INTO users (login_id, password_hash, name) VALUES
('yamada', 'yamada123', '山田太郎'),
('suzuki', 'suzuki123', '鈴木花子'),
('tanaka', 'tanaka123', '田中一郎'),
('watanabe', 'watanabe123', '渡辺美咲'),
('sato', 'sato123', '佐藤健太'),
('admin', 'admin123', '管理者'),
('member1', 'member123', 'メンバー1'),
('member2', 'member123', 'メンバー2')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- テスト用勤怠データ
-- ============================================
-- 現在の日付を基準に、過去30日分のデータを作成
-- 各ユーザーに対して様々なパターンの勤怠データを追加

-- 山田太郎 (user_id = 1) の勤怠データ
-- 今月のデータ（完全な出退勤）
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '09:00:00', '18:00:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:15:00', '18:30:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:05:00', '17:45:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '08:55:00', '18:15:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:10:00', '18:00:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', '17:50:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:20:00', '18:10:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00', '18:00:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '08:50:00', '18:20:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 9 DAY), '09:05:00', '18:05:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '09:00:00', '18:00:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 11 DAY), '09:15:00', '17:45:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 12 DAY), '09:00:00', '18:00:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '09:10:00', '18:15:00'),
(1, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '09:00:00', '18:00:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- 鈴木花子 (user_id = 2) の勤怠データ
-- 一部出勤のみ（退勤未打刻）、一部欠勤
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '09:30:00', NULL),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:25:00', '18:00:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '17:30:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, NULL),
(2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:15:00', '18:15:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', NULL),
(2, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:10:00', '18:00:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 7 DAY), NULL, NULL),
(2, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '09:20:00', '18:10:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 9 DAY), '09:05:00', '17:45:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '09:00:00', '18:00:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, NULL),
(2, DATE_SUB(CURDATE(), INTERVAL 12 DAY), '09:15:00', '18:20:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '09:00:00', '18:00:00'),
(2, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '09:10:00', NULL)
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- 田中一郎 (user_id = 3) の勤怠データ
-- 規則的な勤怠パターン
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(3, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:50:00', '17:50:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '08:55:00', '17:55:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '08:50:00', '17:50:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '08:50:00', '17:50:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 9 DAY), '08:55:00', '17:55:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 11 DAY), '08:50:00', '17:50:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 12 DAY), '08:45:00', '17:45:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '08:50:00', '17:50:00'),
(3, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '08:45:00', '17:45:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- 渡辺美咲 (user_id = 4) の勤怠データ
-- 遅刻・早退パターン
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(4, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '10:00:00', '18:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '16:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00', '18:30:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00', '17:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '10:15:00', '18:15:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', '16:30:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:45:00', '18:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00', '17:15:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '10:00:00', '18:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 9 DAY), '09:00:00', '16:45:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '09:30:00', '18:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 11 DAY), '09:00:00', '17:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 12 DAY), '10:00:00', '18:30:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '09:00:00', '16:00:00'),
(4, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '09:45:00', '18:00:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- 佐藤健太 (user_id = 5) の勤怠データ
-- 様々なパターン（完全、部分、欠勤）
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(5, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL, NULL),
(5, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00', NULL),
(5, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 5 DAY), NULL, NULL),
(5, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 8 DAY), NULL, NULL),
(5, DATE_SUB(CURDATE(), INTERVAL 9 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '09:00:00', NULL),
(5, DATE_SUB(CURDATE(), INTERVAL 11 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 12 DAY), NULL, NULL),
(5, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '09:00:00', '18:00:00'),
(5, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '09:00:00', '18:00:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- 管理者 (user_id = 6) の勤怠データ
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(6, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '09:00:00', '18:00:00'),
(6, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '18:00:00'),
(6, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '18:00:00'),
(6, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00', '18:00:00'),
(6, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:00:00', '18:00:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- メンバー1 (user_id = 7) の勤怠データ
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(7, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '09:00:00', '18:00:00'),
(7, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '18:00:00'),
(7, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '18:00:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- メンバー2 (user_id = 8) の勤怠データ
INSERT INTO attendance (user_id, work_date, start_time, end_time) VALUES
(8, DATE_SUB(CURDATE(), INTERVAL 0 DAY), '09:00:00', '18:00:00'),
(8, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '18:00:00'),
(8, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '18:00:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- ============================================
-- テスト用グループデータ
-- ============================================
-- グループ1: 開発チーム（管理者: 山田太郎）
INSERT INTO groups (name, description, admin_user_id) VALUES
('開発チーム', 'ソフトウェア開発を担当するチーム', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- グループ2: 営業チーム（管理者: 鈴木花子）
INSERT INTO groups (name, description, admin_user_id) VALUES
('営業チーム', '営業活動を担当するチーム', 2)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- グループ3: 管理チーム（管理者: 管理者）
INSERT INTO groups (name, description, admin_user_id) VALUES
('管理チーム', 'システム管理を担当するチーム', 6)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- ============================================
-- テスト用グループメンバーデータ
-- ============================================
-- グループ1（開発チーム）のメンバー
-- 管理者（山田太郎）は自動的にメンバーに含まれる想定ですが、明示的に追加
INSERT INTO group_members (group_id, user_id) VALUES
(1, 1),  -- 山田太郎（管理者）
(1, 3),  -- 田中一郎
(1, 5),  -- 佐藤健太
(1, 7)   -- メンバー1
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- グループ2（営業チーム）のメンバー
INSERT INTO group_members (group_id, user_id) VALUES
(2, 2),  -- 鈴木花子（管理者）
(2, 4),  -- 渡辺美咲
(2, 8)   -- メンバー2
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- グループ3（管理チーム）のメンバー
INSERT INTO group_members (group_id, user_id) VALUES
(3, 6),  -- 管理者（管理者）
(3, 1),  -- 山田太郎
(3, 2),  -- 鈴木花子
(3, 7),  -- メンバー1
(3, 8)   -- メンバー2
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);
