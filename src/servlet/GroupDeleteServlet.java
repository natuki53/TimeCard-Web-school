package servlet;

import dao.GroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.AuthUtil;

import java.io.IOException;

/**
 * グループ削除（論理削除）
 */
@WebServlet("/group/delete")
public class GroupDeleteServlet extends HttpServlet {

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer groupId = toIntOrNull(request.getParameter("groupId"));
        if (groupId == null || groupId <= 0) {
            session.setAttribute("errorMessage", "無効なグループIDです。");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        if (!groupDAO.isGroupAdmin(groupId, loginUser.getId())) {
            session.setAttribute("errorMessage", "このグループの管理権限がありません。");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        boolean ok = groupDAO.softDeleteGroup(groupId, loginUser.getId());
        session.setAttribute("successMessage", ok ? "グループを削除しました（非表示）。" : "グループ削除に失敗しました。");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}


