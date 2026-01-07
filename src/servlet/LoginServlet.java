package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import dao.RememberTokenDAO;
import dao.UserDAO;
import model.User;
import util.AuthUtil;

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
        // セッションから登録成功メッセージを取得して表示
        HttpSession session = request.getSession();
        String registerSuccess = (String) session.getAttribute("registerSuccess");
        if (registerSuccess != null) {
            request.setAttribute("msg", registerSuccess);
            session.removeAttribute("registerSuccess"); // 一度表示したら削除
        }
        
        // ログイン画面（login.jsp）を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // リクエストパラメータを取得
        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");
        
        // UserDAOで検索
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByLoginIdAndPassword(loginId, password);
        
        if (user != null) {
            // ログイン成功：セッションにユーザー情報を保存
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);

            // ログイン状態保持（任意）
            String remember = request.getParameter("rememberMe");
            if ("1".equals(remember)) {
                RememberTokenDAO tokenDAO = new RememberTokenDAO();
                RememberTokenDAO.RememberToken token = tokenDAO.issueToken(user.getId(), 30);
                if (token != null) {
                    Cookie c = new Cookie(AuthUtil.REMEMBER_COOKIE_NAME, token.rawToken);
                    c.setHttpOnly(true);
                    c.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                    c.setMaxAge(60 * 60 * 24 * 30);
                    if (request.isSecure()) {
                        c.setSecure(true);
                    }
                    response.addCookie(c);
                }
            }
            
            // /dashboardにリダイレクト
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // ログイン失敗：エラーメッセージを設定してlogin.jspに戻す
            request.setAttribute("errorMessage", "ログインIDまたはパスワードが正しくありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
            dispatcher.forward(request, response);
        }
    }
}

