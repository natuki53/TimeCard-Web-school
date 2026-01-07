package dao;

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

import model.GroupAttachment;
import model.GroupMessage;

import util.DBUtil;

/**
 * グループチャットDAO
 */
public class GroupChatDAO {

    public GroupChatDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
    }

    /**
     * 最新メッセージを取得（古い→新しい順で返す）
     */
    public List<GroupMessage> findRecentMessages(int groupId, int limit) {
        List<GroupMessage> messages = new ArrayList<>();
        if (limit <= 0) limit = 200;

        String msgSql = "SELECT m.id, m.group_id, m.user_id, m.content, m.created_at, "
                      + "u.name AS user_name, u.login_id AS user_login_id "
                      + "FROM group_messages m "
                      + "INNER JOIN users u ON u.id = m.user_id "
                      + "WHERE m.group_id = ? "
                      + "ORDER BY m.created_at DESC, m.id DESC "
                      + "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(msgSql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupMessage m = new GroupMessage();
                    m.setId(rs.getInt("id"));
                    m.setGroupId(rs.getInt("group_id"));
                    m.setUserId(rs.getInt("user_id"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    m.setCreatedAt(ts != null ? ts.toLocalDateTime() : (LocalDateTime) null);
                    m.setUserName(rs.getString("user_name"));
                    m.setUserLoginId(rs.getString("user_login_id"));
                    messages.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return messages;
        }

        // 取得はDESCなので反転して「古い→新しい」に
        java.util.Collections.reverse(messages);

        // 添付をまとめて取得して詰める
        if (!messages.isEmpty()) {
            Map<Integer, List<GroupAttachment>> byMessageId = findAttachmentsByMessageIds(messages);
            for (GroupMessage m : messages) {
                List<GroupAttachment> atts = byMessageId.get(m.getId());
                if (atts != null) m.setAttachments(atts);
            }
        }

        return messages;
    }

    /**
     * 指定IDより新しいメッセージを取得（古い→新しい順）
     */
    public List<GroupMessage> findMessagesAfterId(int groupId, int afterId, int limit) {
        List<GroupMessage> messages = new ArrayList<>();
        if (limit <= 0) limit = 50;
        if (afterId < 0) afterId = 0;

        String msgSql = "SELECT m.id, m.group_id, m.user_id, m.content, m.created_at, "
                      + "u.name AS user_name, u.login_id AS user_login_id "
                      + "FROM group_messages m "
                      + "INNER JOIN users u ON u.id = m.user_id "
                      + "WHERE m.group_id = ? AND m.id > ? "
                      + "ORDER BY m.id ASC "
                      + "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(msgSql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, afterId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupMessage m = new GroupMessage();
                    m.setId(rs.getInt("id"));
                    m.setGroupId(rs.getInt("group_id"));
                    m.setUserId(rs.getInt("user_id"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    m.setCreatedAt(ts != null ? ts.toLocalDateTime() : (LocalDateTime) null);
                    m.setUserName(rs.getString("user_name"));
                    m.setUserLoginId(rs.getString("user_login_id"));
                    messages.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return messages;
        }

        if (!messages.isEmpty()) {
            Map<Integer, List<GroupAttachment>> byMessageId = findAttachmentsByMessageIds(messages);
            for (GroupMessage m : messages) {
                List<GroupAttachment> atts = byMessageId.get(m.getId());
                if (atts != null) m.setAttachments(atts);
            }
        }

        return messages;
    }

    private Map<Integer, List<GroupAttachment>> findAttachmentsByMessageIds(List<GroupMessage> messages) {
        Map<Integer, List<GroupAttachment>> map = new HashMap<>();
        if (messages == null || messages.isEmpty()) return map;

        StringBuilder in = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) in.append(",");
            in.append("?");
        }

        String sql = "SELECT a.id, a.message_id, a.original_filename, a.stored_filename, a.mime_type, a.size_bytes, a.created_at, "
                   + "m.group_id "
                   + "FROM group_message_attachments a "
                   + "INNER JOIN group_messages m ON m.id = a.message_id "
                   + "WHERE a.message_id IN (" + in + ") "
                   + "ORDER BY a.id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < messages.size(); i++) {
                ps.setInt(i + 1, messages.get(i).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupAttachment a = new GroupAttachment();
                    a.setId(rs.getInt("id"));
                    a.setMessageId(rs.getInt("message_id"));
                    a.setGroupId(rs.getInt("group_id"));
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

    /**
     * メッセージを作成し、生成IDを返す
     */
    public Integer insertMessage(int groupId, int userId, String content) {
        String sql = "INSERT INTO group_messages(group_id, user_id, content) VALUES(?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            int r = ps.executeUpdate();
            if (r <= 0) return null;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertAttachment(int messageId, String originalFilename, String storedFilename, String mimeType, long sizeBytes) {
        String sql = "INSERT INTO group_message_attachments(message_id, original_filename, stored_filename, mime_type, size_bytes) "
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
        }
        return false;
    }

    /**
     * 添付IDから添付情報を取得（グループID含む）
     */
    public GroupAttachment findAttachmentById(int attachmentId) {
        String sql = "SELECT a.id, a.message_id, a.original_filename, a.stored_filename, a.mime_type, a.size_bytes, a.created_at, "
                   + "m.group_id "
                   + "FROM group_message_attachments a "
                   + "INNER JOIN group_messages m ON m.id = a.message_id "
                   + "WHERE a.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attachmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GroupAttachment a = new GroupAttachment();
                    a.setId(rs.getInt("id"));
                    a.setMessageId(rs.getInt("message_id"));
                    a.setGroupId(rs.getInt("group_id"));
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
}


