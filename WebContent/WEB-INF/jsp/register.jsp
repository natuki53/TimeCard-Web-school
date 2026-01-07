<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>新規登録 - 勤怠管理サイト</title>
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
        <h1>新規登録</h1>
        
        <!-- エラーメッセージ表示 -->
        <% String msg = (String) request.getAttribute("msg"); %>
        <% if(msg != null) {%>
           <p class="error-message"><b><%= msg %></b></p>
        <%} %>
        <form method="POST" action="<%= request.getContextPath() %>/register">
            <div class="form-group">
                <label for="name">ユーザー名（表示名）</label>
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

            <div class="form-group">
                <label for="secretQuestion">秘密の質問</label>
                <select id="secretQuestion" name="secretQuestion" required>
                    <option value="" selected disabled>選択してください</option>
                    <option value="初めて飼ったペットの名前は？">初めて飼ったペットの名前は？</option>
                    <option value="生まれた町（市区町村）は？">生まれた町（市区町村）は？</option>
                    <option value="母親の旧姓は？">母親の旧姓は？</option>
                    <option value="小学校の名前は？">小学校の名前は？</option>
                    <option value="好きな食べ物は？">好きな食べ物は？</option>
                    <option value="好きな色は？">好きな色は？</option>
                </select>
            </div>

            <div class="form-group">
                <label for="secretAnswer">秘密の質問の答え</label>
                <input type="password" id="secretAnswer" name="secretAnswer" required>
                <p class="help-text" style="margin-top:8px;">※ 忘れないものを設定してください（再設定時に必要です）</p>
            </div>
            
            <div class="button-group">
                <button type="submit" class="btn btn-primary">登録</button>
            </div>
        </form>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/login" class="btn btn-secondary">ログイン画面に戻る</a>
        </div>
    </div>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>

