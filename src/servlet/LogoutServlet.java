package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ログアウト処理サーブレット
 * GET: セッションを破棄してログイン画面にリダイレクト
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // セッションを無効化
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // /loginにリダイレクト
        response.sendRedirect(request.getContextPath() + "/login");
    }
}

