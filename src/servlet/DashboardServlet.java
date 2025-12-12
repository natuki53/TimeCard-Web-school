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
import java.time.YearMonth;
import java.util.List;

import dao.AttendanceDAO;
import dao.GroupDAO;
import model.Attendance;
import model.Group;
import model.User;
import util.AuthUtil;

/**
 * ダッシュボード画面サーブレット
 * ログイン後のメインページ
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // セッションからログインユーザーを取得
        HttpSession session = request.getSession();
        User loginUser = AuthUtil.getLoginUser(request);
        
        if (loginUser == null) {
            // ログインしていない場合はログイン画面へリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // 今日の勤怠情報を取得
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        LocalDate today = LocalDate.now();
        Attendance todayAttendance = attendanceDAO.findByUserIdAndDate(loginUser.getId(), today);
        
        // 最近の勤怠履歴を取得（直近5日分）
        List<Attendance> recentAttendances = attendanceDAO.findRecentByUserId(loginUser.getId(), 5);

        // 今月の勤怠状況を取得（全グループ混在、直近10件）
        YearMonth ym = YearMonth.now();
        List<Attendance> thisMonthAttendances = attendanceDAO.findByUserIdAndMonthAllGroups(loginUser.getId(), ym.getYear(), ym.getMonthValue(), 10);
        
        // グループ情報を取得
        GroupDAO groupDAO = new GroupDAO();
        List<Group> adminGroups = groupDAO.findGroupsByAdmin(loginUser.getId());
        List<Group> memberGroups = groupDAO.findGroupsByMember(loginUser.getId());
        
        // セッションからメッセージを取得
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        
        // リクエストスコープに設定
        request.setAttribute("todayAttendance", todayAttendance);
        request.setAttribute("recentAttendances", recentAttendances);
        request.setAttribute("thisMonthAttendances", thisMonthAttendances);
        request.setAttribute("thisMonthYear", ym.getYear());
        request.setAttribute("thisMonthMonth", ym.getMonthValue());
        request.setAttribute("adminGroups", adminGroups);
        request.setAttribute("memberGroups", memberGroups);
        
        // ダッシュボード画面を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}
