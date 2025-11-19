package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        // TODO: ログアウト処理
        // 1. セッションを無効化
        // 2. /loginにリダイレクト
    }
}

