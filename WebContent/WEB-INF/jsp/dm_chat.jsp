<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.DmMessage, model.DmAttachment, java.util.List" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Integer threadId = (Integer) request.getAttribute("threadId");
    @SuppressWarnings("unchecked")
    List<DmMessage> messages = (List<DmMessage>) request.getAttribute("messages");
    String otherName = (String) request.getAttribute("otherName");
    String postError = (String) request.getAttribute("postError");
    java.util.function.Function<String,String> h = s -> {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#039;");
    };
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DM - <%= otherName != null ? otherName : "" %> - 勤怠管理サイト</title>
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

<div class="container chat-page">
    <div class="chat-header">
        <div>
            <h1>DM</h1>
            <p class="chat-subtitle"><strong><%= otherName != null ? otherName : "" %></strong></p>
        </div>
        <div class="chat-header-actions">
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/dm">DM一覧へ</a>
        </div>
    </div>

    <% if (postError != null && !postError.trim().isEmpty()) { %>
        <div class="error-message"><%= h.apply(postError) %></div>
    <% } %>

    <div class="chat-messages" id="chatMessages">
        <% if (messages != null && !messages.isEmpty()) { %>
            <% for (DmMessage m : messages) { %>
                <div class="chat-message" data-msgid="<%= m.getId() %>">
                    <div class="chat-meta">
                        <span class="chat-author"><%= m.getSenderName() %></span>
                        <span class="chat-loginid">@<%= m.getSenderLoginId() %></span>
                        <span class="chat-time"><%= m.getCreatedAt() != null ? m.getCreatedAt().toString().replace("T", " ").substring(0, 16) : "" %></span>
                    </div>
                    <% if (m.getContent() != null && !m.getContent().trim().isEmpty()) { %>
                        <div class="chat-content"><%= m.getContent().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>") %></div>
                    <% } %>
                    <% if (m.getAttachments() != null && !m.getAttachments().isEmpty()) { %>
                        <div class="chat-attachments">
                            <% for (DmAttachment a : m.getAttachments()) { %>
                                <%
                                    String mime = a.getMimeType() != null ? a.getMimeType() : "";
                                    String fileUrl = request.getContextPath() + "/dm/chat/file?id=" + a.getId();
                                    String safeName = h.apply(a.getOriginalFileName());
                                %>
                                <div class="chat-attachment-row">
                                    <a class="chat-attachment" href="<%= fileUrl %>" target="_blank" rel="noopener">
                                        <span class="att-name"><%= safeName %></span>
                                        <span class="att-size">(<%= util.UploadUtil.formatBytes(a.getSizeBytes()) %>)</span>
                                    </a>
                                    <a class="chat-attachment-download" href="<%= fileUrl %>"
                                       data-mime="<%= h.apply(mime) %>" data-filename="<%= safeName %>"
                                       title="ダウンロード">↓</a>
                                </div>
                                <% if (mime.startsWith("image/")) { %>
                                    <div class="chat-embed">
                                        <a href="<%= fileUrl %>" target="_blank" rel="noopener">
                                            <img class="chat-image" src="<%= fileUrl %>" alt="<%= safeName %>">
                                        </a>
                                    </div>
                                <% } %>
                                <% if (mime.startsWith("video/")) { %>
                                    <div class="chat-embed">
                                        <video class="chat-video" controls preload="metadata" src="<%= fileUrl %>"></video>
                                    </div>
                                <% } %>
                                <% if (mime.startsWith("audio/")) { %>
                                    <div class="chat-embed">
                                        <audio class="chat-audio" controls preload="metadata" src="<%= fileUrl %>"></audio>
                                    </div>
                                <% } %>
                            <% } %>
                        </div>
                    <% } %>
                </div>
            <% } %>
        <% } else { %>
            <p class="no-records">まだ投稿がありません。</p>
        <% } %>
    </div>

    <form class="chat-form" method="post" action="<%= request.getContextPath() %>/dm/chat" enctype="multipart/form-data">
        <input type="hidden" name="threadId" value="<%= threadId != null ? threadId : 0 %>">
        <input id="chatFiles" class="chat-file-input" type="file" name="files" multiple
               accept="image/*,application/pdf,video/*,audio/*,.zip,application/zip,application/x-zip-compressed">
        <div class="chat-compose" id="chatCompose">
            <label for="chatFiles" class="chat-plus" title="ファイルを添付">＋</label>
            <textarea id="chatContent" class="chat-compose-input" name="content" rows="1"
                      placeholder="メッセージを送信" maxlength="5000"></textarea>
            <button type="submit" class="btn btn-primary chat-send">送信</button>
        </div>
        <div id="chatAttachPreview" class="chat-attach-preview" aria-live="polite"></div>
    </form>
