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
import util.PasswordUtil;

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
        request.setCharacterEncoding("UTF-8");

        // リクエストパラメータを取得
        String name = trimOrEmpty(request.getParameter("name"));
        String loginId = trimOrEmpty(request.getParameter("loginId"));
        String secretQuestion = trimOrEmpty(request.getParameter("secretQuestion"));
        String secretAnswer = request.getParameter("secretAnswer");
        String password = request.getParameter("password");
        if (password == null) password = "";
        if (secretAnswer == null) secretAnswer = "";
        secretAnswer = secretAnswer.trim();

        // 簡易バリデーション
        if (name.isEmpty() || name.length() > 100
                || loginId.isEmpty() || loginId.length() > 50
                || secretQuestion.isEmpty() || secretQuestion.length() > 255
                || secretAnswer.isEmpty() || secretAnswer.length() > 255
                || password.isEmpty() || password.length() > 100) {
            request.setAttribute("msg", "入力内容に不備があります。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/register.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
        // Userオブジェクトを作成
        User user = new User();
        user.setLoginId(loginId);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setSecretQuestion(secretQuestion);
        user.setSecretAnswerHash(PasswordUtil.hash(secretAnswer));
        user.setName(name);
        
        // UserDAO で登録（IDも取得）
        UserDAO userDAO = new UserDAO();
        int userId = userDAO.insertAndReturnId(user);
        
        if (userId > 0) {
            request.getSession().setAttribute("registerSuccess", "新規登録が完了しました。ログインしてください。");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            // 登録失敗：エラーメッセージを設定してregister.jspに戻す
            request.setAttribute("msg", "登録に失敗しました。ログインIDが既に使用されている可能性があります。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/register.jsp");
            dispatcher.forward(request, response);
        }
    }

    private String trimOrEmpty(String s) {
        if (s == null) return "";
        return s.trim();
    }
}

