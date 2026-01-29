<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="model.Group" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    List<Attendance> attendanceList = (List<Attendance>) request.getAttribute("attendanceList");
    @SuppressWarnings("unchecked")
    List<Group> groups = (List<Group>) request.getAttribute("groups");
    Integer selectedGroupId = (Integer) request.getAttribute("selectedGroupId"); // null=グループなし
    @SuppressWarnings("unchecked")
    Map<Integer, Integer> breakMinutesByAttendanceId = (Map<Integer, Integer>) request.getAttribute("breakMinutesByAttendanceId");
    String totalWorkTime = (String) request.getAttribute("totalWorkTime");
    if (totalWorkTime == null) totalWorkTime = "00:00";
    Integer year = (Integer) request.getAttribute("year");
    Integer month = (Integer) request.getAttribute("month");
    Integer attendanceDays = (Integer) request.getAttribute("attendanceDays");
    
    // 現在の年月が設定されていない場合は現在の日付を使用
    if (year == null || month == null) {
        LocalDate now = LocalDate.now();
        year = now.getYear();
        month = now.getMonthValue();
    }
    
    // 前月・次月の計算
    LocalDate currentMonth = LocalDate.of(year, month, 1);
    LocalDate prevMonth = currentMonth.minusMonths(1);
    LocalDate nextMonth = currentMonth.plusMonths(1);
    LocalDate monthFirstDay = currentMonth;
    LocalDate monthLastDay = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
    LocalDate today = LocalDate.now();
    String defaultWorkDate = (today.getYear() == year && today.getMonthValue() == month)
        ? today.toString()
        : monthFirstDay.toString();
    
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
    String currentMonthStr = currentMonth.format(monthFormatter);
    String groupParam = "groupId=" + (selectedGroupId != null ? selectedGroupId : 0);
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>勤怠一覧 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
    <script defer src="<%= request.getContextPath() %>/js/notifications.js"></script>
</head>
<body class="with-header">

