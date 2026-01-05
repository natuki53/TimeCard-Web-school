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
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
    <script defer src="<%= request.getContextPath() %>/js/notifications.js"></script>
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
            <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
            <a href="<%= request.getContextPath() %>/groups">グループ</a>
            <a href="<%= request.getContextPath() %>/dm">DM</a>
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
        <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
        <a href="<%= request.getContextPath() %>/groups">グループ</a>
        <a href="<%= request.getContextPath() %>/dm">DM</a>
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser.getName() %>さん</a>
        <a href="<%= request.getContextPath() %>/logout">ログアウト</a>
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
                    <div class="autocomplete">
                        <input type="text" id="loginId" name="loginId"
                               placeholder="例: user123" required maxlength="50" autocomplete="off">
                        <div id="loginIdSuggestions" class="autocomplete-list" style="display:none;"></div>
                    </div>
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
                                    <td>
                                        <button type="button"
                                                class="member-profile-link"
                                                data-userid="<%= member.getUserId() %>"
                                                data-loginid="<%= member.getUserLoginId() %>">
                                            <%= member.getUserName() %>
                                        </button>
                                    </td>
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
        <div class="back-link back-actions">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">ダッシュボードに戻る</a>
            <form method="post" action="<%= request.getContextPath() %>/group/delete" style="display:inline;"
                  onsubmit="return confirm('このグループを削除（非表示）しますか？\n※勤怠・チャット履歴はDBに残り、画面から見えなくなります。');">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                <button type="submit" class="btn btn-danger">グループ削除</button>
            </form>
        </div>
    </div>

    <!-- メンバープロフィール モーダル -->
    <div id="profileModal" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true" aria-labelledby="profileModalTitle">
        <div class="modal">
            <div class="modal-header">
                <h3 id="profileModalTitle">プロフィール</h3>
                <button type="button" class="modal-close" id="profileModalClose" aria-label="閉じる">×</button>
            </div>
            <div class="modal-body">
                <div class="profile-preview">
                    <img id="profileModalIcon" class="profile-icon" src="<%= request.getContextPath() %>/img/icon.png" alt="icon">
                    <div class="profile-text">
                        <div id="profileModalName" class="profile-name">-</div>
                        <div id="profileModalLoginId" class="profile-loginid">-</div>
                    </div>
                </div>
                <div id="profileModalBio" class="help-text" style="margin-top:12px; white-space:pre-wrap;"></div>
                <div id="profileModalError" class="error-message" style="display:none;"></div>
            </div>
        </div>
    </div>

    <script>
        function toggleMobileMenu() {
            var menu = document.getElementById('mobileMenu');
            menu.classList.toggle('show');
        }

        // ログインID オートコンプリート（候補表示→クリックで確定）
        (function() {
            const input = document.getElementById('loginId');
            const list = document.getElementById('loginIdSuggestions');
            const groupId = "<%= group.getId() %>";
            const baseUrl = "<%= request.getContextPath() %>/api/users/suggest";
            let timer = null;
            let lastQuery = "";

            function hide() {
                list.style.display = "none";
                list.innerHTML = "";
            }

            function show(items) {
                if (!items || items.length === 0) {
                    hide();
                    return;
                }
                list.innerHTML = items.map(u => {
                    const label = (u.loginId || "") + "（" + (u.name || "") + "）";
                    return '<button type="button" class="autocomplete-item" data-loginid="' + escapeHtml(u.loginId) + '">' + escapeHtml(label) + '</button>';
                }).join("");
                list.style.display = "block";
            }

            function escapeHtml(s) {
                if (s == null) return "";
                return String(s)
                    .replace(/&/g, "&amp;")
                    .replace(/</g, "&lt;")
                    .replace(/>/g, "&gt;")
                    .replace(/"/g, "&quot;")
                    .replace(/'/g, "&#039;");
            }

            async function fetchSuggestions(q) {
                const url = baseUrl + "?groupId=" + encodeURIComponent(groupId) + "&q=" + encodeURIComponent(q);
                const res = await fetch(url, { headers: { "Accept": "application/json" } });
                if (!res.ok) return [];
                return await res.json();
            }

            input.addEventListener("input", function() {
                const q = (input.value || "").trim();
                if (q.length === 0) {
                    lastQuery = "";
                    hide();
                    return;
                }
                if (q === lastQuery) return;
                lastQuery = q;

                if (timer) clearTimeout(timer);
                timer = setTimeout(async () => {
                    try {
                        const items = await fetchSuggestions(q);
                        // 入力が変わっていたら捨てる
                        if (((input.value || "").trim()) !== q) return;
                        show(items);
                    } catch (e) {
                        hide();
                    }
                }, 200);
            });

            list.addEventListener("click", function(e) {
                const btn = e.target.closest(".autocomplete-item");
                if (!btn) return;
                const loginId = btn.getAttribute("data-loginid") || "";
                input.value = loginId;
                hide();
                input.focus();
            });

            // フォーカス外れたら少し遅延して閉じる（クリックを拾うため）
            input.addEventListener("blur", function() {
                setTimeout(hide, 150);
            });

            // ESC で閉じる
            input.addEventListener("keydown", function(e) {
                if (e.key === "Escape") {
                    hide();
                    input.blur();
                }
            });
        })();
    </script>

    <script>
        (function() {
            const modal = document.getElementById("profileModal");
            const closeBtn = document.getElementById("profileModalClose");
            const iconEl = document.getElementById("profileModalIcon");
            const nameEl = document.getElementById("profileModalName");
            const loginIdEl = document.getElementById("profileModalLoginId");
            const bioEl = document.getElementById("profileModalBio");
            const errEl = document.getElementById("profileModalError");
            const apiBase = "<%= request.getContextPath() %>/api/user/profile";

            function openModal() {
                modal.style.display = "flex";
                document.body.style.overflow = "hidden";
            }

            function closeModal() {
                modal.style.display = "none";
                document.body.style.overflow = "";
            }

            async function loadProfile(userId) {
                errEl.style.display = "none";
                errEl.textContent = "";
                nameEl.textContent = "読み込み中...";
                loginIdEl.textContent = "";
                bioEl.textContent = "";
                iconEl.src = "<%= request.getContextPath() %>/img/icon.png";

                const url = apiBase + "?id=" + encodeURIComponent(userId);
                const res = await fetch(url, { headers: { "Accept": "application/json" } });
                if (!res.ok) {
                    throw new Error("プロフィールの取得に失敗しました。");
                }
                const data = await res.json();
                nameEl.textContent = data.name || "-";
                loginIdEl.textContent = data.loginId ? ("@" + data.loginId) : "-";
                bioEl.textContent = data.bio || "";
                if (data.iconUrl) {
                    iconEl.src = data.iconUrl;
                }
            }

            document.addEventListener("click", async function(e) {
                const btn = e.target.closest(".member-profile-link");
                if (!btn) return;
                const userId = btn.getAttribute("data-userid");
                openModal();
                try {
                    await loadProfile(userId);
                } catch (err) {
                    nameEl.textContent = "-";
                    loginIdEl.textContent = "-";
                    bioEl.textContent = "";
                    errEl.style.display = "block";
                    errEl.textContent = err && err.message ? err.message : "プロフィールの取得に失敗しました。";
                }
            });

            closeBtn.addEventListener("click", closeModal);
            modal.addEventListener("click", function(e) {
                if (e.target === modal) closeModal();
            });
            document.addEventListener("keydown", function(e) {
                if (e.key === "Escape" && modal.style.display !== "none") closeModal();
            });
        })();
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>
