package servlet;

import dao.UserDAO;
import dao.GroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.AuthUtil;

import java.io.IOException;

/**
 * ユーザー退会（論理削除）
 */
@WebServlet("/user/delete")
public class UserDeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        UserDAO userDAO = new UserDAO();
        // 退会ユーザーが管理しているグループは非表示にする
        GroupDAO groupDAO = new GroupDAO();
        groupDAO.softDeleteGroupsByAdmin(loginUser.getId());

        boolean ok = userDAO.softDelete(loginUser.getId());

        // セッション終了
        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {}

        // remember_token cookie を削除
        Cookie c = new Cookie(AuthUtil.REMEMBER_COOKIE_NAME, "");
        c.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        c.setMaxAge(0);
        c.setHttpOnly(true);
        response.addCookie(c);

        // ログイン画面へ
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("registerSuccess", ok ? "退会処理が完了しました。" : "退会処理に失敗しました。");
        response.sendRedirect(request.getContextPath() + "/login");
    }
}


