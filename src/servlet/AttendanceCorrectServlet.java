package servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.AttendanceDAO;
import dao.GroupDAO;
import model.Attendance;
import model.User;
import util.AuthUtil;

/**
 * 勤怠修正サーブレット
 * - 管理者: グループメンバーの勤怠を修正可能
 * - 一般ユーザー: 自分の勤怠を修正可能（修正表示は黄色）
 */
@WebServlet("/attendance/correct")
public class AttendanceCorrectServlet extends HttpServlet {

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

    private static Integer toIntOrNull(String intParam) {
        if (intParam == null) return null;
        String v = intParam.trim();
        if (v.isEmpty()) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalTime parseTimeOrNull(String hhmm) {
        if (hhmm == null) return null;
        String v = hhmm.trim();
        if (v.isEmpty()) return null;
        return LocalTime.parse(v);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer groupId = toGroupIdOrNull(request.getParameter("groupId"));
        Integer targetUserId = toIntOrNull(request.getParameter("userId"));
        String workDateStr = request.getParameter("workDate");

        if (targetUserId == null || workDateStr == null || workDateStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/attendance/list");
            return;
        }

        LocalDate workDate;
        try {
            workDate = LocalDate.parse(workDateStr.trim());
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/attendance/list");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();

        // グループ指定がある場合は、最低限「自分がそのグループに参加している」ことを要求
        if (groupId != null) {
            boolean allowedInGroup = groupDAO.isGroupAdmin(groupId, loginUser.getId()) || groupDAO.isGroupMember(groupId, loginUser.getId());
            if (!allowedInGroup) {
                groupId = null; // 権限なしはグループなし扱いに落とす
            }
        }

        // 他人の勤怠修正は「そのグループの管理者」だけ許可（グループなしは対象外）
        if (targetUserId != loginUser.getId()) {
            if (groupId == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            boolean isAdmin = groupDAO.isGroupAdmin(groupId, loginUser.getId());
            boolean isMember = groupDAO.isGroupMember(groupId, targetUserId);
            if (!isAdmin || !isMember) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
        }

        LocalTime startTime = parseTimeOrNull(request.getParameter("startTime"));
        LocalTime endTime = parseTimeOrNull(request.getParameter("endTime"));

        AttendanceDAO attendanceDAO = new AttendanceDAO();
        Attendance existing = attendanceDAO.findByUserIdAndDate(targetUserId, workDate, groupId);

        boolean correctedByAdmin = (groupId != null) && groupDAO.isGroupAdmin(groupId, loginUser.getId());

        if (existing == null) {
            Attendance attendance = new Attendance();
            attendance.setUserId(targetUserId);
            attendance.setGroupId(groupId);
            attendance.setWorkDate(workDate);
            attendance.setStartTime(startTime);
            attendance.setEndTime(endTime);
            attendanceDAO.insertAsCorrection(attendance, correctedByAdmin, loginUser.getId());
        } else {
            existing.setStartTime(startTime);
            existing.setEndTime(endTime);
            attendanceDAO.updateAsCorrection(existing, correctedByAdmin, loginUser.getId());
        }
     // insert / update 完了後
        request.getSession().setAttribute("attendanceCorrected", true);


        // 元の画面に戻す（なければ勤怠一覧へ）
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            response.sendRedirect(referer);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/attendance/list");
    }
}

