<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.Attendance, model.Group, java.util.List, java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    // ログイン済みかチェック
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // 今日の日付
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    String todayStr = today.format(formatter);
    
    // 今日の勤怠情報
    Attendance todayAttendance = (Attendance) request.getAttribute("todayAttendance");
    
    // 勤怠履歴
    @SuppressWarnings("unchecked")
    List<Attendance> recentAttendances = (List<Attendance>) request.getAttribute("recentAttendances");
    @SuppressWarnings("unchecked")
    List<Attendance> thisMonthAttendances = (List<Attendance>) request.getAttribute("thisMonthAttendances");
    Integer thisMonthYear = (Integer) request.getAttribute("thisMonthYear");
    Integer thisMonthMonth = (Integer) request.getAttribute("thisMonthMonth");
    
    // グループ情報
    @SuppressWarnings("unchecked")
    List<Group> adminGroups = (List<Group>) request.getAttribute("adminGroups");
    @SuppressWarnings("unchecked")
    List<Group> memberGroups = (List<Group>) request.getAttribute("memberGroups");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ダッシュボード - 勤怠管理サイト</title>
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

    <div class="container">
        <h1>ダッシュボード</h1>
        <p class="welcome-message">おかえりなさい、<%= loginUser.getName() %>さん</p>
        
        <!-- サクセスメッセージ表示 -->
        <% 
            String successMessage = (String) request.getAttribute("successMessage");
            if (successMessage != null) { 
        %>
            <div class="success-message">
                <%= successMessage %>
            </div>
        <% } %>
        
        <!-- 今日の勤怠状況 -->
        <section class="dashboard-section">
            <h2>今日の勤怠状況（<%= todayStr %>）</h2>
            <div class="today-status">
                <% if (todayAttendance != null) { %>
                    <%
                        String dst = todayAttendance.getStartTime() != null ? todayAttendance.getStartTime().toString() : "未打刻";
                        if (dst.length() >= 5 && !"未打刻".equals(dst)) dst = dst.substring(0, 5);
                        String det = todayAttendance.getEndTime() != null ? todayAttendance.getEndTime().toString() : "未打刻";
                        if (det.length() >= 5 && !"未打刻".equals(det)) det = det.substring(0, 5);
                    %>
                    <div class="status-item">
                        <span class="status-label">出勤時刻:</span>
                        <span class="status-value">
                            <%= dst %>
                        </span>
                    </div>
                    <div class="status-item">
                        <span class="status-label">退勤時刻:</span>
                        <span class="status-value">
                            <%= det %>
                        </span>
                    </div>
                <% } else { %>
                    <div class="status-item">
                        <span class="status-value no-record">本日の勤怠記録はありません</span>
                    </div>
                <% } %>
            </div>
        </section>

        <!-- クイックアクション -->
        <section class="dashboard-section">
            <h2>クイックアクション</h2>
            <div class="quick-actions">
                <a href="<%= request.getContextPath() %>/attendance" class="btn btn-primary">勤怠打刻</a>
                <a href="<%= request.getContextPath() %>/attendance/list" class="btn btn-secondary">勤怠一覧</a>
                <a href="<%= request.getContextPath() %>/group/create" class="btn btn-success">グループ作成</a>
                <a href="<%= request.getContextPath() %>/profile" class="btn btn-secondary">プロフィール</a>
            </div>
        </section>

        <!-- グループ管理 -->
        <section class="dashboard-section">
            <h2>グループ管理</h2>
            
            <!-- 管理しているグループ -->
            <div class="group-category">
                <h3>管理しているグループ</h3>
                <% if (adminGroups != null && !adminGroups.isEmpty()) { %>
                    <div class="group-list">
                        <% for (Group group : adminGroups) { %>
                            <div class="group-card admin-group">
                                <div class="group-info">
                                    <h4 class="group-name"><%= group.getName() %></h4>
                                    <p class="group-description"><%= group.getDescription() != null ? group.getDescription() : "" %></p>
                                </div>
                                <div class="group-actions">
                                    <a href="<%= request.getContextPath() %>/group/manage?id=<%= group.getId() %>" class="btn btn-sm btn-primary">管理</a>
                                    <a href="<%= request.getContextPath() %>/group/chat?id=<%= group.getId() %>" class="btn btn-sm btn-secondary">チャット</a>
                                    <a href="<%= request.getContextPath() %>/group/attendance?id=<%= group.getId() %>" class="btn btn-sm btn-secondary">勤怠確認</a>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <p class="no-groups">管理しているグループはありません</p>
                <% } %>
            </div>

            <!-- 参加しているグループ -->
            <div class="group-category">
                <h3>参加しているグループ</h3>
                <% if (memberGroups != null && !memberGroups.isEmpty()) { %>
                    <div class="group-list">
                        <% for (Group group : memberGroups) { %>
                            <div class="group-card member-group">
                                <div class="group-info">
                                    <h4 class="group-name"><%= group.getName() %></h4>
                                    <p class="group-description"><%= group.getDescription() != null ? group.getDescription() : "" %></p>
                                </div>
                                <div class="group-actions">
                                    <a href="<%= request.getContextPath() %>/group/info?id=<%= group.getId() %>" class="btn btn-sm btn-secondary">詳細</a>
                                    <a href="<%= request.getContextPath() %>/group/chat?id=<%= group.getId() %>" class="btn btn-sm btn-secondary">チャット</a>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <p class="no-groups">参加しているグループはありません</p>
                <% } %>
            </div>
        </section>

        <!-- 今月の勤怠状況 -->
        <section class="dashboard-section">
            <h2>今月の勤怠状況</h2>
            <% if (thisMonthAttendances != null && !thisMonthAttendances.isEmpty()) { %>
                <div class="recent-attendance">
                    <table class="attendance-table">
                        <thead>
                            <tr>
                                <th>日付</th>
                                <th>グループ</th>
                                <th>出勤時刻</th>
                                <th>退勤時刻</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Attendance attendance : thisMonthAttendances) { %>
                                <tr>
                                    <td><%= attendance.getWorkDate() %></td>
                                    <td><%= attendance.getGroupName() != null ? attendance.getGroupName() : "グループなし" %></td>
                                    <%
                                        String mst = attendance.getStartTime() != null ? attendance.getStartTime().toString() : "未打刻";
                                        if (mst.length() >= 5 && !"未打刻".equals(mst)) mst = mst.substring(0, 5);
                                        String met = attendance.getEndTime() != null ? attendance.getEndTime().toString() : "未打刻";
                                        if (met.length() >= 5 && !"未打刻".equals(met)) met = met.substring(0, 5);
                                    %>
                                    <td><%= mst %></td>
                                    <td><%= met %></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
                <div class="button-group" style="margin-top: 12px;">
                    <a href="<%= request.getContextPath() %>/attendance/list?year=<%= thisMonthYear != null ? thisMonthYear : LocalDate.now().getYear() %>&month=<%= thisMonthMonth != null ? thisMonthMonth : LocalDate.now().getMonthValue() %>" class="btn btn-secondary">
                        今月の勤怠一覧へ
                    </a>
                </div>
            <% } else { %>
                <p class="no-records">今月の勤怠履歴がありません</p>
            <% } %>
        </section>

        <!-- 最近の勤怠履歴 -->
        <section class="dashboard-section">
            <h2>最近の勤怠履歴</h2>
            <% if (recentAttendances != null && !recentAttendances.isEmpty()) { %>
                <div class="recent-attendance">
                    <table class="attendance-table">
                        <thead>
                            <tr>
                                <th>日付</th>
                                <th>グループ</th>
                                <th>出勤時刻</th>
                                <th>退勤時刻</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Attendance attendance : recentAttendances) { %>
                                <tr>
                                    <td><%= attendance.getWorkDate() %></td>
                                    <td><%= attendance.getGroupName() != null ? attendance.getGroupName() : "グループなし" %></td>
                                    <%
                                        String rst = attendance.getStartTime() != null ? attendance.getStartTime().toString() : "未打刻";
                                        if (rst.length() >= 5 && !"未打刻".equals(rst)) rst = rst.substring(0, 5);
                                        String ret = attendance.getEndTime() != null ? attendance.getEndTime().toString() : "未打刻";
                                        if (ret.length() >= 5 && !"未打刻".equals(ret)) ret = ret.substring(0, 5);
                                    %>
                                    <td><%= rst %></td>
                                    <td><%= ret %></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <p class="no-records">勤怠履歴がありません</p>
            <% } %>
        </section>
    </div>

    <script>
        function toggleMobileMenu() {
            var menu = document.getElementById('mobileMenu');
            menu.classList.toggle('show');
        }
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>