<%
String corrected = request.getParameter("corrected");
if ("1".equals(corrected)) {
%>
<script>
    alert("勤怠修正が完了しました");
</script>
<%
}
%>

    <!-- ヘッダー -->
    <header class="header">
        <a href="<%= request.getContextPath() %>/dashboard">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>
        
        <nav class="header-nav">
            <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
            <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
            <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
            <a href="<%= request.getContextPath() %>/groups">グループ</a>
            <a href="<%= request.getContextPath() %>/dm">DM</a>
        </nav>
        
        <div class="header-user">
            <span class="header-user-name"><%= loginUser != null ? loginUser.getName() + "さん" : "ゲスト" %></span>
            <a href="<%= request.getContextPath() %>/logout" class="header-logout">ログアウト</a>
        </div>
        
        <!-- ハンバーガーメニュー（モバイル用） -->
        <div class="hamburger" onclick="toggleMobileMenu()">
            <span></span>
            <span></span>
            <span></span>
        </div>
    </header>
    
    <!-- モバイルメニュー -->
    <div class="mobile-menu" id="mobileMenu">
        <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
        <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
        <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
        <a href="<%= request.getContextPath() %>/groups">グループ</a>
        <a href="<%= request.getContextPath() %>/dm">DM</a>
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser != null ? loginUser.getName() + "さん" : "ゲスト" %></a>
        <a href="<%= request.getContextPath() %>/logout" style="color: #e74c3c;">ログアウト</a>
    </div>
    <div class="container">
        <h1>勤怠一覧</h1>
        
        <div class="user-info">
            <p><strong><%= loginUser != null ? loginUser.getName() : "ゲスト" %>さんの勤怠一覧</strong></p>
        </div>
        
        <div class="month-navigation">
            <a href="?year=<%= prevMonth.getYear() %>&month=<%= prevMonth.getMonthValue() %>&<%= groupParam %>">前の月</a>
            
            <span class="current-month"><%= currentMonthStr %></span>
            
            <a href="?year=<%= nextMonth.getYear() %>&month=<%= nextMonth.getMonthValue() %>&<%= groupParam %>">次の月</a>
        </div>

        <!-- グループ選択 -->
        <div class="group-select">
            <form method="GET" action="<%= request.getContextPath() %>/attendance/list" class="group-select-form">
                <input type="hidden" name="year" value="<%= year %>">
                <input type="hidden" name="month" value="<%= month %>">
                <label for="groupId"><strong>表示するグループ:</strong></label>
                <select id="groupId" name="groupId" onchange="this.form.submit()">
                    <option value="0" <%= (selectedGroupId == null) ? "selected" : "" %>>グループなし</option>
                    <%
                        if (groups != null) {
                            for (Group g : groups) {
                    %>
                        <option value="<%= g.getId() %>" <%= (selectedGroupId != null && selectedGroupId == g.getId()) ? "selected" : "" %>>
                            <%= g.getName() %>
                        </option>
                    <%
                            }
                        }
                    %>
                </select>
                <noscript>
                    <button type="submit" class="btn btn-primary btn-sm">切替</button>
                </noscript>
            </form>
        </div>

        <!-- 存在しない日の勤怠を追加（修正扱い） -->
        <div class="card" style="margin: 16px 0;">
            <h2 style="margin: 0 0 8px 0; font-size: 1.05rem;">存在しない日の勤怠を追加（修正扱い）</h2>
            <form method="POST" action="<%= request.getContextPath() %>/attendance/correct" class="attendance-correct-form" onsubmit="return confirm('この内容で勤怠を登録（修正扱い）しますか？');">
                <input type="hidden" name="userId" value="<%= loginUser.getId() %>">
                <input type="hidden" name="groupId" value="<%= selectedGroupId != null ? selectedGroupId : 0 %>">
                <div class="form-group inline-form">
                    <label>日付:</label>
                    <input type="date" name="workDate" value="<%= defaultWorkDate %>" min="<%= monthFirstDay.toString() %>" max="<%= monthLastDay.toString() %>">
                    <label>出勤:</label>
                    <input type="time" name="startTime" value="">
                    <label>退勤:</label>
                    <input type="time" name="endTime" value="">
                    <button type="submit" class="btn btn-sm btn-primary">追加</button>
                </div>
                <div style="margin-top: 6px; color: #666; font-size: 0.9rem;">
                    ※ この月（<%= currentMonthStr %>）の範囲で日付を選べます。未登録日でも登録できます。
                </div>
            </form>
        </div>
        
        <table>
            <thead>
                <tr>
                    <th>日付</th>
                    <th>出勤時刻</th>
                    <th>休憩合計</th>
                    <th>退勤時刻</th>
                    <th>修正</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (attendanceList != null && !attendanceList.isEmpty()) {
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        for (Attendance attendance : attendanceList) {
                %>
                <tr class="<%=
                    (attendance.isCancelled() && !attendance.isCancelledByAdmin()) ? "attendance-cancelled-by-member" :
                    (attendance.isCorrected() && !attendance.isCorrectedByAdmin()) ? "attendance-corrected-by-member" : ""
                %>">
                    <td><%= attendance.getWorkDate().format(dateFormatter) %></td>
                    <td>
                        <%
                            String st = "--:--";
                            if (!attendance.isCancelled() && attendance.getStartTime() != null) {
                                st = attendance.getStartTime().toString();
                                if (st.length() >= 5) st = st.substring(0, 5);
                            }
                        %>
                        <%= st %>
                    </td>
                    <td>
                        <%
                            int bm = 0;
                            if (breakMinutesByAttendanceId != null && breakMinutesByAttendanceId.get(attendance.getId()) != null) {
                                bm = breakMinutesByAttendanceId.get(attendance.getId());
                            }
                            int bh = bm / 60;
                            int bmin = bm % 60;
                            String breakHHmm = String.format("%02d:%02d", bh, bmin);
                        %>
                        <%= attendance.isCancelled() ? "00:00" : breakHHmm %>
                    </td>
                    <td>
                        <%
                            String et = "--:--";
                            if (!attendance.isCancelled() && attendance.getEndTime() != null) {
                                et = attendance.getEndTime().toString();
                                if (et.length() >= 5) et = et.substring(0, 5);
                            }
                        %>
                        <%= et %>
                        <% if (attendance.isCancelled() && !attendance.isCancelledByAdmin()) { %>
                            <div class="cancelled-message">この勤怠は取り消しされました。</div>
                        <% } %>
                    </td>
                    <td>
                        <div class="edit-actions">
                            <details>
                                <summary class="btn btn-sm btn-secondary">修正</summary>
                            <%
                                String stVal = attendance.getStartTime() != null ? attendance.getStartTime().toString() : "";
                                if (stVal.length() >= 5) stVal = stVal.substring(0, 5);
                                String etVal = attendance.getEndTime() != null ? attendance.getEndTime().toString() : "";
                                if (etVal.length() >= 5) etVal = etVal.substring(0, 5);
                            %>
                            <form method="POST" action="<%= request.getContextPath() %>/attendance/correct" class="attendance-correct-form">
                                <input type="hidden" name="userId" value="<%= loginUser.getId() %>">
                                <input type="hidden" name="workDate" value="<%= attendance.getWorkDate().toString() %>">
                                <input type="hidden" name="groupId" value="<%= selectedGroupId != null ? selectedGroupId : 0 %>">
                                <div class="form-group inline-form">
                                    <label>出勤:</label>
                                    <input type="time" name="startTime" value="<%= stVal %>">
                                    <label>退勤:</label>
                                    <input type="time" name="endTime" value="<%= etVal %>">
                                    <button type="submit" class="btn btn-sm btn-primary">保存</button>
                                </div>
                            </form>
                            </details>
                            <form method="POST" action="<%= request.getContextPath() %>/attendance/cancel" style="display:inline;" onsubmit="return confirm('この勤怠を取り消しますか？');">
                                <input type="hidden" name="userId" value="<%= loginUser.getId() %>">
                                <input type="hidden" name="workDate" value="<%= attendance.getWorkDate().toString() %>">
                                <input type="hidden" name="groupId" value="<%= selectedGroupId != null ? selectedGroupId : 0 %>">
                                <button type="submit" class="btn btn-sm btn-danger">勤怠削除</button>
                            </form>
                        </div>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5" style="text-align: center; color: #666;">この月の勤怠データはありません</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
        
        <div class="stats">
            <p>
                出勤日数: <strong><%= attendanceDays != null ? attendanceDays : 0 %></strong> 日　
                合計勤務時間: <strong><%= totalWorkTime %></strong>
            </p>
        </div>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/attendance" class="btn btn-primary">打刻画面に戻る</a>
        </div>
    </div>
    
    <script>
        function toggleMobileMenu() {
            const mobileMenu = document.getElementById('mobileMenu');
            mobileMenu.classList.toggle('active');
        }
        
        // 画面サイズが変わったときにモバイルメニューを閉じる
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                document.getElementById('mobileMenu').classList.remove('active');
            }
        });
        
        // 修正ボタンのテキストを開閉状態に応じて変更
        document.addEventListener('DOMContentLoaded', function() {
            const detailsElements = document.querySelectorAll('details');
            detailsElements.forEach(function(details) {
                const summary = details.querySelector('summary');
                if (summary && summary.textContent.trim() === '修正') {
                    // 初期状態を設定
                    updateSummaryText(details, summary);
                    
                    // 開閉イベントを監視
                    details.addEventListener('toggle', function() {
                        updateSummaryText(details, summary);
                    });
                }
            });
        });
        
        function updateSummaryText(details, summary) {
            if (details.open) {
                summary.textContent = '修正キャンセル';
            } else {
                summary.textContent = '修正';
            }
        }
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>

