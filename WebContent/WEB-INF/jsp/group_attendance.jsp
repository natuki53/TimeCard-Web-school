<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.Group, model.GroupMember, model.Attendance, java.util.List, java.util.Map, java.time.YearMonth, java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<%
    // ログイン済みかチェック
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // グループ情報とメンバー一覧を取得
    Group group = (Group) request.getAttribute("group");
    @SuppressWarnings("unchecked")
    List<GroupMember> members = (List<GroupMember>) request.getAttribute("members");
    @SuppressWarnings("unchecked")
    Map<Integer, List<Attendance>> memberAttendances = (Map<Integer, List<Attendance>>) request.getAttribute("memberAttendances");
    @SuppressWarnings("unchecked")
    Map<Integer, Integer> memberAttendanceDays = (Map<Integer, Integer>) request.getAttribute("memberAttendanceDays");
    @SuppressWarnings("unchecked")
    Map<Integer, String> memberTotalWorkTime = (Map<Integer, String>) request.getAttribute("memberTotalWorkTime");
    
    YearMonth targetYearMonth = (YearMonth) request.getAttribute("targetYearMonth");
    YearMonth prevMonth = (YearMonth) request.getAttribute("prevMonth");
    YearMonth nextMonth = (YearMonth) request.getAttribute("nextMonth");
    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
    String currentMonthStr = targetYearMonth.format(monthFormatter);
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>グループ勤怠確認 - <%= group.getName() %> - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
    <script defer src="<%= request.getContextPath() %>/js/notifications.js"></script>
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
            <a href="<%= request.getContextPath() %>/dm">DM</a>
        </nav>
        
        <div class="header-user">
            <span class="header-user-name"><%= loginUser.getName() %>さん</span>
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
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser.getName() %>さん</a>
        <a href="<%= request.getContextPath() %>/logout">ログアウト</a>
    </div>
    </header>

    <div class="container">
        <h1>グループ勤怠確認</h1>
        
        <!-- グループ情報 -->
        <div class="group-info-section">
            <h2><%= group.getName() %></h2>
            <% if (group.getDescription() != null && !group.getDescription().isEmpty()) { %>
                <p class="group-description"><%= group.getDescription() %></p>
            <% } %>
        </div>
        
        <!-- 月選択ナビゲーション -->
        <div class="month-navigation">
            <a href="<%= request.getContextPath() %>/group/attendance?id=<%= group.getId() %>&yearMonth=<%= prevMonth.toString() %>">&lt; 前月</a>
            <span class="current-month"><%= currentMonthStr %></span>
            <a href="<%= request.getContextPath() %>/group/attendance?id=<%= group.getId() %>&yearMonth=<%= nextMonth.toString() %>">次月 &gt;</a>
        </div>
        
        <!-- 管理者向けアクション -->
        <% if (isAdmin != null && isAdmin) { %>
            <div class="admin-actions">
                <a href="<%= request.getContextPath() %>/group/manage?id=<%= group.getId() %>" 
                   class="btn btn-primary">グループ管理</a>
            </div>
        <% } %>
        
        <!-- メンバー勤怠一覧 -->
        <section class="dashboard-section">
            <h2>メンバー勤怠状況（<%= currentMonthStr %>）</h2>
            
            <% if (members != null && !members.isEmpty()) { %>
                <div class="group-attendance-table">
                    <% for (GroupMember member : members) { %>
                        <div class="member-attendance-section">
                            <h3 class="member-name">
                                <%= member.getUserName() %>
                                <% if (member.getUserId() == group.getAdminUserId()) { %>
                                    <span class="role-admin">管理者</span>
                                <% } else { %>
                                    <span class="role-member">メンバー</span>
                                <% } %>
                            </h3>
                            
                            <% 
                                // 管理者でない場合、他のメンバーの勤怠は表示しない
                                if (!isAdmin && member.getUserId() != loginUser.getId()) {
                            %>
                                <div class="access-restricted">
                                    <p class="restriction-message">管理者ではないため閲覧できません。</p>
                                </div>
                            <% 
                                } else {
                                    List<Attendance> attendances = memberAttendances.get(member.getUserId());
                                    if (attendances != null && !attendances.isEmpty()) {
                            %>
                                <table class="attendance-table">
                                    <thead>
                                        <tr>
                                            <th>日付</th>
                                            <th>出勤時刻</th>
                                            <th>退勤時刻</th>
                                            <th>状況</th>
                                            <th>修正</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Attendance attendance : attendances) { %>
                                            <%
                                                // 管理者が「一般ユーザー修正」の黄色行にホバーしたときに差分を見せる
                                                String hoverTitle = null;
                                                if (isAdmin != null && isAdmin && attendance.isCorrected() && !attendance.isCorrectedByAdmin()) {
                                                    String fromStart = attendance.getPrevStartTime() != null ? attendance.getPrevStartTime().toString() : "--:--";
                                                    if (fromStart.length() >= 5 && !"--:--".equals(fromStart)) fromStart = fromStart.substring(0, 5);
                                                    String toStart = attendance.getStartTime() != null ? attendance.getStartTime().toString() : "--:--";
                                                    if (toStart.length() >= 5 && !"--:--".equals(toStart)) toStart = toStart.substring(0, 5);
                                                    String fromEnd = attendance.getPrevEndTime() != null ? attendance.getPrevEndTime().toString() : "--:--";
                                                    if (fromEnd.length() >= 5 && !"--:--".equals(fromEnd)) fromEnd = fromEnd.substring(0, 5);
                                                    String toEnd = attendance.getEndTime() != null ? attendance.getEndTime().toString() : "--:--";
                                                    if (toEnd.length() >= 5 && !"--:--".equals(toEnd)) toEnd = toEnd.substring(0, 5);
                                                    hoverTitle = "修正内容: 出勤 " + fromStart + "→" + toStart + " / 退勤 " + fromEnd + "→" + toEnd;
                                                }
                                            %>
                                            <tr
                                                class="<%=
                                                    (attendance.isCancelled() && !attendance.isCancelledByAdmin()) ? "attendance-cancelled-by-member" :
                                                    (attendance.isCorrected() && !attendance.isCorrectedByAdmin()) ? "attendance-corrected-by-member" : ""
                                                %>"
                                                <%= hoverTitle != null ? "title=\"" + hoverTitle + "\"" : "" %>
                                            >
                                                <td><%= attendance.getWorkDate() %></td>
                                                <td>
                                                    <%
                                                        String gst = "未打刻";
                                                        if (!attendance.isCancelled() && attendance.getStartTime() != null) {
                                                            gst = attendance.getStartTime().toString();
                                                            if (gst.length() >= 5) gst = gst.substring(0, 5);
                                                        }
                                                    %>
                                                    <%= gst %>
                                                </td>
                                                <td>
                                                    <%
                                                        String get = "未打刻";
                                                        if (!attendance.isCancelled() && attendance.getEndTime() != null) {
                                                            get = attendance.getEndTime().toString();
                                                            if (get.length() >= 5) get = get.substring(0, 5);
                                                        }
                                                    %>
                                                    <%= attendance.isCancelled() ? "未打刻" : get %>
                                                    <% if (attendance.isCancelled() && !attendance.isCancelledByAdmin()) { %>
                                                        <div class="cancelled-message">この勤怠は取り消しされました。</div>
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <% 
                                                        if (attendance.getStartTime() != null && attendance.getEndTime() != null) {
                                                    %>
                                                        <span class="status-complete">完了</span>
                                                    <% 
                                                        } else if (attendance.getStartTime() != null) {
                                                    %>
                                                        <span class="status-partial">出勤のみ</span>
                                                    <% 
                                                        } else {
                                                    %>
                                                        <span class="status-incomplete">未出勤</span>
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
                                                            <input type="hidden" name="userId" value="<%= member.getUserId() %>">
                                                            <input type="hidden" name="workDate" value="<%= attendance.getWorkDate().toString() %>">
                                                            <input type="hidden" name="groupId" value="<%= group.getId() %>">
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
                                                        <input type="hidden" name="userId" value="<%= member.getUserId() %>">
                                                        <input type="hidden" name="workDate" value="<%= attendance.getWorkDate().toString() %>">
                                                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                                        <button type="submit" class="btn btn-sm btn-danger">勤怠削除</button>
                                                    </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                                
                                <!-- 統計情報 -->
                                <div class="member-stats">
                                    <%
                                        int days = 0;
                                        if (memberAttendanceDays != null && memberAttendanceDays.get(member.getUserId()) != null) {
                                            days = memberAttendanceDays.get(member.getUserId());
                                        }
                                        String work = "00:00";
                                        if (memberTotalWorkTime != null && memberTotalWorkTime.get(member.getUserId()) != null) {
                                            work = memberTotalWorkTime.get(member.getUserId());
                                        }
                                    %>
                                    <span class="stat-item">出勤日数: <%= days %>日</span>
                                    <span class="stat-item">合計勤務時間: <%= work %></span>
                                </div>
                                    <% } else { %>
                                        <div class="no-attendance">
                                            <p>この月の勤怠記録はありません</p>
                                        </div>
                                    <% } %>
                                <% } %>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="no-records">
                    <p>グループにメンバーがいません</p>
                </div>
            <% } %>
        </section>
        
        <!-- 戻るリンク -->
        <div class="back-link">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">ダッシュボードに戻る</a>
        </div>
    </div>

    <script>
        function toggleMobileMenu() {
            var menu = document.getElementById('mobileMenu');
            menu.classList.toggle('show');
        }
        
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
