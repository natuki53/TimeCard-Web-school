package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import dao.AttendanceDAO;
import model.Attendance;
import model.User;

/**
 * 勤怠一覧表示サーブレット
 * GET: 勤怠一覧表示（年月指定）
 */
@WebServlet("/attendance/list")
public class AttendanceListServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ログイン済みかチェック
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            // 未ログインなら/loginにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // year, monthパラメータを取得（なければ現在の年月）
        String yearParam = request.getParameter("year");
        String monthParam = request.getParameter("month");
        
        int year;
        int month;
        
        if (yearParam != null && monthParam != null) {
            try {
                year = Integer.parseInt(yearParam);
                month = Integer.parseInt(monthParam);
            } catch (NumberFormatException e) {
                // パラメータが不正な場合は現在の年月を使用
                LocalDate now = LocalDate.now();
                year = now.getYear();
                month = now.getMonthValue();
            }
        } else {
            // パラメータがなければ現在の年月を使用
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        
        // AttendanceDAO.findByUserIdAndMonth()で一覧取得
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        List<Attendance> attendanceList = attendanceDAO.findByUserIdAndMonth(user.getId(), year, month);
        
        // 出勤日数を計算（start_timeがnullでない日数）
        int attendanceDays = 0;
        if (attendanceList != null) {
            for (Attendance attendance : attendanceList) {
                if (attendance.getStartTime() != null) {
                    attendanceDays++;
                }
            }
        }
        
        // リクエスト属性にセット
        request.setAttribute("attendanceList", attendanceList);
        request.setAttribute("year", year);
        request.setAttribute("month", month);
        request.setAttribute("attendanceDays", attendanceDays);
        
        // attendance_list.jspにフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/attendance_list.jsp");
        dispatcher.forward(request, response);
    }
}

