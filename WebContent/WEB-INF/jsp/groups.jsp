<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.Group, java.util.List" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
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
    <title>グループ - 勤怠管理サイト</title>
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
        <h1>グループ</h1>

        <section class="dashboard-section">
            <h2>クイックアクション</h2>
            <div class="quick-actions">
                <a href="<%= request.getContextPath() %>/group/create" class="btn btn-success">グループ作成</a>
            </div>
        </section>

        <section class="dashboard-section">
            <h2>管理しているグループ</h2>
            <% if (adminGroups != null && !adminGroups.isEmpty()) { %>
                <div class="group-list">
                    <% for (Group g : adminGroups) { %>
                        <div class="group-card admin-group">
                            <div class="group-info">
                                <h4 class="group-name"><%= g.getName() %></h4>
                                <p class="group-description"><%= g.getDescription() != null ? g.getDescription() : "" %></p>
                            </div>
                            <div class="group-actions">
                                <a href="<%= request.getContextPath() %>/group/manage?id=<%= g.getId() %>" class="btn btn-sm btn-primary">管理</a>
                                <a href="<%= request.getContextPath() %>/group/chat?id=<%= g.getId() %>" class="btn btn-sm btn-secondary">チャット</a>
                                <a href="<%= request.getContextPath() %>/group/attendance?id=<%= g.getId() %>" class="btn btn-sm btn-success">勤怠</a>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="no-groups">管理しているグループはありません</p>
            <% } %>
        </section>

        <section class="dashboard-section">
            <h2>参加しているグループ</h2>
            <% if (memberGroups != null && !memberGroups.isEmpty()) { %>
                <div class="group-list">
                    <% for (Group g : memberGroups) { %>
                        <div class="group-card member-group">
                            <div class="group-info">
                                <h4 class="group-name"><%= g.getName() %></h4>
                                <p class="group-description"><%= g.getDescription() != null ? g.getDescription() : "" %></p>
                            </div>
                            <div class="group-actions">
                                <a href="<%= request.getContextPath() %>/group/info?id=<%= g.getId() %>" class="btn btn-sm btn-secondary">詳細</a>
                                <a href="<%= request.getContextPath() %>/group/chat?id=<%= g.getId() %>" class="btn btn-sm btn-secondary">チャット</a>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="no-groups">参加しているグループはありません</p>
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


