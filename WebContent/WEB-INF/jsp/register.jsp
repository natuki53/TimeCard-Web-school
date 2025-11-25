<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>新規登録 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <div class="container">
        <h1>新規登録</h1>
        
        <!-- エラーメッセージ表示 -->
        <% String msg = (String) request.getAttribute("msg"); %>
        <% if(msg != null) {%>
           <p class="error-message"><b><%= msg %></b></p>
        <%} %>
        <form method="POST" action="<%= request.getContextPath() %>/register">
            <div class="form-group">
                <label for="name">ユーザー名</label>
                <input type="text" id="name" name="name" required>
            </div>
            
            <div class="form-group">
                <label for="loginId">ログインID</label>
                <input type="text" id="loginId" name="loginId" required>
            </div>
            
            <div class="form-group">
                <label for="password">パスワード</label>
                <input type="password" id="password" name="password" required>
            </div>
            
            <div class="button-group">
                <button type="submit" class="btn btn-primary">登録</button>
            </div>
        </form>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/login" class="btn btn-secondary">ログイン画面に戻る</a>
        </div>
    </div>
</body>
</html>

