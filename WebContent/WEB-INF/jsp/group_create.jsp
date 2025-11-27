<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%
    // ログイン済みかチェック
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // エラーメッセージを取得
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>グループ作成 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="with-header">
    <!-- ヘッダー -->
    <header class="header">
        <a href="<%= request.getContextPath() %>/dashboard">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>
        <nav class="header-nav">
            <a href="<%= request.getContextPath() %>/dashboard" class="nav-link">ダッシュボード</a>
            <a href="<%= request.getContextPath() %>/attendance" class="nav-link">勤怠打刻</a>
            <a href="<%= request.getContextPath() %>/attendance-list" class="nav-link">勤怠一覧</a>
            <a href="<%= request.getContextPath() %>/logout" class="nav-link">ログアウト</a>
        </nav>
        <div class="user-info">
            <span class="user-name"><%= loginUser.getName() %>さん</span>
        </div>
    </header>

    <div class="container">
        <h1>グループ作成</h1>
        
        <!-- エラーメッセージ表示 -->
        <% if (errorMessage != null) { %>
            <div class="error-message">
                <%= errorMessage %>
            </div>
        <% } %>
        
        <div class="form-container">
            <form method="post" action="<%= request.getContextPath() %>/group/create">
                <div class="form-group">
                    <label for="groupName">グループ名 <span class="required">*</span></label>
                    <input type="text" id="groupName" name="groupName" 
                           value="<%= request.getParameter("groupName") != null ? request.getParameter("groupName") : "" %>"
                           placeholder="例: 開発チーム" required maxlength="100">
                </div>
                
                <div class="form-group">
                    <label for="description">説明</label>
                    <textarea id="description" name="description" rows="4" 
                              placeholder="グループの説明を入力してください（任意）" maxlength="500"><%= request.getParameter("description") != null ? request.getParameter("description") : "" %></textarea>
                </div>
                
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">グループを作成</button>
                    <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">キャンセル</a>
                </div>
            </form>
        </div>
        
        <div class="info-box">
            <h3>グループ作成について</h3>
            <ul>
                <li>作成したグループの管理者になります</li>
                <li>管理者はメンバーの追加・削除ができます</li>
                <li>管理者はグループメンバーの勤怠状況を確認できます</li>
                <li>グループ名は後から変更できません</li>
            </ul>
        </div>
    </div>
</body>
</html>
