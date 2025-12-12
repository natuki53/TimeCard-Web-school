<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User, model.Group, model.GroupMessage, model.GroupAttachment, java.util.List" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    Group group = (Group) request.getAttribute("group");
    @SuppressWarnings("unchecked")
    List<GroupMessage> messages = (List<GroupMessage>) request.getAttribute("messages");
    String postError = (String) request.getAttribute("postError");

    // 簡易HTMLエスケープ（添付ファイル名表示用）
    java.util.function.Function<String, String> h = (s) -> {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    };
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>グループチャット - <%= group != null ? group.getName() : "" %> - 勤怠管理サイト</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script defer src="<%= request.getContextPath() %>/js/cookie_banner.js"></script>
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
        </nav>

        <div class="header-user">
            <span class="header-user-name"><%= loginUser.getName() %>さん</span>
            <a href="<%= request.getContextPath() %>/logout" class="header-logout">ログアウト</a>
        </div>

        <div class="hamburger" onclick="toggleMobileMenu()">
            <span></span><span></span><span></span>
        </div>
    </header>

    <div class="mobile-menu" id="mobileMenu">
        <a href="<%= request.getContextPath() %>/dashboard">ダッシュボード</a>
        <a href="<%= request.getContextPath() %>/attendance">勤怠打刻</a>
        <a href="<%= request.getContextPath() %>/attendance/list">勤怠一覧</a>
        <a href="<%= request.getContextPath() %>/groups">グループ</a>
        <a href="#" style="border-bottom: none; color: #bdc3c7;"><%= loginUser.getName() %>さん</a>
        <a href="<%= request.getContextPath() %>/logout">ログアウト</a>
    </div>

    <div class="container chat-page">
        <div class="chat-header">
            <div>
                <h1>グループチャット</h1>
                <p class="chat-subtitle"><strong><%= group != null ? group.getName() : "" %></strong></p>
            </div>
            <div class="chat-header-actions">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/groups">グループ一覧へ</a>
            </div>
        </div>

        <% if (postError != null && !postError.trim().isEmpty()) { %>
            <div class="error-message"><%= h.apply(postError) %></div>
        <% } %>
        <div id="chatClientError" class="error-message" style="display:none;" aria-live="polite"></div>
        <div id="chatToast" class="chat-toast" role="status" aria-live="polite" aria-atomic="true" style="display:none;"></div>

        <div class="chat-messages" id="chatMessages">
            <% if (messages != null && !messages.isEmpty()) { %>
                <% for (GroupMessage m : messages) { %>
                    <div class="chat-message">
                        <div class="chat-meta">
                            <span class="chat-author"><%= m.getUserName() %></span>
                            <span class="chat-loginid">@<%= m.getUserLoginId() %></span>
                            <span class="chat-time"><%= m.getCreatedAt() != null ? m.getCreatedAt().toString().replace("T", " ").substring(0, 16) : "" %></span>
                        </div>
                        <% if (m.getContent() != null && !m.getContent().trim().isEmpty()) { %>
                            <div class="chat-content"><%= m.getContent().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>") %></div>
                        <% } %>
                        <% if (m.getAttachments() != null && !m.getAttachments().isEmpty()) { %>
                            <div class="chat-attachments">
                                <% for (GroupAttachment a : m.getAttachments()) { %>
                                    <%
                                        String mime = a.getMimeType() != null ? a.getMimeType() : "";
                                        String fileUrl = request.getContextPath() + "/group/chat/file?id=" + a.getId();
                                        String safeName = h.apply(a.getOriginalFileName());
                                        boolean isZip = "application/zip".equalsIgnoreCase(mime)
                                                     || "application/x-zip-compressed".equalsIgnoreCase(mime)
                                                     || safeName.toLowerCase().endsWith(".zip");
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
                                    <%-- 画像プレビュー --%>
                                    <% if (mime.startsWith("image/")) { %>
                                        <div class="chat-embed">
                                            <a href="<%= fileUrl %>" target="_blank" rel="noopener">
                                                <img class="chat-image" src="<%= fileUrl %>" alt="<%= safeName %>">
                                            </a>
                                        </div>
                                    <% } %>
                                    <%-- 動画再生 --%>
                                    <% if (mime.startsWith("video/")) { %>
                                        <div class="chat-embed">
                                            <video class="chat-video" controls preload="metadata" src="<%= fileUrl %>"></video>
                                        </div>
                                    <% } %>
                                    <%-- 音楽再生 --%>
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

        <form class="chat-form" method="post" action="<%= request.getContextPath() %>/group/chat" enctype="multipart/form-data">
            <input type="hidden" name="groupId" value="<%= group != null ? group.getId() : 0 %>">
            <input id="chatFiles" class="chat-file-input" type="file" name="files" multiple
                   accept="image/*,application/pdf,video/*,audio/*,.zip,application/zip,application/x-zip-compressed">

            <div class="chat-compose" id="chatCompose">
                <label for="chatFiles" class="chat-plus" title="ファイルを添付">＋</label>
                <textarea id="chatContent" class="chat-compose-input" name="content" rows="2"
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
        // 初回表示は末尾へ
        (function() {
            var el = document.getElementById('chatMessages');
            if (el) el.scrollTop = el.scrollHeight;
        })();

        // 添付プレビュー（取り消し可能） + Ctrl+Vで貼り付け添付
        (function() {
            var fileInput = document.getElementById('chatFiles');
            var textarea = document.getElementById('chatContent');
            var preview = document.getElementById('chatAttachPreview');
            var compose = document.getElementById('chatCompose');
            var clientError = document.getElementById('chatClientError');
            var toast = document.getElementById('chatToast');
            var objectUrls = [];
            var MAX_FILE_BYTES = 50 * 1024 * 1024; // 50MB
            var MAX_TOTAL_BYTES = MAX_FILE_BYTES * 6; // サーバ側 maxRequestSize と合わせる
            var toastTimer = null;

            function clearObjectUrls() {
                if (!objectUrls || objectUrls.length === 0) return;
                for (var i = 0; i < objectUrls.length; i++) {
                    try { URL.revokeObjectURL(objectUrls[i]); } catch (e) {}
                }
                objectUrls = [];
            }

            function showError(msg) {
                if (!clientError) return;
                if (msg && String(msg).trim().length > 0) {
                    clientError.textContent = String(msg);
                    clientError.style.display = 'block';
                } else {
                    clientError.textContent = '';
                    clientError.style.display = 'none';
                }
            }

            function showToast(msg) {
                if (!toast) return;
                if (toastTimer) {
                    try { clearTimeout(toastTimer); } catch (e) {}
                    toastTimer = null;
                }
                if (!msg || String(msg).trim().length === 0) {
                    toast.classList.remove('is-visible');
                    toast.style.display = 'none';
                    toast.textContent = '';
                    return;
                }
                toast.textContent = String(msg);
                toast.style.display = 'block';
                // トランジションを確実に走らせる
                requestAnimationFrame(function() {
                    toast.classList.add('is-visible');
                });
                toastTimer = setTimeout(function() {
                    toast.classList.remove('is-visible');
                    // アニメ終了後に非表示
                    setTimeout(function() {
                        if (!toast.classList.contains('is-visible')) {
                            toast.style.display = 'none';
                            toast.textContent = '';
                        }
                    }, 250);
                }, 4200);
            }

            function formatBytes(bytes) {
                if (bytes == null) return '';
                if (bytes < 1024) return bytes + 'B';
                var kb = bytes / 1024.0;
                if (kb < 1024) return kb.toFixed(1) + 'KB';
                var mb = kb / 1024.0;
                if (mb < 1024) return mb.toFixed(1) + 'MB';
                var gb = mb / 1024.0;
                return gb.toFixed(1) + 'GB';
            }

            function extOf(name) {
                if (!name) return '';
                var idx = name.lastIndexOf('.');
                if (idx < 0) return '';
                return name.substring(idx).toLowerCase();
            }

            function kindOf(file) {
                // まずmime優先、無ければ拡張子で推測
                var t = (file && file.type) ? String(file.type).toLowerCase() : '';
                var e = extOf(file && file.name ? file.name : '');
                if (t.indexOf('image/') === 0) return 'image';
                if (t.indexOf('video/') === 0) return 'video';
                if (t.indexOf('audio/') === 0) return 'audio';
                if (t === 'application/pdf') return 'pdf';
                if (t === 'application/zip' || t === 'application/x-zip-compressed') return 'zip';

                if (e === '.png' || e === '.jpg' || e === '.jpeg' || e === '.gif' || e === '.webp' || e === '.bmp') return 'image';
                if (e === '.mp4' || e === '.webm' || e === '.ogg' || e === '.mov') return 'video';
                if (e === '.mp3' || e === '.wav' || e === '.m4a' || e === '.aac' || e === '.ogg') return 'audio';
                if (e === '.pdf') return 'pdf';
                if (e === '.zip') return 'zip';
                return 'file';
            }

            function isAllowedKind(k) {
                return k === 'image' || k === 'video' || k === 'audio' || k === 'pdf' || k === 'zip';
            }

            function tryAddFiles(existingFiles, newFiles) {
                var dt = new DataTransfer();
                var total = 0;
                var i;
                if (existingFiles) {
                    for (i = 0; i < existingFiles.length; i++) {
                        dt.items.add(existingFiles[i]);
                        total += (existingFiles[i].size || 0);
                    }
                }

                var errorMsg = '';
                for (var j = 0; j < newFiles.length; j++) {
                    var f = newFiles[j];
                    if (!f) continue;
                    var name = f.name || '';
                    var k = kindOf(f);
                    if (!isAllowedKind(k)) {
                        errorMsg = '形式未対応: 対応形式は画像/動画/音楽/PDF/zipです。' + '(' + name + ')';
                        continue;
                    }
                    if ((f.size || 0) > MAX_FILE_BYTES) {
                        errorMsg = '容量オーバー: 1ファイルあたり50MBまでです。' + name + '（' + formatBytes(f.size || 0) + '）。';
                        continue;
                    }
                    if (total + (f.size || 0) > MAX_TOTAL_BYTES) {
                        errorMsg = '容量オーバー: 添付の合計が上限を超えます（上限 ' + formatBytes(MAX_TOTAL_BYTES) + '）。';
                        continue;
                    }
                    dt.items.add(f);
                    total += (f.size || 0);
                }

                return { files: dt.files, error: errorMsg };
            }

            function rebuildFiles(excludeIndex) {
                var dt = new DataTransfer();
                var files = fileInput.files;
                for (var i = 0; i < files.length; i++) {
                    if (i === excludeIndex) continue;
                    dt.items.add(files[i]);
                }
                fileInput.files = dt.files;
            }

            function renderPreview() {
                if (!fileInput || !preview) return;
                var files = fileInput.files;
                if (!files || files.length === 0) {
                    preview.innerHTML = '';
                    clearObjectUrls();
                    return;
                }

                clearObjectUrls();
                var html = '<div class="chat-attach-grid">';
                for (var i = 0; i < files.length; i++) {
                    var f = files[i];
                    var k = kindOf(f);
                    var size = formatBytes(f.size);
                    var safeName = (f && f.name) ? String(f.name) : '';
                    var label = '';
                    if (k === 'pdf') label = 'PDF';
                    else if (k === 'zip') label = 'ZIP';
                    else if (k === 'audio') label = 'AUDIO';
                    else label = 'FILE';

                    html += '<div class="chat-attach-card" data-idx="' + i + '">';
                    html += '  <button type="button" class="chat-attach-remove" data-idx="' + i + '" aria-label="添付を取り消す">×</button>';
                    html += '  <div class="chat-attach-body">';

                    if (k === 'image') {
                        var urlImg = URL.createObjectURL(f);
                        objectUrls.push(urlImg);
                        html += '    <div class="chat-attach-media"><img class="chat-attach-img" src="' + urlImg + '" alt=""></div>';
                    } else if (k === 'video') {
                        var urlVid = URL.createObjectURL(f);
                        objectUrls.push(urlVid);
                        html += '    <div class="chat-attach-media"><video class="chat-attach-video" controls preload="metadata" src="' + urlVid + '"></video></div>';
                    } else {
                        html += '    <div class="chat-attach-media"><div class="chat-attach-icon" data-kind="' + k + '">' + label + '</div></div>';
                    }

                    html += '    <div class="chat-attach-meta">';
                    html += '      <div class="chat-attach-name" title="' + safeName.replace(/"/g, '&quot;') + '">' + safeName.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') + '</div>';
                    html += '      <div class="chat-attach-size">' + size + '</div>';
                    html += '    </div>';
                    html += '  </div>';
                    html += '</div>';
                }
                html += '</div>';
                preview.innerHTML = html;
            }

            function addFiles(newFiles) {
                if (!newFiles || newFiles.length === 0) return;
                var r = tryAddFiles(fileInput.files, newFiles);
                fileInput.files = r.files;
                showError(r.error); // DOM上のエラー（デバッグ/アクセシビリティ用）
                showToast(r.error); // ポップアップ表示
                renderPreview();
            }

            if (fileInput) {
                fileInput.addEventListener('change', function() {
                    // ファイル選択ダイアログから入った分も同じルールで整形
                    var r = tryAddFiles([], fileInput.files);
                    fileInput.files = r.files;
                    showError(r.error);
                    showToast(r.error);
                    renderPreview();
                });
            }

            // 取り消し（カードの×）
            if (preview) {
                preview.addEventListener('click', function(e) {
                    var btn = e.target && e.target.closest ? e.target.closest('.chat-attach-remove') : null;
                    if (!btn) return;
                    e.preventDefault();
                    var idxStr = btn.getAttribute('data-idx');
                    var idx = idxStr != null ? parseInt(idxStr, 10) : NaN;
                    if (isNaN(idx)) return;
                    rebuildFiles(idx);
                    renderPreview();
                });
            }

            if (textarea) {
                textarea.addEventListener('paste', function(e) {
                    var cd = e.clipboardData;
                    if (!cd || !cd.items) return;

                    var files = [];
                    for (var i = 0; i < cd.items.length; i++) {
                        var it = cd.items[i];
                        if (it.kind === 'file') {
                            var f = it.getAsFile();
                            if (f) files.push(f);
                        }
                    }
                    if (files.length > 0) {
                        // ファイル貼り付けのときは、本文への不要な貼り付けを抑制
                        e.preventDefault();
                        addFiles(files);
                    }
                });
            }

            // ドラッグ&ドロップで添付
            function isFileDrag(e) {
                var dt = e.dataTransfer;
                if (!dt) return false;
                if (dt.types) {
                    for (var i = 0; i < dt.types.length; i++) {
                        if (dt.types[i] === 'Files') return true;
                    }
                }
                return !!dt.files;
            }

            function setDragActive(active) {
                if (!compose) return;
                if (active) compose.classList.add('drag-active');
                else compose.classList.remove('drag-active');
            }

            if (compose) {
                compose.addEventListener('dragenter', function(e) {
                    if (!isFileDrag(e)) return;
                    e.preventDefault();
                    setDragActive(true);
                });
                compose.addEventListener('dragover', function(e) {
                    if (!isFileDrag(e)) return;
                    e.preventDefault();
                    setDragActive(true);
                });
                compose.addEventListener('dragleave', function(e) {
                    e.preventDefault();
                    setDragActive(false);
                });
                compose.addEventListener('drop', function(e) {
                    if (!isFileDrag(e)) return;
                    e.preventDefault();
                    setDragActive(false);
                    var fs = e.dataTransfer && e.dataTransfer.files ? e.dataTransfer.files : null;
                    if (fs && fs.length > 0) addFiles(fs);
                });
            }

            // 初期描画
            renderPreview();

            // 送信前チェック（容量超過で接続が切れるのを防ぐ）
            var form = document.querySelector('form.chat-form');
            if (form) {
                form.addEventListener('submit', function(e) {
                    var fs = fileInput && fileInput.files ? fileInput.files : null;
                    if (!fs || fs.length === 0) {
                        showError('');
                        return;
                    }
                    // 今のfilesが規定内か再チェック（削除されず残っている場合に備える）
                    var r = tryAddFiles([], fs);
                    if (r.error) {
                        e.preventDefault();
                        showError(r.error);
                        showToast(r.error);
                        // files は r.files に差し替えても良いが、ここではユーザーが×で調整できるよう維持
                        return;
                    }
                    showError('');
                    showToast('');
                });
            }
        })();

        // zipダウンロードは警告してから開始
        (function() {
            document.addEventListener('click', function(e) {
                var a = e.target.closest('.chat-attachment-download');
                if (!a) return;
                var mime = (a.getAttribute('data-mime') || '').toLowerCase();
                var filename = (a.getAttribute('data-filename') || '').toLowerCase();
                var isZip = (mime === 'application/zip' || mime === 'application/x-zip-compressed' || filename.endsWith('.zip'));
                if (isZip) {
                    var ok = window.confirm('zipファイルをダウンロードします。\n信頼できるファイルか確認してください。OKならダウンロードを開始します。');
                    if (!ok) {
                        e.preventDefault();
                        return;
                    }
                }
                // 通常はそのまま遷移（ダウンロード）
            }, { capture: true });
        })();
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>


