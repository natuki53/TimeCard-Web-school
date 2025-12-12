<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%
    // ログイン済みかチェック
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser != null) {
        // ログイン済みならダッシュボードへリダイレクト
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
</head>
<body class="with-header">
    <!-- シンプルヘッダー（ロゴのみ） -->
    <header class="header">
        <a href="<%= request.getContextPath() %>/">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>
    </header>
    <div class="container">
        <h1>勤怠管理サイト</h1>
        <p>出勤・退勤を簡単に記録できるWebアプリケーションです。</p>
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/login" class="btn btn-primary">ログイン</a>
            <a href="<%= request.getContextPath() %>/register" class="btn btn-secondary">新規登録</a>
        </div>
    </div>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>

