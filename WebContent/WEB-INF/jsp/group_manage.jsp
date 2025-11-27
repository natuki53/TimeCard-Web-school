<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.Group, model.GroupMember, java.util.List" %>
<%
    // ログイン済みかチェック
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // グループ情報とメンバー一覧を取得
    Group group = (Group) request.getAttribute("group");
    @SuppressWarnings("unchecked")
    List<GroupMember> members = (List<GroupMember>) request.getAttribute("members");
    
    // メッセージを取得
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>グループ管理 - <%= group.getName() %> - 勤怠管理サイト</title>
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
        <h1>グループ管理</h1>
        
        <!-- グループ情報 -->
        <div class="group-info-section">
            <h2><%= group.getName() %></h2>
            <% if (group.getDescription() != null && !group.getDescription().isEmpty()) { %>
                <p class="group-description"><%= group.getDescription() %></p>
            <% } %>
        </div>
        
        <!-- メッセージ表示 -->
        <% if (successMessage != null) { %>
            <div class="success-message">
                <%= successMessage %>
            </div>
        <% } %>
        
        <% if (errorMessage != null) { %>
            <div class="error-message">
                <%= errorMessage %>
            </div>
        <% } %>
        
        <!-- メンバー追加フォーム -->
        <section class="dashboard-section">
            <h2>メンバー追加</h2>
            <form method="post" action="<%= request.getContextPath() %>/group/manage" class="add-member-form">
                <input type="hidden" name="action" value="addMember">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                
                <div class="form-group inline-form">
                    <label for="loginId">ユーザーのログインID:</label>
                    <input type="text" id="loginId" name="loginId" 
                           placeholder="例: user123" required maxlength="50">
                    <button type="submit" class="btn btn-primary">追加</button>
                </div>
            </form>
            
            <div class="help-text">
                <p>※ 追加したいユーザーのログインIDを入力してください</p>
            </div>
        </section>
        
        <!-- メンバー一覧 -->
        <section class="dashboard-section">
            <h2>メンバー一覧（<%= members != null ? members.size() : 0 %>人）</h2>
            
            <div class="action-buttons">
                <a href="<%= request.getContextPath() %>/group/attendance?id=<%= group.getId() %>" 
                   class="btn btn-success">グループ勤怠確認</a>
            </div>
            
            <% if (members != null && !members.isEmpty()) { %>
                <div class="members-table">
                    <table class="attendance-table">
                        <thead>
                            <tr>
                                <th>名前</th>
                                <th>ログインID</th>
                                <th>参加日時</th>
                                <th>役割</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (GroupMember member : members) { %>
                                <tr>
                                    <td><%= member.getUserName() %></td>
                                    <td><%= member.getUserLoginId() %></td>
                                    <td><%= member.getJoinedAt().toString().substring(0, 16).replace("T", " ") %></td>
                                    <td>
                                        <% if (member.getUserId() == group.getAdminUserId()) { %>
                                            <span class="role-admin">管理者</span>
                                        <% } else { %>
                                            <span class="role-member">メンバー</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <% if (member.getUserId() != group.getAdminUserId()) { %>
                                            <form method="post" action="<%= request.getContextPath() %>/group/manage" 
                                                  style="display: inline;" 
                                                  onsubmit="return confirm('このメンバーをグループから削除しますか？');">
                                                <input type="hidden" name="action" value="removeMember">
                                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                                <input type="hidden" name="userId" value="<%= member.getUserId() %>">
                                                <button type="submit" class="btn btn-sm btn-danger">削除</button>
                                            </form>
                                        <% } else { %>
                                            <span class="text-muted">-</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="no-records">
                    <p>メンバーがいません</p>
                </div>
            <% } %>
        </section>
        
        <!-- 戻るリンク -->
        <div class="back-link">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">ダッシュボードに戻る</a>
        </div>
    </div>
</body>
</html>
