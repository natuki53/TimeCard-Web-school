<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.Group, model.DmThread, java.util.List" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<DmThread> threads = (List<DmThread>) request.getAttribute("threads");
    @SuppressWarnings("unchecked")
    List<Group> groups = (List<Group>) request.getAttribute("groups");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DM - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
    <script defer src="<%= request.getContextPath() %>/js/notifications.js"></script>
</head>
<body class="with-header">
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
    <div class="hamburger" onclick="toggleMobileMenu()"><span></span><span></span><span></span></div>
</header>
<div class="mobile-menu" id="mobileMenu">
    <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
    <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
    <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
    <a href="<%= request.getContextPath() %>/groups">グループ</a>
    <a href="<%= request.getContextPath() %>/dm">DM</a>
    <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser.getName() %>さん</a>
    <a href="<%= request.getContextPath() %>/logout">ログアウト</a>
</div>

<div class="container">
    <h1>DM</h1>

    <section class="dashboard-section">
        <h2>新規DM</h2>

        <div class="form-group">
            <label for="dmGroupSelect">グループからメンバーを選ぶ</label>
            <select id="dmGroupSelect">
                <option value="" selected>選択してください</option>
                <% if (groups != null) { %>
                    <% for (Group g : groups) { %>
                        <option value="<%= g.getId() %>"><%= g.getName() %></option>
                    <% } %>
                <% } %>
            </select>
        </div>
        <div class="form-group">
            <label for="dmMemberSelect">メンバー</label>
            <select id="dmMemberSelect" disabled>
                <option value="" selected>まずグループを選択してください</option>
            </select>
            <p class="help-text" style="margin-top:8px;">※ グループ内DMは相手のDM許可に関係なく開始できます</p>
        </div>

        <div class="form-group">
            <label for="dmSearchInput">ID検索で送る（相手がDM許可ONの人だけ）</label>
            <div class="autocomplete">
                <input type="text" id="dmSearchInput" placeholder="例: user123" autocomplete="off">
                <div id="dmSearchSuggestions" class="autocomplete-list" style="display:none;"></div>
            </div>
            <p class="help-text" style="margin-top:8px;">※ 相手がプロフィールで「DMを許可する」をONにしている必要があります</p>
        </div>

        <form method="post" action="<%= request.getContextPath() %>/dm" id="dmStartForm">
            <input type="hidden" name="targetUserId" id="dmTargetUserId" value="">
            <div class="button-group">
                <button type="submit" class="btn btn-primary" id="dmStartBtn" disabled>DMを開始</button>
            </div>
        </form>
    </section>

    <section class="dashboard-section">
        <h2>DM一覧</h2>

        <% if (threads != null && !threads.isEmpty()) { %>
            <div class="members-table">
                <table class="attendance-table">
                    <thead>
                    <tr>
                        <th>名前</th>
                        <th>ログインID</th>
                        <th>最新メッセージ</th>
                        <th>日時</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (DmThread t : threads) { %>
                        <tr>
                            <td>
                                <button type="button"
                                        class="member-profile-link"
                                        data-userid="<%= t.getOtherUserId() %>"
                                        data-loginid="<%= t.getOtherLoginId() %>">
                                    <%= t.getOtherName() %>
                                </button>
                            </td>
                            <td>@<%= t.getOtherLoginId() %></td>
                            <td><%= t.getLastContent() != null ? t.getLastContent() : "" %></td>
                            <td><%= t.getLastCreatedAt() != null ? t.getLastCreatedAt().toString().replace("T"," ").substring(0,16) : "" %></td>
                            <td>
                                <a class="btn btn-sm btn-primary" href="<%= request.getContextPath() %>/dm/chat?id=<%= t.getId() %>">開く</a>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <p class="no-records">DMはまだありません。</p>
        <% } %>
    </section>
</div>

<!-- メンバープロフィール モーダル（DM用） -->
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

    (function() {
        const groupSel = document.getElementById("dmGroupSelect");
        const memberSel = document.getElementById("dmMemberSelect");
        const targetHidden = document.getElementById("dmTargetUserId");
        const startBtn = document.getElementById("dmStartBtn");
        const searchInput = document.getElementById("dmSearchInput");
        const list = document.getElementById("dmSearchSuggestions");

        function setTarget(userId) {
            targetHidden.value = userId ? String(userId) : "";
            startBtn.disabled = !targetHidden.value;
        }

        function hideSuggest() {
            list.style.display = "none";
            list.innerHTML = "";
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

        async function loadMembers(groupId) {
            const url = "<%= request.getContextPath() %>/api/group/members?groupId=" + encodeURIComponent(groupId);
            const res = await fetch(url, { headers: { "Accept": "application/json" } });
            if (!res.ok) return [];
            return await res.json();
        }

        groupSel.addEventListener("change", async function() {
            const gid = groupSel.value;
            setTarget("");
            hideSuggest();
            searchInput.value = "";
            if (!gid) {
                memberSel.disabled = true;
                memberSel.innerHTML = '<option value="" selected>まずグループを選択してください</option>';
                return;
            }
            memberSel.disabled = true;
            memberSel.innerHTML = '<option value="" selected>読み込み中...</option>';
            try {
                const members = await loadMembers(gid);
                const opts = ['<option value="" selected>選択してください</option>']
                    .concat(members.map(m => '<option value="' + m.id + '">' + escapeHtml(m.name) + ' (@' + escapeHtml(m.loginId) + ')</option>'));
                memberSel.innerHTML = opts.join("");
                memberSel.disabled = false;
            } catch (e) {
                memberSel.innerHTML = '<option value="" selected>読み込みに失敗しました</option>';
                memberSel.disabled = true;
            }
        });

        memberSel.addEventListener("change", function() {
            const uid = memberSel.value;
            searchInput.value = "";
            hideSuggest();
            setTarget(uid);
        });

        // ID検索（dm_allowed=1のみ）
        let timer = null;
        let lastQuery = "";
        async function fetchSuggestions(q) {
            const url = "<%= request.getContextPath() %>/api/users/dm_suggest?q=" + encodeURIComponent(q);
            const res = await fetch(url, { headers: { "Accept": "application/json" } });
            if (!res.ok) return [];
            return await res.json();
        }

        function showSuggest(items) {
            if (!items || items.length === 0) {
                hideSuggest();
                return;
            }
            list.innerHTML = items.map(u => {
                const label = (u.loginId || "") + "（" + (u.name || "") + "）";
                return '<button type="button" class="autocomplete-item"'
                    + ' data-userid="' + u.id + '"'
                    + ' data-loginid="' + escapeHtml(u.loginId) + '"'
                    + ' data-name="' + escapeHtml(u.name) + '"'
                    + '>' + escapeHtml(label) + '</button>';
            }).join("");
            list.style.display = "block";
        }

        searchInput.addEventListener("input", function() {
            const q = (searchInput.value || "").trim();
            setTarget("");
            if (q.length === 0) {
                lastQuery = "";
                hideSuggest();
                return;
            }
            if (q === lastQuery) return;
            lastQuery = q;
            if (timer) clearTimeout(timer);
            timer = setTimeout(async () => {
                try {
                    const items = await fetchSuggestions(q);
                    if (((searchInput.value || "").trim()) !== q) return;
                    showSuggest(items);
                } catch (e) {
                    hideSuggest();
                }
            }, 200);
        });

        list.addEventListener("click", function(e) {
            const btn = e.target.closest(".autocomplete-item");
            if (!btn) return;
            const userId = btn.getAttribute("data-userid");
            if (!userId) return;
            const loginId = btn.getAttribute("data-loginid") || "";
            // 選択確定
            hideSuggest();
            // 入力欄も「選択済み」状態にする
            searchInput.value = loginId;
            memberSel.value = "";
            groupSel.value = "";
            memberSel.disabled = true;
            memberSel.innerHTML = '<option value="" selected>まずグループを選択してください</option>';
            setTarget(userId);
        });

        searchInput.addEventListener("blur", function() { setTimeout(hideSuggest, 150); });
    })();
</script>

<script>
    // DM一覧：相手をクリックするとプロフィールをモーダル表示
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
            if (!userId) return;
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


