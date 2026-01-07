package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import dao.GroupDAO;
import dao.UserDAO;
import model.User;
import util.AuthUtil;

/**
 * ユーザー候補検索API（グループ管理のメンバー追加用）
 * GET /api/users/suggest?groupId=1&q=ab
 *
 * - ログイン必須
 * - 指定グループの管理者のみ利用可能
 * - 既にグループメンバーのユーザーは除外
 */
@WebServlet("/api/users/suggest")
public class UserSuggestServlet extends HttpServlet {

    private static Integer toIntOrNull(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }

        Integer groupId = toIntOrNull(request.getParameter("groupId"));
        String q = request.getParameter("q");

        if (groupId == null || groupId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\":\"invalid_group\"}");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        if (!groupDAO.isGroupAdmin(groupId, loginUser.getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\":\"forbidden\"}");
            return;
        }

        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.findSuggestionsByLoginId(q, groupId, 10);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        try (PrintWriter out = response.getWriter()) {
            out.write("[");
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (i > 0) out.write(",");
                out.write("{");
                out.write("\"id\":" + u.getId() + ",");
                out.write("\"loginId\":\"" + jsonEscape(u.getLoginId()) + "\",");
                out.write("\"name\":\"" + jsonEscape(u.getName()) + "\"");
                out.write("}");
            }
            out.write("]");
        }
    }
}


