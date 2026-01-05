package servlet;

import dao.GroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.GroupMember;
import model.User;
import util.AuthUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * グループメンバー取得API（DM開始用）
 * GET /api/group/members?groupId=1
 */
@WebServlet("/api/group/members")
public class GroupMembersApiServlet extends HttpServlet {
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
        if (groupId == null || groupId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        if (!groupDAO.isGroupMember(groupId, loginUser.getId()) && !groupDAO.isGroupAdmin(groupId, loginUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<GroupMember> members = groupDAO.findGroupMembers(groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (GroupMember m : members) {
            if (m.getUserId() == loginUser.getId()) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append("\"id\":").append(m.getUserId()).append(",")
              .append("\"loginId\":\"").append(jsonEscape(m.getUserLoginId())).append("\",")
              .append("\"name\":\"").append(jsonEscape(m.getUserName())).append("\"")
              .append("}");
        }
        sb.append("]");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(sb.toString());
    }
}