</div>

<script>
    function toggleMobileMenu() {
        var menu = document.getElementById('mobileMenu');
        menu.classList.toggle('show');
    }
    (function() {
        var el = document.getElementById('chatMessages');
        if (el) el.scrollTop = el.scrollHeight;
    })();

    // 入力欄自動リサイズ（最大10行）
    (function() {
        var textarea = document.getElementById('chatContent');
        if (!textarea) return;

        function autoResize() {
            var cs = window.getComputedStyle(textarea);
            var lh = parseFloat(cs.lineHeight || "18") || 18;
            var pt = parseFloat(cs.paddingTop || "0") || 0;
            var pb = parseFloat(cs.paddingBottom || "0") || 0;
            var bt = parseFloat(cs.borderTopWidth || "0") || 0;
            var bb = parseFloat(cs.borderBottomWidth || "0") || 0;
            var minH = (lh * 1) + pt + pb + bt + bb;
            var maxH = (lh * 10) + pt + pb + bt + bb;

            textarea.style.height = "auto";
            var next = textarea.scrollHeight;
            next = Math.max(minH, next);
            if (next > maxH) {
                textarea.style.height = maxH + "px";
                textarea.style.overflowY = "auto";
            } else {
                textarea.style.height = next + "px";
                textarea.style.overflowY = "hidden";
            }
        }

        textarea.addEventListener('input', autoResize);
        setTimeout(autoResize, 0);
    })();
</script>

