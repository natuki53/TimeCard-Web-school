package servlet;

import java.io.IOException;
import java.time.LocalDate;

import dao.AttendanceDAO;
import dao.GroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Attendance;
import model.User;
import util.AuthUtil;

/**
 * 勤怠取消（出勤取り消し）サーブレット
 * - 一般ユーザー: 自分の勤怠のみ取消可能（表示は黄色＋メッセージ）
 * - 管理者: グループメンバーの勤怠を取消可能
 */
@WebServlet("/attendance/cancel")
public class AttendanceCancelServlet extends HttpServlet {

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

        // 他人の取消は「そのグループの管理者」だけ許可（グループなしは対象外）
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

        AttendanceDAO attendanceDAO = new AttendanceDAO();
        Attendance existing = attendanceDAO.findByUserIdAndDate(targetUserId, workDate, groupId);
        if (existing == null) {
            // 対象なし
            String referer = request.getHeader("Referer");
            response.sendRedirect(referer != null ? referer : (request.getContextPath() + "/attendance/list"));
            return;
        }

        boolean cancelledByAdmin = (groupId != null) && groupDAO.isGroupAdmin(groupId, loginUser.getId());
        attendanceDAO.cancelAttendance(existing.getId(), cancelledByAdmin, loginUser.getId());

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            response.sendRedirect(referer);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/attendance/list");
    }
}

