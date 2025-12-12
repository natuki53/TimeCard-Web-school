<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="model.Group" %>
<%@ page import="model.AttendanceBreak" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    Attendance todayAttendance = (Attendance) request.getAttribute("todayAttendance");
    @SuppressWarnings("unchecked")
    List<Group> groups = (List<Group>) request.getAttribute("groups");
    Integer selectedGroupId = (Integer) request.getAttribute("selectedGroupId"); // null=グループなし
    Boolean isOnBreakObj = (Boolean) request.getAttribute("isOnBreak");
    boolean isOnBreak = isOnBreakObj != null && isOnBreakObj;
    @SuppressWarnings("unchecked")
    List<AttendanceBreak> breaks = (List<AttendanceBreak>) request.getAttribute("breaks");
    String totalBreakTime = (String) request.getAttribute("totalBreakTime");
    if (totalBreakTime == null) totalBreakTime = "00:00";
    LocalDate today = LocalDate.now();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    String todayStr = today.format(dateFormatter);
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>勤怠打刻 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
</head>
<body class="with-header">
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
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser != null ? loginUser.getName() + "さん" : "ゲスト" %></a>
        <a href="<%= request.getContextPath() %>/logout" style="color: #e74c3c;">ログアウト</a>
    </div>
    <div class="container">
        <h1>勤怠打刻</h1>
        
        <div class="user-info">
            <p><strong><%= loginUser != null ? loginUser.getName() : "ゲスト" %>さん</strong></p>
            <p>今日の日付: <strong><%= todayStr %></strong></p>
        </div>

        <!-- グループ選択（グループ別で出退勤を保存） -->
        <div class="group-select">
            <form method="GET" action="<%= request.getContextPath() %>/attendance" class="group-select-form">
                <label for="groupId"><strong>打刻するグループ:</strong></label>
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
        
        <%
            String startDisp = "--:--";
            if (todayAttendance != null && todayAttendance.getStartTime() != null) {
                startDisp = todayAttendance.getStartTime().toString();
                if (startDisp.length() >= 5) startDisp = startDisp.substring(0, 5);
            }
            String endDisp = "--:--";
            if (todayAttendance != null && todayAttendance.getEndTime() != null) {
                endDisp = todayAttendance.getEndTime().toString();
                if (endDisp.length() >= 5) endDisp = endDisp.substring(0, 5);
            }
        %>
        <div class="attendance-info">
            <p>出勤時刻: <span class="time-display"><%= startDisp %></span></p>
            <hr class="attendance-divider">

            <!-- 休憩一覧（複数回） -->
            <div class="break-list">
                <p class="break-total">休憩（合計: <%= totalBreakTime %>）</p>
                <%
                    if (breaks != null && !breaks.isEmpty()) {
                        for (AttendanceBreak br : breaks) {
                            String bs = br.getBreakStart() != null ? br.getBreakStart().toString() : "--:--";
                            if (bs.length() >= 5 && !"--:--".equals(bs)) bs = bs.substring(0, 5);
                            String be = br.getBreakEnd() != null ? br.getBreakEnd().toString() : "--:--";
                            if (be.length() >= 5 && !"--:--".equals(be)) be = be.substring(0, 5);
                %>
                    <p class="break-item">休憩時刻：<span class="time-display"><%= bs %></span> 〜 <span class="time-display"><%= be %></span></p>
                <%
                        }
                    } else {
                %>
                    <p class="break-item">休憩時刻：--:-- 〜 --:--</p>
                <%
                    }
                %>
            </div>

            <hr class="attendance-divider">
            <p>退勤時刻: <span class="time-display"><%= endDisp %></span></p>
        </div>
        
        <div class="attendance-buttons">
            <%
                boolean hasStarted = todayAttendance != null && todayAttendance.getStartTime() != null;
                boolean hasEnded = todayAttendance != null && todayAttendance.getEndTime() != null;
                boolean canBreak = hasStarted && !hasEnded;
            %>
            
            <!-- 出勤ボタン（未出勤のときのみ有効） -->
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="start">
                <input type="hidden" name="groupId" value="<%= selectedGroupId != null ? selectedGroupId : 0 %>">
                <button type="submit" class="btn btn-success" <%= hasStarted ? "disabled" : "" %>>
                    <%= hasStarted ? "出勤済み" : "出勤" %>
                </button>
            </form>

            <!-- 休憩ボタン（出勤〜退勤の間のみ有効、複数回OK） -->
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="break">
                <input type="hidden" name="groupId" value="<%= selectedGroupId != null ? selectedGroupId : 0 %>">
                <button type="submit" class="btn btn-secondary" <%= canBreak ? "" : "disabled" %>>
                    <%= isOnBreak ? "休憩終了" : "休憩" %>
                </button>
            </form>
            
            <!-- 退勤ボタン（出勤済み・退勤未済みのときのみ有効） -->
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="end">
                <input type="hidden" name="groupId" value="<%= selectedGroupId != null ? selectedGroupId : 0 %>">
                <button type="submit" class="btn btn-danger" <%= (!hasStarted || hasEnded) ? "disabled" : "" %>>
                    <%= hasEnded ? "退勤済み" : "退勤" %>
                </button>
            </form>
        </div>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/attendance/list?<%= "groupId=" + (selectedGroupId != null ? selectedGroupId : 0) %>" class="btn btn-primary">今月の勤怠一覧を見る</a>
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
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>

