package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import dao.AttendanceDAO;
import dao.GroupDAO;
import model.Attendance;
import model.AttendanceBreak;
import model.Group;
import model.User;
import util.AuthUtil;

/**
 * 勤怠打刻処理サーブレット
 * GET: 今日の打刻状態表示
 * POST: 出勤 or 退勤ボタン処理
 */
@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
    
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
        // ログイン済みかチェック（セッション確認）
        User user = AuthUtil.getLoginUser(request);
        
        if (user == null) {
            // 未ログインなら/loginにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // グループ一覧（管理者ORメンバーをまとめて取得）
        GroupDAO groupDAO = new GroupDAO();
        List<Group> groups = groupDAO.findGroupsByUser(user.getId());

        // 選択中グループ（未指定/不正/権限なしの場合は「グループなし」扱い）
        Integer groupId = toGroupIdOrNull(request.getParameter("groupId"));
        if (groupId != null) {
            boolean allowed = groupDAO.isGroupAdmin(groupId, user.getId()) || groupDAO.isGroupMember(groupId, user.getId());
            if (!allowed) {
                groupId = null;
            }
        }

        // 今日のAttendanceを取得（グループ別）
        LocalDate today = LocalDate.now();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), today, groupId);

        // 休憩中判定（出勤済み＆退勤前のみ）
        boolean isOnBreak = false;
        List<AttendanceBreak> breaks = null;
        String totalBreakTime = "00:00";
        if (attendance != null && attendance.getStartTime() != null && attendance.getEndTime() == null) {
            isOnBreak = attendanceDAO.hasOpenBreak(attendance.getId());
            breaks = attendanceDAO.findBreaksByAttendanceId(attendance.getId());
            int endedBreakMinutes = attendanceDAO.sumBreakMinutes(attendance.getId());
            int realtimeBreakMinutes = endedBreakMinutes;
            if (isOnBreak) {
                LocalTime openStart = attendanceDAO.findOpenBreakStart(attendance.getId());
                if (openStart != null) {
                    long diff = java.time.Duration.between(openStart, java.time.LocalTime.now().truncatedTo(ChronoUnit.MINUTES)).toMinutes();
                    if (diff > 0) realtimeBreakMinutes += (int) diff;
                }
            }
            totalBreakTime = AttendanceDAO.formatMinutesHHmm(realtimeBreakMinutes);
        } else if (attendance != null) {
            breaks = attendanceDAO.findBreaksByAttendanceId(attendance.getId());
            totalBreakTime = AttendanceDAO.formatMinutesHHmm(attendanceDAO.sumBreakMinutes(attendance.getId()));
        }
        
        // リクエスト属性にセット
        request.setAttribute("todayAttendance", attendance);
        request.setAttribute("today", today);
        request.setAttribute("groups", groups);
        request.setAttribute("selectedGroupId", groupId); // null=グループなし
        request.setAttribute("isOnBreak", isOnBreak);
        request.setAttribute("breaks", breaks);
        request.setAttribute("totalBreakTime", totalBreakTime);
        
        // attendance.jspにフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/attendance.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ログイン済みかチェック
        User user = AuthUtil.getLoginUser(request);
        
        if (user == null) {
            // 未ログインなら/loginにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // actionパラメータで出勤/退勤を判別
        String action = request.getParameter("action");
        Integer groupId = toGroupIdOrNull(request.getParameter("groupId"));
        // 権限チェック（グループ指定がある場合のみ）
        if (groupId != null) {
            GroupDAO groupDAO = new GroupDAO();
            boolean allowed = groupDAO.isGroupAdmin(groupId, user.getId()) || groupDAO.isGroupMember(groupId, user.getId());
            if (!allowed) {
                groupId = null;
            }
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        
        if ("start".equals(action)) {
            // 出勤処理
            Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), today, groupId);
            
            if (attendance == null) {
                // 今日のレコードがなければ新規作成してINSERT
                attendance = new Attendance();
                attendance.setUserId(user.getId());
                attendance.setGroupId(groupId);
                attendance.setWorkDate(today);
                attendance.setStartTime(now);
                attendanceDAO.insert(attendance);
            } else {
                // 既にレコードがあればstart_timeをUPDATE（仕様書では上書きでも良いとされている）
                if (attendance.isCancelled()) {
                    // 取り消し後の再打刻を許可（取消フラグを解除）
                    attendanceDAO.clearCancellation(attendance.getId());
                }
                attendance.setStartTime(now);
                attendanceDAO.update(attendance);
            }
        } else if ("break".equals(action)) {
            // 休憩開始/終了（トグル）
            Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), today, groupId);
            if (attendance != null && attendance.getStartTime() != null && attendance.getEndTime() == null) {
                boolean onBreak = attendanceDAO.hasOpenBreak(attendance.getId());
                if (onBreak) {
                    attendanceDAO.endBreak(attendance.getId(), now);
                } else {
                    attendanceDAO.startBreak(attendance.getId(), now);
                }
            }
        } else if ("end".equals(action)) {
            // 退勤処理
            Attendance attendance = attendanceDAO.findByUserIdAndDate(user.getId(), today, groupId);
            
            if (attendance != null) {
                // 今日のレコードのend_timeをUPDATE
                attendance.setEndTime(now);
                attendanceDAO.update(attendance);
            }
        }
        
        // 処理後/attendanceにリダイレクト
        String redirect = request.getContextPath() + "/attendance";
        if (groupId != null) {
            redirect += "?groupId=" + groupId;
        }
        response.sendRedirect(redirect);
    }
}

