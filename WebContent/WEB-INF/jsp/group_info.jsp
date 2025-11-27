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
    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    Boolean isMember = (Boolean) request.getAttribute("isMember");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>グループ詳細 - <%= group.getName() %> - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="with-header">
    <!-- ヘッダー -->
    <header class="header">
        <a href="<%= request.getContextPath() %>/dashboard">
            <img src="<%= request.getContextPath() %>/img/index.png" alt="CLOCK" class="header-logo">
        </a>
        
        <nav class="header-nav">
            <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
            <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
            <a href="<%= request.getContextPath() %>/attendance-list">勤怠一覧</a>
        </nav>
        
        <div class="header-user">
            <span class="header-user-name"><%= loginUser.getName() %>さん</span>
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
        <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
        <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
        <a href="<%= request.getContextPath() %>/attendance-list">勤怠一覧</a>
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser.getName() %>さん</a>
        <a href="<%= request.getContextPath() %>/logout">ログアウト</a>
    </div>

    <div class="container">
        <h1>グループ詳細</h1>
        
        <!-- グループ情報 -->
        <section class="dashboard-section">
            <div class="group-header">
                <div class="group-title">
                    <h2><%= group.getName() %></h2>
                    <% if (isAdmin != null && isAdmin) { %>
                        <span class="user-role admin-badge">管理者</span>
                    <% } else { %>
                        <span class="user-role member-badge">メンバー</span>
                    <% } %>
                </div>
                
                <% if (group.getDescription() != null && !group.getDescription().isEmpty()) { %>
                    <div class="group-description-section">
                        <h3>説明</h3>
                        <p class="group-description"><%= group.getDescription() %></p>
                    </div>
                <% } %>
                
                <div class="group-meta">
                    <div class="meta-item">
                        <span class="meta-label">作成日時:</span>
                        <span class="meta-value"><%= group.getCreatedAt().toString().substring(0, 16).replace("T", " ") %></span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">メンバー数:</span>
                        <span class="meta-value"><%= members != null ? members.size() : 0 %>人</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- アクションボタン -->
        <section class="dashboard-section">
            <h2>アクション</h2>
            <div class="action-buttons-grid">
                <% if (isAdmin != null && isAdmin) { %>
                    <a href="<%= request.getContextPath() %>/group/manage?id=<%= group.getId() %>" 
                       class="btn btn-primary">グループ管理</a>
                <% } %>
                
                <% if ((isAdmin != null && isAdmin) || (isMember != null && isMember)) { %>
                    <a href="<%= request.getContextPath() %>/group/attendance?id=<%= group.getId() %>" 
                       class="btn btn-success">勤怠確認</a>
                <% } %>
                
                <a href="<%= request.getContextPath() %>/dashboard" 
                   class="btn btn-secondary">ダッシュボードに戻る</a>
            </div>
        </section>

        <!-- メンバー一覧 -->
        <section class="dashboard-section">
            <h2>メンバー一覧（<%= members != null ? members.size() : 0 %>人）</h2>
            
            <% if (members != null && !members.isEmpty()) { %>
                <div class="members-grid">
                    <% for (GroupMember member : members) { %>
                        <div class="member-card">
                            <div class="member-info">
                                <div class="member-name">
                                    <%= member.getUserName() %>
                                    <% if (member.getUserId() == group.getAdminUserId()) { %>
                                        <span class="role-admin">管理者</span>
                                    <% } else { %>
                                        <span class="role-member">メンバー</span>
                                    <% } %>
                                </div>
                                <div class="member-details">
                                    <div class="detail-item">
                                        <span class="detail-label">ログインID:</span>
                                        <span class="detail-value"><%= member.getUserLoginId() %></span>
                                    </div>
                                    <div class="detail-item">
                                        <span class="detail-label">参加日:</span>
                                        <span class="detail-value"><%= member.getJoinedAt().toString().substring(0, 10) %></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="no-records">
                    <p>メンバーがいません</p>
                </div>
            <% } %>
        </section>
        
        <!-- 管理者向け情報 -->
        <% if (isAdmin != null && isAdmin) { %>
            <section class="dashboard-section admin-info">
                <h2>管理者向け情報</h2>
                <div class="info-grid">
                    <div class="info-card">
                        <h3>グループ管理</h3>
                        <p>メンバーの追加・削除、グループ設定の変更ができます。</p>
                        <a href="<%= request.getContextPath() %>/group/manage?id=<%= group.getId() %>" 
                           class="btn btn-sm btn-primary">管理画面へ</a>
                    </div>
                    <div class="info-card">
                        <h3>勤怠管理</h3>
                        <p>全メンバーの勤怠状況を確認・管理できます。</p>
                        <a href="<%= request.getContextPath() %>/group/attendance?id=<%= group.getId() %>" 
                           class="btn btn-sm btn-success">勤怠確認へ</a>
                    </div>
                </div>
            </section>
        <% } %>
    </div>

    <script>
        function toggleMobileMenu() {
            var menu = document.getElementById('mobileMenu');
            menu.classList.toggle('show');
        }
    </script>
</body>
</html>
