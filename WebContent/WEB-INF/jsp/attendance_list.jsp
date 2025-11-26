<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="java.util.List" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>勤怠一覧 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="with-header">
    <!-- ヘッダー -->
    <header class="header">
        <a href="<%= request.getContextPath() %>/attendance">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>
        
        <nav class="header-nav">
            <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
            <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
        </nav>
        
        <div class="header-user">
            <span class="header-user-name"><%= loginUser != null ? loginUser.getName() + "さん" : "ゲスト" %></span>
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
        <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
        <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser != null ? loginUser.getName() + "さん" : "ゲスト" %></a>
        <a href="<%= request.getContextPath() %>/logout" style="color: #e74c3c;">ログアウト</a>
    </div>
    <div class="container">
        <h1>勤怠一覧</h1>
        
        <!-- TODO: ログインユーザー名を表示 -->
        
        <div class="month-navigation">
            <!-- TODO: 前の月へのリンク -->
            <a href="?year=<!-- TODO: 前の年 -->&month=<!-- TODO: 前の月 -->">前の月</a>
            
            <span class="current-month"><!-- TODO: YYYY年MM月を表示 --></span>
            
            <!-- TODO: 次の月へのリンク -->
            <a href="?year=<!-- TODO: 次の年 -->&month=<!-- TODO: 次の月 -->">次の月</a>
        </div>
        
        <table>
            <thead>
                <tr>
                    <th>日付</th>
                    <th>出勤時刻</th>
                    <th>退勤時刻</th>
                </tr>
            </thead>
            <tbody>
                <!-- TODO: 勤怠一覧をループで表示 -->
                <!-- 
                <tr>
                    <td>2025-11-01</td>
                    <td>09:00</td>
                    <td>18:00</td>
                </tr>
                -->
            </tbody>
        </table>
        
        <div class="stats">
            <p>出勤日数: <!-- TODO: 出勤日数を表示 --> 日</p>
        </div>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/attendance" class="btn btn-primary">打刻画面に戻る</a>
        </div>
    </div>
    
    <script>
        function toggleMobileMenu() {
            const mobileMenu = document.getElementById('mobileMenu');
            mobileMenu.classList.toggle('active');
        }
        
        // 画面サイズが変わったときにモバイルメニューを閉じる
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                document.getElementById('mobileMenu').classList.remove('active');
            }
        });
    </script>
</body>
</html>

