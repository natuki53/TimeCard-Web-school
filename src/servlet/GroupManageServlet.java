package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import dao.GroupDAO;
import dao.UserDAO;
import model.Group;
import model.GroupMember;
import model.User;

/**
 * グループ管理サーブレット
 * GET: グループ管理画面表示
 * POST: メンバー追加・削除処理
 */
@WebServlet("/group/manage")
public class GroupManageServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // セッションからログインユーザーを取得
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        
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
        
        // 管理者権限チェック
        if (!groupDAO.isGroupAdmin(groupId, loginUser.getId())) {
            request.setAttribute("errorMessage", "このグループの管理権限がありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
        // グループメンバー一覧を取得
        List<GroupMember> members = groupDAO.findGroupMembers(groupId);
        
        // セッションからメッセージを取得
        String successMessage = (String) session.getAttribute("successMessage");
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        
        // リクエストスコープに設定
        request.setAttribute("group", group);
        request.setAttribute("members", members);
        
        // グループ管理画面を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_manage.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // セッションからログインユーザーを取得
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            // ログインしていない場合はログイン画面へリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // パラメータを取得
        String action = request.getParameter("action");
        String groupIdStr = request.getParameter("groupId");
        
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
        
        // 管理者権限チェック
        if (!groupDAO.isGroupAdmin(groupId, loginUser.getId())) {
            session.setAttribute("errorMessage", "このグループの管理権限がありません。");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        if ("addMember".equals(action)) {
            // メンバー追加処理
            String loginId = request.getParameter("loginId");
            
            if (loginId == null || loginId.trim().isEmpty()) {
                session.setAttribute("errorMessage", "ログインIDを入力してください。");
            } else {
                UserDAO userDAO = new UserDAO();
                User targetUser = userDAO.findByLoginId(loginId.trim());
                
                if (targetUser == null) {
                    session.setAttribute("errorMessage", "ユーザーが見つかりません。");
                } else if (groupDAO.isGroupMember(groupId, targetUser.getId())) {
                    session.setAttribute("errorMessage", "このユーザーは既にグループに参加しています。");
                } else {
                    boolean success = groupDAO.addGroupMember(groupId, targetUser.getId());
                    if (success) {
                        session.setAttribute("successMessage", targetUser.getName() + "さんをグループに追加しました。");
                    } else {
                        session.setAttribute("errorMessage", "メンバーの追加に失敗しました。");
                    }
                }
            }
            
        } else if ("removeMember".equals(action)) {
            // メンバー削除処理
            String userIdStr = request.getParameter("userId");
            
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    
                    // 管理者自身は削除できない
                    if (userId == loginUser.getId()) {
                        session.setAttribute("errorMessage", "管理者自身をグループから削除することはできません。");
                    } else {
                        boolean success = groupDAO.removeGroupMember(groupId, userId);
                        if (success) {
                            session.setAttribute("successMessage", "メンバーをグループから削除しました。");
                        } else {
                            session.setAttribute("errorMessage", "メンバーの削除に失敗しました。");
                        }
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("errorMessage", "無効なユーザーIDです。");
                }
            }
        }
        
        // グループ管理画面にリダイレクト
        response.sendRedirect(request.getContextPath() + "/group/manage?id=" + groupId);
    }
}
