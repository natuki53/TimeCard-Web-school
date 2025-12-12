<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>プライバシー / Cookie - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
</head>
<body class="<%= loginUser != null ? "with-header" : "with-header" %>">
    <!-- ヘッダー（ログイン有無で出し分け） -->
    <header class="header">
        <a href="<%= request.getContextPath() %>/<%= loginUser != null ? "dashboard" : "" %>">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>

        <% if (loginUser != null) { %>
        <nav class="header-nav">
            <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
            <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
            <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
            <a href="<%= request.getContextPath() %>/groups">グループ</a>
        </nav>
        <div class="header-user">
            <span class="header-user-name"><%= loginUser.getName() %>さん</span>
            <a href="<%= request.getContextPath() %>/logout" class="header-logout">ログアウト</a>
        </div>
        <% } %>

        <div class="hamburger" onclick="toggleMobileMenu()">
            <span></span><span></span><span></span>
        </div>
    </header>

    <% if (loginUser != null) { %>
    <div class="mobile-menu" id="mobileMenu">
        <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
        <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
        <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
        <a href="<%= request.getContextPath() %>/groups">グループ</a>
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser.getName() %>さん</a>
        <a href="<%= request.getContextPath() %>/logout">ログアウト</a>
    </div>
    <% } %>

    <div class="container">
        <h1>プライバシー / Cookieについて</h1>

        <section class="dashboard-section">
            <h2>Cookieの利用目的</h2>
            <p>当サイトでは、以下の目的でCookieを使用します。</p>
            <ul class="policy-list">
                <li>ログイン状態の維持（セッション管理）</li>
                <li>「ログイン状態を保持する」を選択した場合のログイン維持</li>
                <li>セキュリティ上必要な制御</li>
            </ul>
        </section>

        <section class="dashboard-section">
            <h2>Cookieの種類</h2>
            <ul class="policy-list">
                <li><strong>必須Cookie</strong>：サイトの動作に必要なCookie（例：セッションCookie）</li>
                <li><strong>任意Cookie</strong>：利便性向上のためのCookie（例：ログイン保持）</li>
            </ul>
        </section>

        <section class="dashboard-section">
            <h2>同意の扱い</h2>
            <p>Cookie利用通知は初回アクセス時に表示されます。「同意する」を押すと以降表示されません。</p>
        </section>

        <div class="button-group">
            <a href="<%= request.getContextPath() %>/" class="btn btn-secondary">トップへ戻る</a>
        </div>
    </div>

    <script>
        function toggleMobileMenu() {
            var menu = document.getElementById('mobileMenu');
            if (!menu) return;
            menu.classList.toggle('show');
        }
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>


