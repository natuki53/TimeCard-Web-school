package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 勤怠一覧表示サーブレット
 * GET: 勤怠一覧表示（年月指定）
 */
@WebServlet("/attendance/list")
public class AttendanceListServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 勤怠一覧表示
        // 1. ログイン済みかチェック
        // 2. 未ログインなら/loginにリダイレクト
        // 3. year, monthパラメータを取得（なければ現在の年月）
        // 4. AttendanceDAO.findByUserIdAndMonth()で一覧取得
        // 5. リクエスト属性にセットしてattendance_list.jspにフォワード
    }
}

