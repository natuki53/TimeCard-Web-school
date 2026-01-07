<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>パスワード再設定 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
</head>
<body class="with-header">
    <header class="header">
        <a href="<%= request.getContextPath() %>/">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>
    </header>

    <div class="container">
        <h1>パスワード再設定</h1>

        <% String errorMessage = (String) request.getAttribute("errorMessage"); %>
        <% if(errorMessage != null) {%>
            <p class="error-message"><b><%= errorMessage %></b></p>
        <%} %>
        <% String msg = (String) request.getAttribute("msg"); %>
        <% if(msg != null) {%>
            <p class="success-message"><b><%= msg %></b></p>
        <%} %>

        <%
            String step = (String) request.getAttribute("step");
            String loginId = (String) request.getAttribute("loginId");
            String secretQuestion = (String) request.getAttribute("secretQuestion");
            boolean challenge = "challenge".equals(step) && loginId != null && secretQuestion != null;
        %>

        <% if (!challenge) { %>
            <form method="POST" action="<%= request.getContextPath() %>/forgot-password">
                <input type="hidden" name="action" value="lookup">
                <div class="form-group">
                    <label for="loginId">ログインID</label>
                    <input type="text" id="loginId" name="loginId" required>
                </div>

                <div class="button-group">
                    <button type="submit" class="btn btn-primary">次へ</button>
                </div>
            </form>
        <% } else { %>
            <form method="POST" action="<%= request.getContextPath() %>/forgot-password">
                <input type="hidden" name="action" value="reset">
                <input type="hidden" name="loginId" value="<%= loginId %>">

                <div class="form-group">
                    <label>秘密の質問</label>
                    <div class="help-text" style="margin-top:8px;"><b><%= secretQuestion %></b></div>
                </div>

                <div class="form-group">
                    <label for="secretAnswer">秘密の質問の答え</label>
                    <input type="password" id="secretAnswer" name="secretAnswer" required>
                </div>

                <div class="form-group">
                    <label for="password">新しいパスワード</label>
                    <input type="password" id="password" name="password" required>
                </div>

                <div class="form-group">
                    <label for="password2">新しいパスワード（確認）</label>
                    <input type="password" id="password2" name="password2" required>
                </div>

                <div class="button-group">
                    <button type="submit" class="btn btn-primary">更新</button>
                </div>
            </form>
        <% } %>

        <div class="button-group">
            <a href="<%= request.getContextPath() %>/login" class="btn btn-secondary">ログイン画面に戻る</a>
        </div>
    </div>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>


