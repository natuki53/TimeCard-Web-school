package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import dao.UserDAO;
import model.User;

/**
 * ログイン処理サーブレット
 * GET: ログイン画面表示
 * POST: ログイン処理
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ログイン画面（login.jsp）を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // リクエストパラメータを取得
        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");
        
        // UserDAOで検索
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByLoginIdAndPassword(loginId, password);
        
        if (user != null) {
            // ログイン成功：セッションにユーザー情報を保存
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            
            // /attendanceにリダイレクト
            response.sendRedirect(request.getContextPath() + "/attendance");
        } else {
            // ログイン失敗：エラーメッセージを設定してlogin.jspに戻す
            request.setAttribute("errorMessage", "ログインIDまたはパスワードが正しくありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
            dispatcher.forward(request, response);
        }
    }
}

