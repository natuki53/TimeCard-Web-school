package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import dao.UserDAO;
import model.User;

/**
 * 新規登録処理サーブレット
 * GET: 新規登録画面表示
 * POST: 新規登録処理
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 新規登録画面（register.jsp）を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/register.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // リクエストパラメータを取得
        String name = request.getParameter("name");
        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");
        
        // Userオブジェクトを作成
        User user = new User();
        user.setLoginId(loginId);
        user.setPasswordHash(password); // 仕様書では平文でも可とされている
        user.setName(name);
        
        // UserDAO.insert()で登録
        UserDAO userDAO = new UserDAO();
        boolean success = userDAO.insert(user);
        
        if (success) {
            // 登録成功：成功メッセージをセッションに保存してlogin.jspにリダイレクト
            request.getSession().setAttribute("registerSuccess", "新規登録が完了しました。ログインしてください。");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            // 登録失敗：エラーメッセージを設定してregister.jspに戻す
            request.setAttribute("msg", "登録に失敗しました。ログインIDが既に使用されている可能性があります。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/register.jsp");
            dispatcher.forward(request, response);
        }
    }
}

