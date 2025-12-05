<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Attendance" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

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
        <!-- 間違い: セミコロンが余分（JSP式では不要） -->
        <!-- 正しい: <%= user.getName()%> -->
        <% User user = (User)session.getAttribute("user");%>
        <% 
          if(user != null) {%>
          <p>ユーザー：<%= user.getName()%>さん</p>
         <% }
         %> 
 
        <% 
          Integer year = (Integer)request.getAttribute("year"); 
          Integer month = (Integer)request.getAttribute("month");
          // 間違い: yearとmonthがnullの場合の処理がない（NullPointerExceptionが発生する可能性がある）
          // 正しい: 以下のようにnullチェックとデフォルト値の設定が必要
          
          if (year == null || month == null) {
              LocalDate now = LocalDate.now();
              year = now.getYear();
              month = now.getMonthValue();
          }
          
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
            <!-- 間違い: prevMonth == 13 のチェックが間違っている（nextMonth == 13 であるべき） -->
            <!-- 正しい: 
            <%
              int nextYear = year;
              int nextMonth = month + 1;
              if (nextMonth == 13){
                nextMonth = 1;
                nextYear = nextYear + 1;
              }
            %>
            または、LocalDateを使用する方が安全:
            <%
              LocalDate currentMonth = LocalDate.of(year, month, 1);
              LocalDate nextMonth = currentMonth.plusMonths(1);
            %>
            -->
            <%
              int nextYear = year;
              int nextMonth = month + 1;
              if (nextMonth == 13){
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
                  // 間違い: コロンがセミコロンであるべき（int WorkCount = 0: → int WorkCount = 0;）
                  // 間違い: WorkCountの計算ロジックが間違っている（JSPスクリプトレットの外にJavaコードがある）
                  // 間違い: getWorkDate()のフォーマット処理がない
                  // 間違い: nullチェックがない
                  // 正しい: 
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
                  int WorkCount = 0;
                  WorkCount = WorkCount + 1;
                %> 
                <%-- 
                <%for(Attendance a:attendanceList){%>
                    <tr>
                      <!-- 間違い: セミコロンが余分（JSP式では不要） -->
                      <!-- 間違い: フォーマット処理がない -->
                      <td><%= a.getWorkDate()%></td>
                      <td><%= a.getStartTime() %></td>
                      <td><%= a.getEndTime() %></td>
                    </tr>
                    <!-- 間違い: この行はJSPスクリプトレットの外にある（構文エラー） -->
                     WorkCount = WorkCount+1;
                <%} %>
                --%>
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

