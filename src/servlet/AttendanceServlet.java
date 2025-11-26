package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import dao.AttendanceDAO;
import model.Attendance;
import model.User;

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
        // ログイン済みかチェック（セッション確認）
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            // 未ログインなら/loginにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // 今日のAttendanceを取得
        LocalDate today = LocalDate.now();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), Date.valueOf(today));
        
        // リクエスト属性にセット
        request.setAttribute("todayAttendance", attendance);
        request.setAttribute("today", today);
        
        // attendance.jspにフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/attendance.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ログイン済みかチェック
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            // 未ログインなら/loginにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // actionパラメータで出勤/退勤を判別
        String action = request.getParameter("action");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        
        if ("start".equals(action)) {
            // 出勤処理
            Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), Date.valueOf(today));
            
            if (attendance == null) {
                // 今日のレコードがなければ新規作成してINSERT
                attendance = new Attendance();
                attendance.setUserId(user.getId());
                attendance.setWorkDate(today);
                attendance.setStartTime(now);
                attendanceDAO.insert(attendance);
            } else {
                // 既にレコードがあればstart_timeをUPDATE（仕様書では上書きでも良いとされている）
                attendance.setStartTime(now);
                attendanceDAO.update(attendance);
            }
        } else if ("end".equals(action)) {
            // 退勤処理
            Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), Date.valueOf(today));
            
            if (attendance != null) {
                // 今日のレコードのend_timeをUPDATE
                attendance.setEndTime(now);
                attendanceDAO.update(attendance);
            }
        }
        
        // 処理後/attendanceにリダイレクト
        response.sendRedirect(request.getContextPath() + "/attendance");
    }
}