<script>
    // リアルタイム更新（ポーリング）
    (function() {
        var container = document.getElementById('chatMessages');
        if (!container) return;
        var threadId = parseInt("<%= threadId != null ? threadId : 0 %>", 10) || 0;
        if (!threadId) return;

        function getLastId() {
            var nodes = container.querySelectorAll('.chat-message[data-msgid]');
            if (!nodes || nodes.length === 0) return 0;
            var last = nodes[nodes.length - 1];
            var v = last.getAttribute('data-msgid');
            var n = parseInt(v, 10);
            return isFinite(n) ? n : 0;
        }

        function isNearBottom() {
            return (container.scrollHeight - container.scrollTop - container.clientHeight) < 60;
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

        function fmtTime(iso) {
            if (!iso) return "";
            var t = String(iso).replace("T", " ");
            return t.length >= 16 ? t.substring(0, 16) : t;
        }

        function formatBytes(bytes) {
            var b = Number(bytes || 0);
            if (b < 1024) return b + "B";
            var kb = b / 1024.0;
            if (kb < 1024) return kb.toFixed(1) + "KB";
            var mb = kb / 1024.0;
            if (mb < 1024) return mb.toFixed(1) + "MB";
            var gb = mb / 1024.0;
            return gb.toFixed(1) + "GB";
        }

        function buildAttachmentHtml(a) {
            var url = escapeHtml(a.url || "");
            var mime = escapeHtml(a.mimeType || "");
            var name = escapeHtml(a.originalFileName || "");
            var size = formatBytes(a.sizeBytes || 0);
            var html = '';
            html += '<div class="chat-attachment-row">';
            html += '  <a class="chat-attachment" href="' + url + '" target="_blank" rel="noopener">';
            html += '    <span class="att-name">' + name + '</span>';
            html += '    <span class="att-size">(' + size + ')</span>';
            html += '  </a>';
            html += '  <a class="chat-attachment-download" href="' + url + '" data-mime="' + mime + '" data-filename="' + name + '" title="ダウンロード">↓</a>';
            html += '</div>';

            if (mime.indexOf("image/") === 0) {
                html += '<div class="chat-embed"><a href="' + url + '" target="_blank" rel="noopener">';
                html += '<img class="chat-image" src="' + url + '" alt="' + name + '"></a></div>';
            } else if (mime.indexOf("video/") === 0) {
                html += '<div class="chat-embed"><video class="chat-video" controls preload="metadata" src="' + url + '"></video></div>';
            } else if (mime.indexOf("audio/") === 0) {
                html += '<div class="chat-embed"><audio class="chat-audio" controls preload="metadata" src="' + url + '"></audio></div>';
            }
            return html;
        }

        function appendMessage(m) {
            var html = '<div class="chat-message" data-msgid="' + m.id + '">';
            html += '  <div class="chat-meta">';
            html += '    <span class="chat-author">' + escapeHtml(m.senderName || '') + '</span>';
            html += '    <span class="chat-loginid">@' + escapeHtml(m.senderLoginId || '') + '</span>';
            html += '    <span class="chat-time">' + escapeHtml(fmtTime(m.createdAt || '')) + '</span>';
            html += '  </div>';
            var content = (m.content || "");
            if (String(content).trim().length > 0) {
                html += '  <div class="chat-content">' + escapeHtml(content).replace(/\n/g, "<br>") + '</div>';
            }
            if (m.attachments && m.attachments.length > 0) {
                html += '  <div class="chat-attachments">';
                for (var i = 0; i < m.attachments.length; i++) {
                    html += buildAttachmentHtml(m.attachments[i]);
                }
                html += '  </div>';
            }
            html += '</div>';
            container.insertAdjacentHTML('beforeend', html);
        }

        var inFlight = false;
        async function poll() {
            if (document.hidden) return;
            if (inFlight) return;
            inFlight = true;
            try {
                var lastId = getLastId();
                var url = "<%= request.getContextPath() %>/api/dm/chat/messages?threadId=" + encodeURIComponent(threadId) + "&afterId=" + encodeURIComponent(lastId);
                var res = await fetch(url, { headers: { "Accept": "application/json" } });
                if (!res.ok) return;
                var items = await res.json();
                if (!items || items.length === 0) return;
                var stick = isNearBottom();
                var empty = container.querySelector('.no-records');
                if (empty) try { empty.remove(); } catch (e) {}
                for (var i = 0; i < items.length; i++) appendMessage(items[i]);
                if (stick) container.scrollTop = container.scrollHeight;
            } catch (e) {
                // silent
            } finally {
                inFlight = false;
            }
        }

        poll();
        setInterval(poll, 2000);
    })();
</script>

<script>
    // グループチャットと同じ添付プレビュー（簡易版）
    (function() {
        var fileInput = document.getElementById('chatFiles');
        var preview = document.getElementById('chatAttachPreview');
        if (!fileInput || !preview) return;

        function formatBytes(bytes) {
            if (bytes < 1024) return bytes + 'B';
            var kb = bytes / 1024.0;
            if (kb < 1024) return kb.toFixed(1) + 'KB';
            var mb = kb / 1024.0;
            if (mb < 1024) return mb.toFixed(1) + 'MB';
            var gb = mb / 1024.0;
            return gb.toFixed(1) + 'GB';
        }

        function escapeHtml(s) {
            if (s == null) return '';
            return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#039;');
        }

        function render() {
            var fs = fileInput.files;
            if (!fs || fs.length === 0) {
                preview.innerHTML = '';
                return;
            }
            var html = '<div class="chat-attach-grid">';
            for (var i = 0; i < fs.length; i++) {
                var f = fs[i];
                html += '<div class="chat-attach-card">';
                html += '  <div class="chat-attach-body">';
                html += '    <div class="chat-attach-meta">';
                html += '      <div class="chat-attach-name">' + escapeHtml(f.name) + '</div>';
                html += '      <div class="chat-attach-size">' + formatBytes(f.size || 0) + '</div>';
                html += '    </div>';
                html += '  </div>';
                html += '</div>';
            }
            html += '</div>';
            preview.innerHTML = html;
        }

        fileInput.addEventListener('change', render);
        render();
    })();
</script>

<%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>


