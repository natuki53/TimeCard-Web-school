package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.AuthUtil;
import util.DBUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知API（グループチャット + DM）
 * GET /api/notifications
 */
@WebServlet("/api/notifications")
public class NotificationsApiServlet extends HttpServlet {

    private static class Item {
        String kind; // group | dm
        int groupId;
        int threadId;
        String title;
        String from;
        String iconUrl;
        String preview;
        String url;
        Timestamp createdAt;
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String trimPreview(String s, int max) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s+", " ");
        if (t.length() <= max) return t;
        return t.substring(0, Math.max(0, max - 1)) + "…";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = loginUser.getId();

        // DMも初回大量通知を避けるため last_read を作成
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO dm_last_read(user_id, thread_id, last_read_at) " +
                     "SELECT ?, t.id, NOW() FROM dm_threads t WHERE t.user1_id = ? OR t.user2_id = ?"
             )) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 初回アクセスで「昔の投稿が大量通知」にならないよう、所属グループ分の last_read を作成しておく
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO group_last_read(user_id, group_id, last_read_at) " +
                     "SELECT ?, gm.group_id, NOW() FROM group_members gm WHERE gm.user_id = ?"
             )) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Item> items = new ArrayList<>();

        // グループ通知（新着）
        String groupSql =
                "SELECT m.id AS msg_id, m.group_id, g.name AS group_name, m.user_id AS sender_id, u.name AS sender_name, " +
                "m.content, m.created_at, " +
                "(SELECT COUNT(*) FROM group_message_attachments a WHERE a.message_id = m.id) AS att_count, " +
                "glr.last_read_at " +
                "FROM group_members gm " +
                "INNER JOIN group_messages m ON m.group_id = gm.group_id " +
                "INNER JOIN `groups` g ON g.id = m.group_id " +
                "INNER JOIN users u ON u.id = m.user_id " +
                "INNER JOIN group_last_read glr ON glr.user_id = gm.user_id AND glr.group_id = gm.group_id " +
                "WHERE gm.user_id = ? AND m.user_id <> ? AND m.created_at > glr.last_read_at " +
                "ORDER BY m.created_at DESC, m.id DESC " +
                "LIMIT 10";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(groupSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item it = new Item();
                    it.kind = "group";
                    it.groupId = rs.getInt("group_id");
                    it.title = rs.getString("group_name");
                    it.from = rs.getString("sender_name");
                    // グループ通知は固定アイコン（グループ名の区別はtitleで出す）
                    it.iconUrl = request.getContextPath() + "/img/icon_black.png";
                    String c = rs.getString("content");
                    int att = rs.getInt("att_count");
                    if ((c == null || c.trim().isEmpty()) && att > 0) c = "[添付]";
                    it.preview = trimPreview(c, 40);
                    it.createdAt = rs.getTimestamp("created_at");
                    it.url = request.getContextPath() + "/group/chat?id=" + it.groupId;
                    items.add(it);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // DM通知（新着）
        String dmSql =
                "SELECT m.id AS msg_id, m.thread_id, m.sender_user_id, su.name AS sender_name, m.content, m.created_at, " +
                "  (SELECT COUNT(*) FROM dm_message_attachments a WHERE a.message_id = m.id) AS att_count, " +
                "  CASE WHEN t.user1_id = ? THEN t.user2_id ELSE t.user1_id END AS other_user_id, " +
                "  ou.name AS other_name, " +
                "  dlr.last_read_at " +
                "FROM dm_threads t " +
                "INNER JOIN dm_last_read dlr ON dlr.user_id = ? AND dlr.thread_id = t.id " +
                "INNER JOIN dm_messages m ON m.thread_id = t.id " +
                "INNER JOIN users su ON su.id = m.sender_user_id " +
                "INNER JOIN users ou ON ou.id = (CASE WHEN t.user1_id = ? THEN t.user2_id ELSE t.user1_id END) " +
                "WHERE (t.user1_id = ? OR t.user2_id = ?) " +
                "  AND m.sender_user_id <> ? " +
                "  AND m.created_at > dlr.last_read_at " +
                "ORDER BY m.created_at DESC, m.id DESC " +
                "LIMIT 10";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(dmSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, userId);
            ps.setInt(6, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item it = new Item();
                    it.kind = "dm";
                    it.threadId = rs.getInt("thread_id");
                    it.title = "DM";
                    it.from = rs.getString("sender_name");
                    // DM通知は送信者アイコン
                    int senderId = rs.getInt("sender_user_id");
                    it.iconUrl = request.getContextPath() + "/user/avatar?id=" + senderId;
                    String c = rs.getString("content");
                    int att = rs.getInt("att_count");
                    if ((c == null || c.trim().isEmpty()) && att > 0) c = "[添付]";
                    it.preview = trimPreview(c, 40);
                    it.createdAt = rs.getTimestamp("created_at");
                    it.url = request.getContextPath() + "/dm/chat?id=" + it.threadId;
                    items.add(it);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 新しい順にして上位だけ
        items.sort(Comparator.comparing((Item i) -> i.createdAt).reversed());
        if (items.size() > 5) items = new ArrayList<>(items.subList(0, 5));

        // 重複通知防止：配信した分は last_read を進める（GREATEST）
        Map<Integer, Timestamp> maxGroup = new HashMap<>();
        Map<Integer, Timestamp> maxThread = new HashMap<>();
        for (Item it : items) {
            if (it.createdAt == null) continue;
            if ("group".equals(it.kind)) {
                Timestamp cur = maxGroup.get(it.groupId);
                if (cur == null || it.createdAt.after(cur)) maxGroup.put(it.groupId, it.createdAt);
            } else if ("dm".equals(it.kind)) {
                Timestamp cur = maxThread.get(it.threadId);
                if (cur == null || it.createdAt.after(cur)) maxThread.put(it.threadId, it.createdAt);
            }
        }

        try (Connection conn = DBUtil.getConnection()) {
            if (!maxGroup.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO group_last_read(user_id, group_id, last_read_at) VALUES(?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE last_read_at = GREATEST(last_read_at, VALUES(last_read_at))"
                )) {
                    for (Map.Entry<Integer, Timestamp> e : maxGroup.entrySet()) {
                        ps.setInt(1, userId);
                        ps.setInt(2, e.getKey());
                        ps.setTimestamp(3, e.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            if (!maxThread.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dm_last_read(user_id, thread_id, last_read_at) VALUES(?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE last_read_at = GREATEST(last_read_at, VALUES(last_read_at))"
                )) {
                    for (Map.Entry<Integer, Timestamp> e : maxThread.entrySet()) {
                        ps.setInt(1, userId);
                        ps.setInt(2, e.getKey());
                        ps.setTimestamp(3, e.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // JSON返却
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"kind\":\"").append(jsonEscape(it.kind)).append("\",")
              .append("\"title\":\"").append(jsonEscape(it.title)).append("\",")
              .append("\"from\":\"").append(jsonEscape(it.from)).append("\",")
              .append("\"iconUrl\":\"").append(jsonEscape(it.iconUrl)).append("\",")
              .append("\"preview\":\"").append(jsonEscape(it.preview)).append("\",")
              .append("\"url\":\"").append(jsonEscape(it.url)).append("\"")
              .append("}");
        }
        sb.append("]");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(sb.toString());
    }
}


