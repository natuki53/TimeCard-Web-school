package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.AttendanceDAO;
import dao.GroupDAO;
import model.Attendance;
import model.Group;
import model.GroupMember;
import model.User;

/**
 * グループ勤怠確認サーブレット
 * 管理者がグループメンバーの勤怠状況を確認する
 */
@WebServlet("/group/attendance")
public class GroupAttendanceServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // セッションからログインユーザーを取得
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            // ログインしていない場合はログイン画面へリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // グループIDを取得
        String groupIdStr = request.getParameter("id");
        if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        GroupDAO groupDAO = new GroupDAO();
        
        // グループ情報を取得
        Group group = groupDAO.findGroupById(groupId);
        if (group == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // 管理者権限またはメンバー権限チェック
        boolean isAdmin = groupDAO.isGroupAdmin(groupId, loginUser.getId());
        boolean isMember = groupDAO.isGroupMember(groupId, loginUser.getId());
        
        if (!isAdmin && !isMember) {
            request.setAttribute("errorMessage", "このグループの閲覧権限がありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
        // 表示する年月を取得（デフォルトは今月）
        String yearMonthStr = request.getParameter("yearMonth");
        YearMonth targetYearMonth;
        
        if (yearMonthStr != null && !yearMonthStr.trim().isEmpty()) {
            try {
                targetYearMonth = YearMonth.parse(yearMonthStr);
            } catch (Exception e) {
                targetYearMonth = YearMonth.now();
            }
        } else {
            targetYearMonth = YearMonth.now();
        }
        
        // グループメンバー一覧を取得
        List<GroupMember> members = groupDAO.findGroupMembers(groupId);
        
        // 各メンバーの勤怠情報を取得
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        Map<Integer, List<Attendance>> memberAttendances = new HashMap<>();
        
        for (GroupMember member : members) {
            List<Attendance> attendances = attendanceDAO.findByUserIdAndMonth(
                member.getUserId(), 
                targetYearMonth.getYear(), 
                targetYearMonth.getMonthValue()
            );
            memberAttendances.put(member.getUserId(), attendances);
        }
        
        // 前月・次月のリンク用
        YearMonth prevMonth = targetYearMonth.minusMonths(1);
        YearMonth nextMonth = targetYearMonth.plusMonths(1);
        
        // リクエストスコープに設定
        request.setAttribute("group", group);
        request.setAttribute("members", members);
        request.setAttribute("memberAttendances", memberAttendances);
        request.setAttribute("targetYearMonth", targetYearMonth);
        request.setAttribute("prevMonth", prevMonth);
        request.setAttribute("nextMonth", nextMonth);
        request.setAttribute("isAdmin", isAdmin);
        
        // グループ勤怠確認画面を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_attendance.jsp");
        dispatcher.forward(request, response);
    }
}
