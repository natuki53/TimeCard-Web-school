package servlet;

import dao.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.PasswordUtil;

import java.io.IOException;

/**
 * パスワード忘れ（秘密の質問で再設定）
 * GET: ログインID入力
 * POST(action=lookup): 質問表示
 * POST(action=reset): 回答確認→パスワード更新
 */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "";

        UserDAO userDAO = new UserDAO();

        if ("lookup".equals(action)) {
            String loginId = trimOrEmpty(request.getParameter("loginId"));
            if (loginId.isEmpty() || loginId.length() > 50) {
                request.setAttribute("errorMessage", "ログインIDを入力してください。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            User user = userDAO.findByLoginId(loginId);
            if (user == null) {
                request.setAttribute("errorMessage", "ログインIDが見つかりません。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            request.setAttribute("step", "challenge");
            request.setAttribute("loginId", user.getLoginId());
            request.setAttribute("secretQuestion", user.getSecretQuestion());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if ("reset".equals(action)) {
            String loginId = trimOrEmpty(request.getParameter("loginId"));
            String answer = request.getParameter("secretAnswer");
            String password = request.getParameter("password");
            String password2 = request.getParameter("password2");
            if (answer == null) answer = "";
            if (password == null) password = "";
            if (password2 == null) password2 = "";
            answer = answer.trim();

            if (loginId.isEmpty() || loginId.length() > 50) {
                request.setAttribute("errorMessage", "ログインIDが不正です。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            User user = userDAO.findByLoginId(loginId);
            if (user == null) {
                request.setAttribute("errorMessage", "ログインIDまたは秘密の質問の答えが正しくありません。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            String stored = user.getSecretAnswerHash();
            boolean ok;
            if (PasswordUtil.looksLikeBcryptHash(stored)) {
                ok = PasswordUtil.verify(answer, stored);
            } else {
                // 旧互換: 平文が入っている場合
                ok = stored != null && stored.equals(answer);
                if (ok) {
                    String newHash = PasswordUtil.hash(answer);
                    userDAO.updateSecretAnswerHash(user.getId(), newHash);
                    stored = newHash;
                }
            }

            if (!ok) {
                request.setAttribute("step", "challenge");
                request.setAttribute("loginId", user.getLoginId());
                request.setAttribute("secretQuestion", user.getSecretQuestion());
                request.setAttribute("errorMessage", "ログインIDまたは秘密の質問の答えが正しくありません。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            if (password.isEmpty() || password.length() > 100) {
                request.setAttribute("step", "challenge");
                request.setAttribute("loginId", user.getLoginId());
                request.setAttribute("secretQuestion", user.getSecretQuestion());
                request.setAttribute("errorMessage", "新しいパスワードが不正です。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }
            if (!password.equals(password2)) {
                request.setAttribute("step", "challenge");
                request.setAttribute("loginId", user.getLoginId());
                request.setAttribute("secretQuestion", user.getSecretQuestion());
                request.setAttribute("errorMessage", "新しいパスワードが一致しません。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            boolean updated = userDAO.updatePasswordHash(user.getId(), PasswordUtil.hash(password));
            if (!updated) {
                request.setAttribute("step", "challenge");
                request.setAttribute("loginId", user.getLoginId());
                request.setAttribute("secretQuestion", user.getSecretQuestion());
                request.setAttribute("errorMessage", "更新に失敗しました。");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgot_password.jsp");
                dispatcher.forward(request, response);
                return;
            }

            request.getSession().setAttribute("registerSuccess", "パスワードを再設定しました。ログインしてください。");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 不正アクセス
        response.sendRedirect(request.getContextPath() + "/forgot-password");
    }

    private String trimOrEmpty(String s) {
        if (s == null) return "";
        return s.trim();
    }
}


