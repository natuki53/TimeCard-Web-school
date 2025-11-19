package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        // TODO: ログイン画面（login.jsp）を表示
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: ログイン処理
        // 1. リクエストパラメータを取得（loginId, password）
        // 2. UserDAO.findByLoginIdAndPassword()で検索
        // 3. 見つかったらセッションに保存して/attendanceにリダイレクト
        // 4. 見つからなかったらエラーメッセージと共にlogin.jspに戻す
    }
}

