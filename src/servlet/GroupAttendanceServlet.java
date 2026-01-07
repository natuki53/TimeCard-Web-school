package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import util.AuthUtil;

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
        User loginUser = AuthUtil.getLoginUser(request);
        
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
        Map<Integer, Integer> memberAttendanceDays = new HashMap<>();
        Map<Integer, String> memberTotalWorkTime = new HashMap<>();
        
        if (isAdmin) {
            // 管理者の場合：全メンバーの勤怠を取得
            for (GroupMember member : members) {
                List<Attendance> attendances = attendanceDAO.findByUserIdAndMonth(
                    member.getUserId(), 
                    groupId,
                    targetYearMonth.getYear(), 
                    targetYearMonth.getMonthValue()
                );
                memberAttendances.put(member.getUserId(), attendances);

                // 統計（出勤日数、合計勤務時間=休憩控除後）
                int days = 0;
                int workMinutes = 0;
                if (attendances != null) {
                    for (Attendance att : attendances) {
                        if (att.getStartTime() != null) days++;
                        if (!att.isCancelled() && att.getStartTime() != null && att.getEndTime() != null) {
                            int minutes = (int) java.time.Duration.between(att.getStartTime(), att.getEndTime()).toMinutes();
                            int breakMin = attendanceDAO.sumBreakMinutes(att.getId());
                            int wm = minutes - breakMin;
                            if (wm > 0) workMinutes += wm;
                        }
                    }
                }
                memberAttendanceDays.put(member.getUserId(), days);
                memberTotalWorkTime.put(member.getUserId(), AttendanceDAO.formatMinutesHHmm(workMinutes));
            }
        } else {
            // 一般メンバーの場合：自分の勤怠のみ取得
            List<Attendance> myAttendances = attendanceDAO.findByUserIdAndMonth(
                loginUser.getId(), 
                groupId,
                targetYearMonth.getYear(), 
                targetYearMonth.getMonthValue()
            );
            memberAttendances.put(loginUser.getId(), myAttendances);

            int days = 0;
            int workMinutes = 0;
            if (myAttendances != null) {
                for (Attendance att : myAttendances) {
                    if (att.getStartTime() != null) days++;
                    if (!att.isCancelled() && att.getStartTime() != null && att.getEndTime() != null) {
                        int minutes = (int) java.time.Duration.between(att.getStartTime(), att.getEndTime()).toMinutes();
                        int breakMin = attendanceDAO.sumBreakMinutes(att.getId());
                        int wm = minutes - breakMin;
                        if (wm > 0) workMinutes += wm;
                    }
                }
            }
            memberAttendanceDays.put(loginUser.getId(), days);
            memberTotalWorkTime.put(loginUser.getId(), AttendanceDAO.formatMinutesHHmm(workMinutes));
        }
        
        // 前月・次月のリンク用
        YearMonth prevMonth = targetYearMonth.minusMonths(1);
        YearMonth nextMonth = targetYearMonth.plusMonths(1);
        
        // リクエストスコープに設定
        request.setAttribute("group", group);
        request.setAttribute("members", members);
        request.setAttribute("memberAttendances", memberAttendances);
        request.setAttribute("memberAttendanceDays", memberAttendanceDays);
        request.setAttribute("memberTotalWorkTime", memberTotalWorkTime);
        request.setAttribute("targetYearMonth", targetYearMonth);
        request.setAttribute("prevMonth", prevMonth);
        request.setAttribute("nextMonth", nextMonth);
        request.setAttribute("isAdmin", isAdmin);
        
        // グループ勤怠確認画面を表示
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_attendance.jsp");
        dispatcher.forward(request, response);
    }
}
