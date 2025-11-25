<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ログイン - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <div class="container">
        <h1>ログイン</h1>
        
        <!-- メッセージ表示 -->
        <% String msg = (String) request.getAttribute("msg"); %>
        <% if(msg != null) {%>
           <p class="success-message"><b><%= msg %></b></p>
        <%} %>
        <% String errorMessage = (String) request.getAttribute("errorMessage"); %>
        <% if(errorMessage != null) {%>
           <p class="error-message"><b><%= errorMessage %></b></p>
        <%} %>
        
        <form method="POST" action="<%= request.getContextPath() %>/login">
            <div class="form-group">
                <label for="loginId">ログインID</label>
                <input type="text" id="loginId" name="loginId" required>
            </div>
            
            <div class="form-group">
                <label for="password">パスワード</label>
                <input type="password" id="password" name="password" required>
            </div>
            
            <div class="button-group">
                <button type="submit" class="btn btn-primary">ログイン</button>
            </div>
        </form>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/register" class="btn btn-secondary">新規登録はこちら</a>
        </div>
    </div>
</body>
</html>

