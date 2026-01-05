package dao;

import model.DmMessage;
import model.DmThread;
import model.DmAttachment;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DM DAO
 */
public class DmDAO {

    public DmDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
    }

    private static int min(int a, int b) { return Math.min(a, b); }
    private static int max(int a, int b) { return Math.max(a, b); }

    /**
     * スレッドを取得（存在しなければ作成）してIDを返す
     */
    public Integer findOrCreateThread(int userA, int userB) {
        if (userA == userB) return null;
        int u1 = min(userA, userB);
        int u2 = max(userA, userB);

        String find = "SELECT id FROM dm_threads WHERE user1_id = ? AND user2_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(find)) {
            ps.setInt(1, u1);
            ps.setInt(2, u2);
            try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        ensureLastReadRow(userA, id);
                        ensureLastReadRow(userB, id);
                        return id;
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        String ins = "INSERT INTO dm_threads(user1_id, user2_id) VALUES(?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u1);
            ps.setInt(2, u2);
            int r = ps.executeUpdate();
            if (r <= 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ensureLastReadRow(userA, id);
                    ensureLastReadRow(userB, id);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            // 競合（同時作成）ならもう一度探す
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(find)) {
                ps.setInt(1, u1);
                ps.setInt(2, u2);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        ensureLastReadRow(userA, id);
                        ensureLastReadRow(userB, id);
                        return id;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ユーザーが所属するDMスレッド一覧（最新順）
     */
    public List<DmThread> findThreadsByUser(int userId, int limit) {
        List<DmThread> out = new ArrayList<>();
        if (limit <= 0) limit = 50;

        // 各スレッドの相手ユーザー + 最新メッセージ（添付のみなら "[添付]" を表示）
        String sql =
                "SELECT t.id AS thread_id, " +
                "  CASE WHEN t.user1_id = ? THEN t.user2_id ELSE t.user1_id END AS other_user_id, " +
                "  u.login_id AS other_login_id, u.name AS other_name, u.icon_filename AS other_icon_filename, " +
                "  m.content AS last_content, m.created_at AS last_created_at, " +
                "  (SELECT COUNT(*) FROM dm_message_attachments a WHERE a.message_id = m.id) AS att_count " +
                "FROM dm_threads t " +
                "INNER JOIN users u ON u.id = (CASE WHEN t.user1_id = ? THEN t.user2_id ELSE t.user1_id END) " +
                "LEFT JOIN dm_messages m ON m.id = (" +
                "  SELECT mm.id FROM dm_messages mm WHERE mm.thread_id = t.id ORDER BY mm.created_at DESC, mm.id DESC LIMIT 1" +
                ") " +
                "WHERE (t.user1_id = ? OR t.user2_id = ?) " +
                "ORDER BY (m.created_at IS NULL) ASC, m.created_at DESC, t.id DESC " +
                "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DmThread t = new DmThread();
                    t.setId(rs.getInt("thread_id"));
                    t.setOtherUserId(rs.getInt("other_user_id"));
                    t.setOtherLoginId(rs.getString("other_login_id"));
                    t.setOtherName(rs.getString("other_name"));
                    t.setOtherIconFilename(rs.getString("other_icon_filename"));
                    String c = rs.getString("last_content");
                    int att = rs.getInt("att_count");
                    if ((c == null || c.trim().isEmpty()) && att > 0) c = "[添付]";
                    t.setLastContent(c);
                    Timestamp ts = rs.getTimestamp("last_created_at");
                    t.setLastCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                    out.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }

    public boolean isUserInThread(int threadId, int userId) {
        String sql = "SELECT 1 FROM dm_threads WHERE id = ? AND (user1_id = ? OR user2_id = ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threadId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer insertMessage(int threadId, int senderUserId, String content) {
        String sql = "INSERT INTO dm_messages(thread_id, sender_user_id, content) VALUES(?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, threadId);
            ps.setInt(2, senderUserId);
            ps.setString(3, content);
            int r = ps.executeUpdate();
            if (r <= 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DmMessage> findRecentMessages(int threadId, int limit) {
        List<DmMessage> out = new ArrayList<>();
        if (limit <= 0) limit = 200;

        String sql =
                "SELECT m.id, m.thread_id, m.sender_user_id, m.content, m.created_at, " +
                "u.name AS sender_name, u.login_id AS sender_login_id " +
                "FROM dm_messages m " +
                "INNER JOIN users u ON u.id = m.sender_user_id " +
                "WHERE m.thread_id = ? " +
                "ORDER BY m.created_at DESC, m.id DESC " +
                "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threadId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DmMessage m = new DmMessage();
                    m.setId(rs.getInt("id"));
                    m.setThreadId(rs.getInt("thread_id"));
                    m.setSenderUserId(rs.getInt("sender_user_id"));
                    m.setSenderName(rs.getString("sender_name"));
                    m.setSenderLoginId(rs.getString("sender_login_id"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    m.setCreatedAt(ts != null ? ts.toLocalDateTime() : (LocalDateTime) null);
                    out.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return out;
        }

        // 添付をまとめて取得して詰める
        if (!out.isEmpty()) {
            Map<Integer, List<DmAttachment>> byMessageId = findAttachmentsByMessageIds(out);
            for (DmMessage m : out) {
                List<DmAttachment> atts = byMessageId.get(m.getId());
                if (atts != null) m.setAttachments(atts);
            }
        }

        java.util.Collections.reverse(out);
        return out;
    }

    /**
     * 指定IDより新しいDMメッセージを取得（古い→新しい順）
     */
    public List<DmMessage> findMessagesAfterId(int threadId, int afterId, int limit) {
        List<DmMessage> out = new ArrayList<>();
        if (limit <= 0) limit = 50;
        if (afterId < 0) afterId = 0;

        String sql =
                "SELECT m.id, m.thread_id, m.sender_user_id, m.content, m.created_at, " +
                "u.name AS sender_name, u.login_id AS sender_login_id " +
                "FROM dm_messages m " +
                "INNER JOIN users u ON u.id = m.sender_user_id " +
                "WHERE m.thread_id = ? AND m.id > ? " +
                "ORDER BY m.id ASC " +
                "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threadId);
            ps.setInt(2, afterId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DmMessage m = new DmMessage();
                    m.setId(rs.getInt("id"));
                    m.setThreadId(rs.getInt("thread_id"));
                    m.setSenderUserId(rs.getInt("sender_user_id"));
                    m.setSenderName(rs.getString("sender_name"));
                    m.setSenderLoginId(rs.getString("sender_login_id"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    m.setCreatedAt(ts != null ? ts.toLocalDateTime() : (LocalDateTime) null);
                    out.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return out;
        }

        if (!out.isEmpty()) {
            Map<Integer, List<DmAttachment>> byMessageId = findAttachmentsByMessageIds(out);
            for (DmMessage m : out) {
                List<DmAttachment> atts = byMessageId.get(m.getId());
                if (atts != null) m.setAttachments(atts);
            }
        }

        return out;
    }

    private Map<Integer, List<DmAttachment>> findAttachmentsByMessageIds(List<DmMessage> messages) {
        Map<Integer, List<DmAttachment>> map = new HashMap<>();
        if (messages == null || messages.isEmpty()) return map;

        StringBuilder in = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) in.append(",");
            in.append("?");
        }

        String sql = "SELECT a.id, a.message_id, a.original_filename, a.stored_filename, a.mime_type, a.size_bytes, a.created_at, "
                   + "m.thread_id "
                   + "FROM dm_message_attachments a "
                   + "INNER JOIN dm_messages m ON m.id = a.message_id "
                   + "WHERE a.message_id IN (" + in + ") "
                   + "ORDER BY a.id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < messages.size(); i++) {
                ps.setInt(i + 1, messages.get(i).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DmAttachment a = new DmAttachment();
                    a.setId(rs.getInt("id"));
                    a.setMessageId(rs.getInt("message_id"));
                    a.setThreadId(rs.getInt("thread_id"));
                    a.setOriginalFileName(rs.getString("original_filename"));
                    a.setStoredFileName(rs.getString("stored_filename"));
                    a.setMimeType(rs.getString("mime_type"));
                    a.setSizeBytes(rs.getLong("size_bytes"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    a.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

                    map.computeIfAbsent(a.getMessageId(), k -> new ArrayList<>()).add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }

    public boolean insertAttachment(int messageId, String originalFilename, String storedFilename, String mimeType, long sizeBytes) {
        String sql = "INSERT INTO dm_message_attachments(message_id, original_filename, stored_filename, mime_type, size_bytes) "
                   + "VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setString(2, originalFilename);
            ps.setString(3, storedFilename);
            ps.setString(4, mimeType);
            ps.setLong(5, sizeBytes);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DmAttachment findAttachmentById(int attachmentId) {
        String sql = "SELECT a.id, a.message_id, a.original_filename, a.stored_filename, a.mime_type, a.size_bytes, a.created_at, "
                   + "m.thread_id "
                   + "FROM dm_message_attachments a "
                   + "INNER JOIN dm_messages m ON m.id = a.message_id "
                   + "WHERE a.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attachmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DmAttachment a = new DmAttachment();
                    a.setId(rs.getInt("id"));
                    a.setMessageId(rs.getInt("message_id"));
                    a.setThreadId(rs.getInt("thread_id"));
                    a.setOriginalFileName(rs.getString("original_filename"));
                    a.setStoredFileName(rs.getString("stored_filename"));
                    a.setMimeType(rs.getString("mime_type"));
                    a.setSizeBytes(rs.getLong("size_bytes"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    a.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                    return a;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOtherName(int threadId, int userId) {
        String sql =
                "SELECT u.name " +
                "FROM dm_threads t " +
                "INNER JOIN users u ON u.id = (CASE WHEN t.user1_id = ? THEN t.user2_id ELSE t.user1_id END) " +
                "WHERE t.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, threadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean upsertLastRead(int userId, int threadId) {
        String sql =
                "INSERT INTO dm_last_read(user_id, thread_id, last_read_at) VALUES(?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE last_read_at = NOW()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, threadId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * last_read行が無い場合のみ作る（古いDMを初回に大量通知しないため）
     */
    public void ensureLastReadRow(int userId, int threadId) {
        String sql = "INSERT IGNORE INTO dm_last_read(user_id, thread_id, last_read_at) VALUES(?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, threadId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


