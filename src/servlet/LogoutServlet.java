package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import dao.RememberTokenDAO;
import util.AuthUtil;

/**
 * ログアウト処理サーブレット
 * GET: セッションを破棄してログイン画面にリダイレクト
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // remember_token を失効
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (AuthUtil.REMEMBER_COOKIE_NAME.equals(c.getName())) {
                    token = c.getValue();
                    // クッキー削除
                    c.setValue("");
                    c.setMaxAge(0);
                    c.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                    response.addCookie(c);
                }
            }
        }
        if (token != null && !token.trim().isEmpty()) {
            new RememberTokenDAO().revokeToken(token);
        }

        // セッションを無効化
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // /loginにリダイレクト
        response.sendRedirect(request.getContextPath() + "/login");
    }
}

