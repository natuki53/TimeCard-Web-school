package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dao.AttendanceDAO;
import dao.GroupDAO;
import model.Attendance;
import model.Group;
import model.User;
import util.AuthUtil;

/**
 * 勤怠一覧表示サーブレット
 * GET: 勤怠一覧表示（年月指定）
 */
@WebServlet("/attendance/list")
public class AttendanceListServlet extends HttpServlet {

    private static Integer toGroupIdOrNull(String groupIdParam) {
        if (groupIdParam == null) return null;
        String v = groupIdParam.trim();
        if (v.isEmpty()) return null;
        try {
            int id = Integer.parseInt(v);
            return id <= 0 ? null : id;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ログイン済みかチェック
        User user = AuthUtil.getLoginUser(request);
        
        if (user == null) {
            // 未ログインなら/loginにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // year, monthパラメータを取得（なければ現在の年月）
        String yearParam = request.getParameter("year");
        String monthParam = request.getParameter("month");
        Integer groupId = toGroupIdOrNull(request.getParameter("groupId"));

        // グループ一覧（管理者ORメンバーをまとめて取得）
        GroupDAO groupDAO = new GroupDAO();
        List<Group> groups = groupDAO.findGroupsByUser(user.getId());

        // 権限チェック（グループ指定がある場合のみ）
        if (groupId != null) {
            boolean allowed = groupDAO.isGroupAdmin(groupId, user.getId()) || groupDAO.isGroupMember(groupId, user.getId());
            if (!allowed) {
                groupId = null;
            }
        }
        
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
        
        // AttendanceDAO.findByUserIdAndMonth()で一覧取得（グループ別）
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        List<Attendance> attendanceList = attendanceDAO.findByUserIdAndMonth(user.getId(), groupId, year, month);

        // 休憩合計（分）を勤怠IDごとに計算
        Map<Integer, Integer> breakMinutesByAttendanceId = new HashMap<>();
        if (attendanceList != null) {
            for (Attendance att : attendanceList) {
                if (att.getId() > 0) {
                    breakMinutesByAttendanceId.put(att.getId(), attendanceDAO.sumBreakMinutes(att.getId()));
                }
            }
        }
        
        // 出勤日数を計算（start_timeがnullでない日数）
        int attendanceDays = 0;
        int totalWorkMinutes = 0;
        if (attendanceList != null) {
            for (Attendance attendance : attendanceList) {
                if (attendance.getStartTime() != null) {
                    attendanceDays++;
                }
                // 合計勤務時間（休憩控除後）：start/endが揃っている日だけ加算
                if (!attendance.isCancelled() && attendance.getStartTime() != null && attendance.getEndTime() != null) {
                    int minutes = (int) java.time.Duration.between(attendance.getStartTime(), attendance.getEndTime()).toMinutes();
                    int breakMin = 0;
                    Integer bm = breakMinutesByAttendanceId.get(attendance.getId());
                    if (bm != null) breakMin = bm;
                    int workMin = minutes - breakMin;
                    if (workMin > 0) totalWorkMinutes += workMin;
                }
            }
        }
        
        // リクエスト属性にセット
        request.setAttribute("attendanceList", attendanceList);
        request.setAttribute("year", year);
        request.setAttribute("month", month);
        request.setAttribute("attendanceDays", attendanceDays);
        request.setAttribute("groups", groups);
        request.setAttribute("selectedGroupId", groupId); // null=グループなし
        request.setAttribute("breakMinutesByAttendanceId", breakMinutesByAttendanceId);
        request.setAttribute("totalWorkTime", AttendanceDAO.formatMinutesHHmm(totalWorkMinutes));
        
        // attendance_list.jspにフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/attendance_list.jsp");
        dispatcher.forward(request, response);
    }
}

