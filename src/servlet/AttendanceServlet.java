package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 勤怠打刻処理サーブレット
 * GET: 今日の打刻状態表示
 * POST: 出勤 or 退勤ボタン処理
 */
@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 勤怠打刻画面表示
        // 1. ログイン済みかチェック（セッション確認）
        // 2. 未ログインなら/loginにリダイレクト
        // 3. 今日のAttendanceを取得
        // 4. attendance.jspにフォワード
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 出勤 or 退勤処理
        // 1. ログイン済みかチェック
        // 2. actionパラメータで出勤/退勤を判別
        // 3. 出勤の場合: 今日のレコードがなければINSERT、あればUPDATE
        // 4. 退勤の場合: 今日のレコードのend_timeをUPDATE
        // 5. 処理後/attendanceにリダイレクト
    }
}

