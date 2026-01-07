package util;

import dao.RememberTokenDAO;
import dao.UserDAO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * ログイン状態取得ユーティリティ
 * - セッションに loginUser が無い場合、remember_token クッキーから自動ログインを試みる
 */
public class AuthUtil {
    public static final String REMEMBER_COOKIE_NAME = "remember_token";

    public static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User u = (User) session.getAttribute("loginUser");
        if (u != null) return u;

        String token = getCookieValue(request, REMEMBER_COOKIE_NAME);
        if (token == null) return null;

        RememberTokenDAO tokenDAO = new RememberTokenDAO();
        Integer userId = tokenDAO.findUserIdByToken(token);
        if (userId == null) return null;

        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(userId);
        if (user == null) return null;

        session.setAttribute("loginUser", user);
        return user;
    }

    private static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}

