package servlet;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.AuthUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * メンバープロフィール取得API（JSON）
 */
@WebServlet("/api/user/profile")
public class UserProfileApiServlet extends HttpServlet {

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

        Integer id = toIntOrNull(request.getParameter("id"));
        if (id == null || id <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User u = userDAO.findPublicProfileById(id);
        if (u == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String iconUrl = request.getContextPath() + "/user/avatar?id=" + u.getId() + "&v=" + System.currentTimeMillis();
        String json = "{"
                + "\"id\":" + u.getId() + ","
                + "\"loginId\":\"" + jsonEscape(u.getLoginId()) + "\","
                + "\"name\":\"" + jsonEscape(u.getName()) + "\","
                + "\"bio\":\"" + jsonEscape(u.getBio()) + "\","
                + "\"iconUrl\":\"" + jsonEscape(iconUrl) + "\""
                + "}";

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(json);
    }
}


