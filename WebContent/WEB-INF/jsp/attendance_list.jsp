<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    List<Attendance> attendanceList = (List<Attendance>) request.getAttribute("attendanceList");
    Integer year = (Integer) request.getAttribute("year");
    Integer month = (Integer) request.getAttribute("month");
    Integer attendanceDays = (Integer) request.getAttribute("attendanceDays");
    
    // 現在の年月が設定されていない場合は現在の日付を使用
    if (year == null || month == null) {
        LocalDate now = LocalDate.now();
        year = now.getYear();
        month = now.getMonthValue();
    }
    
    // 前月・次月の計算
    LocalDate currentMonth = LocalDate.of(year, month, 1);
    LocalDate prevMonth = currentMonth.minusMonths(1);
    LocalDate nextMonth = currentMonth.plusMonths(1);
    
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
    String currentMonthStr = currentMonth.format(monthFormatter);
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
        
        <div class="user-info">
            <p><strong><%= loginUser != null ? loginUser.getName() : "ゲスト" %>さんの勤怠一覧</strong></p>
        </div>
        
        <div class="month-navigation">
            <a href="?year=<%= prevMonth.getYear() %>&month=<%= prevMonth.getMonthValue() %>">前の月</a>
            
            <span class="current-month"><%= currentMonthStr %></span>
            
            <a href="?year=<%= nextMonth.getYear() %>&month=<%= nextMonth.getMonthValue() %>">次の月</a>
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
                <%
                    if (attendanceList != null && !attendanceList.isEmpty()) {
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        for (Attendance attendance : attendanceList) {
                %>
                <tr>
                    <td><%= attendance.getWorkDate().format(dateFormatter) %></td>
                    <td><%= attendance.getStartTime() != null ? attendance.getStartTime().toString() : "--:--" %></td>
                    <td><%= attendance.getEndTime() != null ? attendance.getEndTime().toString() : "--:--" %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="3" style="text-align: center; color: #666;">この月の勤怠データはありません</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
        
        <div class="stats">
            <p>出勤日数: <strong><%= attendanceDays != null ? attendanceDays : 0 %></strong> 日</p>
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

