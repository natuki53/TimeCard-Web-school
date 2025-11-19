package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        // TODO: 新規登録画面（register.jsp）を表示
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 新規登録処理
        // 1. リクエストパラメータを取得（name, loginId, password）
        // 2. Userオブジェクトを作成
        // 3. UserDAO.insert()で登録
        // 4. 成功したらlogin.jspにリダイレクト
        // 5. 失敗したらエラーメッセージと共にregister.jspに戻す
    }
}

