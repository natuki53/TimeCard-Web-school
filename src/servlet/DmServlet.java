package servlet;

import dao.DmDAO;
import dao.GroupDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DmThread;
import model.Group;
import model.User;
import util.AuthUtil;

import java.io.IOException;
import java.util.List;

/**
 * DMトップ（スレッド一覧 + 新規DM開始）
 */
@WebServlet("/dm")
public class DmServlet extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        DmDAO dmDAO = new DmDAO();
        List<DmThread> threads = dmDAO.findThreadsByUser(loginUser.getId(), 100);
        request.setAttribute("threads", threads);

        GroupDAO groupDAO = new GroupDAO();
        List<Group> groups = groupDAO.findGroupsByUser(loginUser.getId());
        request.setAttribute("groups", groups);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dm.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer targetUserId = toIntOrNull(request.getParameter("targetUserId"));
        if (targetUserId == null || targetUserId <= 0 || targetUserId == loginUser.getId()) {
            response.sendRedirect(request.getContextPath() + "/dm");
            return;
        }

        DmDAO dmDAO = new DmDAO();
        Integer threadId = dmDAO.findOrCreateThread(loginUser.getId(), targetUserId);
        if (threadId == null) {
            response.sendRedirect(request.getContextPath() + "/dm");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dm/chat?id=" + threadId);
    }
}


