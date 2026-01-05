package servlet;

import dao.GroupChatDAO;
import dao.GroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.GroupAttachment;
import model.GroupMessage;
import model.User;
import util.AuthUtil;
import util.DBUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * グループチャット：新着取得API（ポーリング用）
 * GET /api/group/chat/messages?groupId=1&afterId=123
 */
@WebServlet("/api/group/chat/messages")
public class GroupChatMessagesApiServlet extends HttpServlet {
    private static Integer toIntOrNull(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer groupId = toIntOrNull(request.getParameter("groupId"));
        Integer afterId = toIntOrNull(request.getParameter("afterId"));
        if (groupId == null || groupId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int a = (afterId == null) ? 0 : Math.max(0, afterId);

        GroupDAO groupDAO = new GroupDAO();
        boolean allowed = groupDAO.isGroupAdmin(groupId, loginUser.getId()) || groupDAO.isGroupMember(groupId, loginUser.getId());
        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        GroupChatDAO chatDAO = new GroupChatDAO();
        List<GroupMessage> messages = chatDAO.findMessagesAfterId(groupId, a, 50);

        // 画面を見ている間は既読を進める（通知の重複防止）
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO group_last_read(user_id, group_id, last_read_at) VALUES(?, ?, NOW()) " +
                             "ON DUPLICATE KEY UPDATE last_read_at = NOW()"
             )) {
            ps.setInt(1, loginUser.getId());
            ps.setInt(2, groupId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // ignore
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < messages.size(); i++) {
            GroupMessage m = messages.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"id\":").append(m.getId()).append(",")
              .append("\"userName\":\"").append(jsonEscape(m.getUserName())).append("\",")
              .append("\"userLoginId\":\"").append(jsonEscape(m.getUserLoginId())).append("\",")
              .append("\"createdAt\":\"").append(jsonEscape(m.getCreatedAt() != null ? m.getCreatedAt().toString() : "")).append("\",")
              .append("\"content\":\"").append(jsonEscape(m.getContent() != null ? m.getContent() : "")).append("\",")
              .append("\"attachments\":[");

            List<GroupAttachment> atts = m.getAttachments();
            if (atts != null) {
                for (int j = 0; j < atts.size(); j++) {
                    GroupAttachment a1 = atts.get(j);
                    if (j > 0) sb.append(",");
                    String fileUrl = request.getContextPath() + "/group/chat/file?id=" + a1.getId();
                    sb.append("{")
                      .append("\"id\":").append(a1.getId()).append(",")
                      .append("\"originalFileName\":\"").append(jsonEscape(a1.getOriginalFileName())).append("\",")
                      .append("\"mimeType\":\"").append(jsonEscape(a1.getMimeType())).append("\",")
                      .append("\"sizeBytes\":").append(a1.getSizeBytes()).append(",")
                      .append("\"url\":\"").append(jsonEscape(fileUrl)).append("\"")
                      .append("}");
                }
            }
            sb.append("]}");
        }
        sb.append("]");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(sb.toString());
    }
}


