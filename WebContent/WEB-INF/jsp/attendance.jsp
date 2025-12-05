<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    // 間違い: session.getAttribute("loginUser")
    // 正しい: session.getAttribute("user") // サーブレットで"user"として設定されているため
    User loginUser = (User) session.getAttribute("user");
    // 間違い: request.getAttribute("todayAttendance")
    // 正しい: request.getAttribute("attendance") // サーブレットで"attendance"として設定されているため
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
        
        <!-- TODO: ログインユーザー名を表示 -->
        <!-- TODO: 今日の日付を表示 -->
        <div class="user-info">
        <!-- 間違い: スペースが不足している -->
        <!-- 正しい: <p><strong><%= loginUser != null ? loginUser.getName() : "ゲスト" %>さん</strong></p> -->
        <p><strong><%= loginUser != null ? loginUser.getName(): "ゲスト" %>さん</strong></p>
        <!-- 間違い: todeyStr (タイポ) -->
        <!-- 正しい: todayStr -->
        <p>今日の日付:<strong><%= todayStr %></strong></p>
        </div>
        
        <div class="attendance-info">
            <!-- 間違い: todeyAttendance (タイポ) -->
            <!-- 間違い: --:-- が文字列として引用符で囲まれていない -->
            <!-- 正しい: <%= todayAttendance != null && todayAttendance.getStartTime() != null ? todayAttendance.getStartTime().toString() : "--:--" %> -->
            <p>出勤時刻: <span class="time-display"><!-- TODO: 出勤時刻を表示 --><%= todayAttendance != null && todayAttendance.getStartTime() != null ? todayAttendance.getStartTime().toString() : "--:--" %></span></p>
            <!-- 間違い: todeyAttendance (タイポ) -->
            <!-- 間違い: --:-- が文字列として引用符で囲まれていない -->
            <!-- 正しい: <%= todayAttendance != null && todayAttendance.getEndTime() != null ? todayAttendance.getEndTime().toString() : "--:--" %> -->
            <p>退勤時刻: <span class="time-display"><!-- TODO: 退勤時刻を表示 --><%= todayAttendance != null && todayAttendance.getEndTime() != null ? todayAttendance.getEndTime().toString() : "--:--" %></span></p>
        </div>
        
        <div class="attendance-buttons">
    		<%
      		  boolean hasStarted = todayAttendance != null && todayAttendance.getStartTime() != null;
      		  boolean hasEnded = todayAttendance != null && todayAttendance.getEndTime() != null;
   			 %>
            <!-- TODO: 出勤ボタン（未出勤のときのみ有効） -->
            <!-- 間違い: ボタンのdisabled属性が実装されていない、三項演算子の構文が間違っている（文字列が引用符で囲まれていない） -->
            <!-- 正しい: 
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="start">
                <button type="submit" class="btn btn-success" <%= hasStarted ? "disabled" : "" %>>
                    <%= hasStarted ? "出勤済み" : "出勤" %>
                </button>
            </form>
            -->
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="start">
                <button type="submit" class="btn btn-success" <%= hasStarted ? "disabled" : "" %>>
                <%= hasStarted ? "出勤済み" : "出勤" %>
                </button>
            </form>
            
            <!-- TODO: 退勤ボタン（出勤済み・退勤未済みのときのみ有効） -->
            <!-- 間違い: ボタンのdisabled属性が実装されていない、三項演算子の構文が間違っている（文字列が引用符で囲まれていない） -->
            <!-- 正しい: 
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="end">
                <button type="submit" class="btn btn-danger" <%= (!hasStarted || hasEnded) ? "disabled" : "" %>>
                    <%= hasEnded ? "退勤済み" : "退勤" %>
                </button>
            </form>
            -->
            <form method="POST" action="<%= request.getContextPath() %>/attendance" style="display: inline;">
                <input type="hidden" name="action" value="end">
                <!-- 間違い: タイポ hasStaared → hasStarted -->
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

