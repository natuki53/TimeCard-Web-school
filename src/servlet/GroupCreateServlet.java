package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import dao.GroupDAO;
import model.Group;
import model.User;
import util.AuthUtil;

/**
 * グループ作成サーブレット
 * GET: グループ作成画面表示
 * POST: グループ作成処理
 */
@WebServlet("/group/create")
public class GroupCreateServlet extends HttpServlet {
    
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
        
        // グループ作成画面を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_create.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // セッションからログインユーザーを取得
        User loginUser = AuthUtil.getLoginUser(request);
        
        if (loginUser == null) {
            // ログインしていない場合はログイン画面へリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // リクエストパラメータを取得
        String groupName = request.getParameter("groupName");
        String description = request.getParameter("description");
        
        // 入力値検証
        if (groupName == null || groupName.trim().isEmpty()) {
            request.setAttribute("errorMessage", "グループ名を入力してください。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_create.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
        // グループを作成
        GroupDAO groupDAO = new GroupDAO();
        Group group = new Group(groupName.trim(), description != null ? description.trim() : "", loginUser.getId());
        Group createdGroup = groupDAO.createGroup(group);
        
        if (createdGroup != null) {
            // 作成成功：ダッシュボードにリダイレクト
            request.getSession().setAttribute("successMessage", "グループ「" + groupName + "」を作成しました。");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // 作成失敗：エラーメッセージを設定して作成画面に戻す
            request.setAttribute("errorMessage", "グループの作成に失敗しました。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_create.jsp");
            dispatcher.forward(request, response);
        }
    }
}
