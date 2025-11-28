<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>勤怠一覧 - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <div class="container">
        <h1>勤怠一覧</h1>
        
        <!-- TODO: ログインユーザー名を表示 -->
        <% User user = (User)session.getAttribute("user");%>
        <% if(user != null) {%>
          <p>ユーザー：<%= user.getName(); %>さん</p>
        <% } %>
        <% 
          Integer year = (Integer)request.getAttribute("year"); 
          Integer month = (Integer)request.getAttribute("month");
         %>
        <div class="month-navigation">
            <!-- TODO: 前の月へのリンク -->
            <%
              int prevYear = year;
              int prevMonth = month - 1;
              if (prevMonth == 0){
                prevMonth = 12;
                prevYear = prevYear-1;
               }
             %>
            <a href="?year=<%= prevYear %>&month=<%= prevMonth%> ">前の月</a>
            <!-- TODO: YYYY年MM月を表示 -->
            <span class="current-month"><%= year %>年<%= month %>月</span>
            
            <!-- TODO: 次の月へのリンク -->
            <%
              int nextYear = year;
              int nextMonth = month + 1;
              if (prevMonth == 13){
                nextMonth = 1;
                nextYear = nextYear + 1;
               }
             %>
            <a href="?year=<%= nextYear %>&month=<%= nextMonth%>">次の月</a>
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
                <% 
                  List<Attendance> attendanceList =(List<Attendance>)request.getAttribute("attendanceList");
                  int WorkCount = 0:
                %> 
                <%for(Attendance a:attendanceList){%>
                    <tr>
                      <td><%= a.getWorkDate();%></td>
                      <td><%= a.getStartTime(); %></td>
                      <td><%= a.getEndTime(); %></td>
                    </tr>
                    WorkCount = WorkCount+1;
                <%} %>
            </tbody>
        </table>
        
        <div class="stats">
            <p>出勤日数: <!-- TODO: 出勤日数を表示 --><%= WorkCount %> 日</p>
        </div>
        
        <div class="button-group">
            <a href="<%= request.getContextPath() %>/attendance" class="btn btn-primary">打刻画面に戻る</a>
            <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary">ログアウト</a>
        </div>
    </div>
</body>
</html>

