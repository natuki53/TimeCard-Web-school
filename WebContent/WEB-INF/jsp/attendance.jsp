<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    
    User loginUser = (User) session.getAttribute("user");
    
    Attendance todayAttendance = (Attendance) request.getAttribute("attendanc");
    LocalDate today = LocalDate.now();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    String todayStr = today.format(dateFormatter);
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>勤怠打刻 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <div class="container">
        <h1>勤怠打刻</h1>
        
        
        <div class="user-info">
        
        <p><strong><%= loginUser != null ? loginUser.getName() : "ゲスト" %>さん</strong></p>
        
        <p>今日の日付:<strong><%= todayStr %></strong></p>
        </div>
        
        <div class="attendance-info">
            
            <p>出勤時刻: <span class="time-display"><%= todayAttendance != null && todayAttendance.getStartTime() != null ? todayAttendance.getStartTime().toString() : "--:--" %></span></p>
            
            <p>退勤時刻: <span class="time-display"><%= todayAttendance != null && todayAttendance.getEndTime() != null ? todayAttendance.getEndTime().toString() : "--:--" %></span></p>
        </div>
        
        <div class="attendance-buttons">
    		<%
      		  boolean hasStarted = todayAttendance != null && todayAttendance.getStartTime() != null;
      		  boolean hasEnded = todayAttendance != null && todayAttendance.getEndTime() != null;
   			 %>
            
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="start">
                <button type="submit" class="btn btn-success" <%= hasStarted ? "disabled" : "" %>>
                <%= hasStarted ? "出勤済み" : "出勤" %>
                </button>
            </form>
            
            
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="end">
                <button type="submit" class="btn btn-danger" <%= (!hasStarted || hasEnded) ? "disabled" : "" %>>
                <%= hasEnded ? "退勤済み" : "退勤" %>
                </button>
            </form>
        </div>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/attendance/list" class="btn btn-primary">今月の勤怠一覧を見る</a>
            <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary">ログアウト</a>
        </div>
    </div>
</body>
</html>

