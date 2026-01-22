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

/**
 * DM送信先候補（ID検索）
 * - 受信側がプロフィールでDM許可（dm_allowed=1）しているユーザーのみ返す
 * GET /api/users/dm_suggest?q=ab
 */
@WebServlet("/api/users/dm_suggest")
public class DmUserSuggestServlet extends HttpServlet {

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

        String q = request.getParameter("q");
        if (q == null) q = "";
        q = q.trim();
        if (q.isEmpty()) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("[]");
            return;
        }

        // JDBCドライバをロード（DBUtilのstaticでロードされるが念のため）
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String like = "%" + q + "%";
        String sql = "SELECT id, login_id, name "
                   + "FROM users "
                   + "WHERE is_deleted = 0 AND dm_allowed = 1 AND id <> ? AND login_id LIKE ? "
                   + "ORDER BY (login_id = ?) DESC, login_id ASC "
                   + "LIMIT 10";

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loginUser.getId());
            ps.setString(2, like);
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{")
                      .append("\"id\":").append(rs.getInt("id")).append(",")
                      .append("\"loginId\":\"").append(jsonEscape(rs.getString("login_id"))).append("\",")
                      .append("\"name\":\"").append(jsonEscape(rs.getString("name"))).append("\"")
                      .append("}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sb.append("]");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(sb.toString());
    }
}


