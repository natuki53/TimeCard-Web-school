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

-- attendance テーブル（勤怠情報）
-- グループ別に勤怠を保存する（未所属/グループなしは group_id=NULL として扱う）
-- 一般ユーザーが行った修正は corrected_by_admin=0 として区別する
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    group_id INT NULL,
    work_date DATE NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    prev_start_time TIME NULL,
    prev_end_time TIME NULL,
    is_cancelled TINYINT(1) NOT NULL DEFAULT 0,
    cancelled_by_admin TINYINT(1) NOT NULL DEFAULT 0,
    cancelled_by_user_id INT NULL,
    cancelled_at DATETIME NULL,
    is_corrected TINYINT(1) NOT NULL DEFAULT 0,
    corrected_by_admin TINYINT(1) NOT NULL DEFAULT 0,
    corrected_by_user_id INT NULL,
    corrected_at DATETIME NULL,
    -- group_id が NULL の場合も一意制約を効かせるためのキー
    group_id_key INT GENERATED ALWAYS AS (IFNULL(group_id, 0)) STORED,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL,
    FOREIGN KEY (cancelled_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (corrected_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_date_group (user_id, work_date, group_id_key),
    INDEX idx_user_id (user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- attendance_breaks テーブル（休憩情報）
-- 1日の勤怠に対して複数回の休憩を記録できる
CREATE TABLE IF NOT EXISTS attendance_breaks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    attendance_id INT NOT NULL,
    break_start TIME NOT NULL,
    break_end TIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    INDEX idx_attendance_id (attendance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- group_messages テーブル（グループチャット投稿）
CREATE TABLE IF NOT EXISTS group_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_group_id_created_at (group_id, created_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- group_message_attachments テーブル（チャット添付ファイル）
-- 画像/PDF/動画/音楽などを保存（実体ファイルはサーバ側に保存し、ここはメタ情報）
CREATE TABLE IF NOT EXISTS group_message_attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message_id INT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES group_messages(id) ON DELETE CASCADE,
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- remember_tokens テーブル（ログイン状態保持）
CREATE TABLE IF NOT EXISTS remember_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_token_hash (token_hash),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- テスト用ユーザー（パスワード: test123）
-- パスワードは平文で保存（本来はハッシュ化推奨）
INSERT INTO users (login_id, password_hash, name) VALUES
('test_user', 'test123', 'テストユーザー'),
('admin_user', 'admin123', '管理者ユーザー'),
('member1', 'member123', 'メンバー1'),
('member2', 'member123', 'メンバー2'),
('member3', 'member123', 'メンバー3'),
('member4', 'member123', 'メンバー4'),
('manager1', 'manager123', 'マネージャー1')
ON DUPLICATE KEY UPDATE name=name;

-- テスト用グループ
INSERT INTO groups (name, description, admin_user_id) VALUES
('テストグループ1', 'テスト用のグループ1です', 2),
('テストグループ2', 'テスト用のグループ2です', 2),
('開発チーム', '開発部門のグループです', 7),
('営業チーム', '営業部門のグループです', 2)
ON DUPLICATE KEY UPDATE name=name;

-- テスト用グループメンバー
INSERT INTO group_members (group_id, user_id) VALUES
(1, 2), -- admin_user がグループ1の管理者兼メンバー
(1, 3), -- member1 がグループ1のメンバー
(1, 4), -- member2 がグループ1のメンバー
(1, 5), -- member3 がグループ1のメンバー
(2, 2), -- admin_user がグループ2の管理者兼メンバー
(2, 3), -- member1 がグループ2のメンバー
(2, 6), -- member4 がグループ2のメンバー
(3, 7), -- manager1 が開発チームの管理者兼メンバー
(3, 3), -- member1 が開発チームのメンバー
(3, 4), -- member2 が開発チームのメンバー
(3, 5), -- member3 が開発チームのメンバー
(4, 2), -- admin_user が営業チームの管理者兼メンバー
(4, 6)  -- member4 が営業チームのメンバー
ON DUPLICATE KEY UPDATE group_id=group_id;

-- テスト用勤怠データ（今日のデータ）
INSERT INTO attendance (user_id, group_id, work_date, start_time, end_time) VALUES
(1, NULL, CURDATE(), '09:00:00', '18:00:00'), -- test_user の今日の勤怠（グループなし）
(2, 1, CURDATE(), '09:00:00', '18:00:00'),    -- admin_user の今日の勤怠（グループ1）
(3, 1, CURDATE(), '09:30:00', '18:30:00'),    -- member1 の今日の勤怠（グループ1）
(4, 1, CURDATE(), '10:00:00', '19:00:00'),    -- member2 の今日の勤怠（グループ1）
(2, 2, CURDATE(), '09:00:00', NULL),          -- admin_user の今日の勤怠（グループ2、退勤未登録）
(3, 2, CURDATE(), NULL, NULL)                 -- member1 の今日の勤怠（グループ2、出退勤未登録）
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- テスト用勤怠データ（過去1週間分）
INSERT INTO attendance (user_id, group_id, work_date, start_time, end_time) VALUES
-- 1日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:15:00', '18:15:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:30:00', '18:30:00'),
(5, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', '19:00:00'),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '17:30:00'),
(3, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:20:00', '18:20:00'),
(7, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:30:00', '17:30:00'),
-- 2日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00', '18:30:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:00:00', '19:00:00'),
(5, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:45:00', '18:45:00'),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', NULL), -- 退勤未登録
(7, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '08:30:00', '17:30:00'),
-- 3日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:30:00', '18:30:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '10:00:00', '19:00:00'),
(5, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:15:00', '18:15:00'),
(3, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:20:00', '18:20:00'),
(6, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:10:00', '18:10:00'),
(7, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '08:30:00', '17:30:00'),
(3, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00', '18:00:00'),
-- 4日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:30:00', '18:30:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '10:00:00', '19:00:00'),
(5, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:45:00', '18:45:00'),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:00:00', '17:30:00'),
(7, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:30:00', '17:30:00'),
-- 5日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:30:00', '18:30:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00:00', '19:00:00'),
(3, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:20:00', '18:20:00'),
(6, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:10:00', '18:10:00'),
(7, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '08:30:00', '17:30:00'),
(3, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', '18:00:00'),
(4, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:15:00', '18:15:00'),
-- 6日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:30:00', '18:30:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '10:00:00', '19:00:00'),
(5, 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:45:00', '18:45:00'),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:00:00', '17:30:00'),
(7, 3, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '08:30:00', '17:30:00'),
(3, 3, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:00:00', '18:00:00'),
-- 7日前
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00', '18:00:00'),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00', '18:00:00'),
(3, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:30:00', '18:30:00'),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '10:00:00', '19:00:00'),
(3, 2, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:20:00', '18:20:00'),
(7, 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '08:30:00', '17:30:00'),
(3, 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00', '18:00:00'),
(4, 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:15:00', '18:15:00'),
(5, 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:30:00', '18:30:00')
ON DUPLICATE KEY UPDATE start_time=VALUES(start_time), end_time=VALUES(end_time);

-- テスト用勤怠データ（修正済みのデータ）
INSERT INTO attendance (user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, is_corrected, corrected_by_admin, corrected_by_user_id, corrected_at) VALUES
-- 一般ユーザーが修正したデータ（管理者修正ではない）
(3, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:45:00', '18:45:00', '09:30:00', '18:30:00', 1, 0, 3, NOW()),
(4, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:15:00', '19:15:00', '10:00:00', '19:00:00', 1, 0, 4, NOW()),
-- 管理者が修正したデータ
(5, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:20:00', '18:20:00', '09:45:00', '18:45:00', 1, 1, 2, NOW()),
(6, 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '09:05:00', '18:05:00', '09:10:00', '18:10:00', 1, 1, 2, NOW())
ON DUPLICATE KEY UPDATE 
    start_time=VALUES(start_time), 
    end_time=VALUES(end_time),
    prev_start_time=VALUES(prev_start_time),
    prev_end_time=VALUES(prev_end_time),
    is_corrected=VALUES(is_corrected),
    corrected_by_admin=VALUES(corrected_by_admin),
    corrected_by_user_id=VALUES(corrected_by_user_id),
    corrected_at=VALUES(corrected_at);

-- テスト用勤怠データ（取り消し済みのデータ）
INSERT INTO attendance (user_id, group_id, work_date, start_time, end_time, is_cancelled, cancelled_by_admin, cancelled_by_user_id, cancelled_at) VALUES
-- 一般ユーザーが取り消したデータ
(5, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, NULL, 1, 0, 5, NOW()),
(6, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, NULL, 1, 0, 6, NOW()),
-- 管理者が取り消したデータ
(4, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), NULL, NULL, 1, 1, 2, NOW())
ON DUPLICATE KEY UPDATE 
    start_time=VALUES(start_time),
    end_time=VALUES(end_time),
    is_cancelled=VALUES(is_cancelled),
    cancelled_by_admin=VALUES(cancelled_by_admin),
    cancelled_by_user_id=VALUES(cancelled_by_user_id),
    cancelled_at=VALUES(cancelled_at);

-- テスト用休憩データ
-- 注意: attendance_idは実際のattendanceレコードのIDに依存するため、後で更新が必要な場合があります
INSERT INTO attendance_breaks (attendance_id, break_start, break_end) 
SELECT 
    a.id,
    '12:00:00' AS break_start,
    '13:00:00' AS break_end
FROM attendance a
WHERE a.user_id = 1 AND a.work_date = CURDATE()
LIMIT 1
ON DUPLICATE KEY UPDATE break_start=VALUES(break_start), break_end=VALUES(break_end);

INSERT INTO attendance_breaks (attendance_id, break_start, break_end) 
SELECT 
    a.id,
    '12:00:00' AS break_start,
    '13:00:00' AS break_end
FROM attendance a
WHERE a.user_id = 2 AND a.group_id = 1 AND a.work_date = CURDATE()
LIMIT 1
ON DUPLICATE KEY UPDATE break_start=VALUES(break_start), break_end=VALUES(break_end);

INSERT INTO attendance_breaks (attendance_id, break_start, break_end) 
SELECT 
    a.id,
    '12:30:00' AS break_start,
    '13:30:00' AS break_end
FROM attendance a
WHERE a.user_id = 3 AND a.group_id = 1 AND a.work_date = CURDATE()
LIMIT 1
ON DUPLICATE KEY UPDATE break_start=VALUES(break_start), break_end=VALUES(break_end);

-- 複数回の休憩があるケース
INSERT INTO attendance_breaks (attendance_id, break_start, break_end) 
SELECT 
    a.id,
    '12:00:00' AS break_start,
    '13:00:00' AS break_end
FROM attendance a
WHERE a.user_id = 4 AND a.group_id = 1 AND a.work_date = CURDATE()
LIMIT 1
ON DUPLICATE KEY UPDATE break_start=VALUES(break_start), break_end=VALUES(break_end);

INSERT INTO attendance_breaks (attendance_id, break_start, break_end) 
SELECT 
    a.id,
    '15:00:00' AS break_start,
    '15:15:00' AS break_end
FROM attendance a
WHERE a.user_id = 4 AND a.group_id = 1 AND a.work_date = CURDATE()
LIMIT 1
ON DUPLICATE KEY UPDATE break_start=VALUES(break_start), break_end=VALUES(break_end);

