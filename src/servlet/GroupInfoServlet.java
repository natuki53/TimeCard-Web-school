package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import dao.GroupDAO;
import model.Group;
import model.GroupMember;
import model.User;
import util.AuthUtil;

/**
 * グループ詳細表示サーブレット
 * 参加しているグループの詳細情報とメンバー一覧を表示
 */
@WebServlet("/group/info")
public class GroupInfoServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // セッションからログインユーザーを取得
        User loginUser = AuthUtil.getLoginUser(request);
        
        if (loginUser == null) {
            // ログインしていない場合はログイン画面へリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // グループIDを取得
        String groupIdStr = request.getParameter("id");
        if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        GroupDAO groupDAO = new GroupDAO();
        
        // グループ情報を取得
        Group group = groupDAO.findGroupById(groupId);
        if (group == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // アクセス権限チェック（メンバーまたは管理者のみ）
        boolean isAdmin = groupDAO.isGroupAdmin(groupId, loginUser.getId());
        boolean isMember = groupDAO.isGroupMember(groupId, loginUser.getId());
        
        if (!isAdmin && !isMember) {
            request.setAttribute("errorMessage", "このグループの閲覧権限がありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
        // グループメンバー一覧を取得
        List<GroupMember> members = groupDAO.findGroupMembers(groupId);
        
        // リクエストスコープに設定
        request.setAttribute("group", group);
        request.setAttribute("members", members);
        request.setAttribute("isAdmin", isAdmin);
        request.setAttribute("isMember", isMember);
        
        // グループ詳細画面を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_info.jsp");
        dispatcher.forward(request, response);
    }
}
