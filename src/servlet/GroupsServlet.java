package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.GroupDAO;
import model.Group;
import model.User;
import util.AuthUtil;

/**
 * グループ一覧ページ
 * - 自分が管理している/参加しているグループをまとめて表示
 */
@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        List<Group> adminGroups = groupDAO.findGroupsByAdmin(loginUser.getId());
        List<Group> memberGroupsRaw = groupDAO.findGroupsByMember(loginUser.getId());

        // adminとmemberが重複する場合（管理者は自動でメンバーに入るが念のため）
        Set<Integer> adminIds = new HashSet<>();
        if (adminGroups != null) {
            for (Group g : adminGroups) adminIds.add(g.getId());
        }
        if (memberGroupsRaw != null && !adminIds.isEmpty()) {
            memberGroupsRaw.removeIf(g -> adminIds.contains(g.getId()));
        }

        request.setAttribute("adminGroups", adminGroups);
        request.setAttribute("memberGroups", memberGroupsRaw);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/groups.jsp");
        dispatcher.forward(request, response);
    }
}


