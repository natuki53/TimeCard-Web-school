<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>プロフィール - 勤怠管理サイト</title>
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

        <div class="hamburger" onclick="toggleMobileMenu()">
            <span></span>
            <span></span>
            <span></span>
        </div>
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
        <h1>プロフィール</h1>

        <% if (successMessage != null) { %>
            <div class="success-message"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="error-message"><%= errorMessage %></div>
        <% } %>

        <section class="dashboard-section">
            <h2>基本情報</h2>

            <div class="profile-preview" style="margin-bottom: 16px;">
                <img class="profile-icon" src="<%= request.getContextPath() %>/user/avatar?id=<%= loginUser.getId() %>" alt="icon">
                <div class="profile-text">
                    <div class="profile-name"><%= loginUser.getName() %></div>
                    <div class="profile-loginid">@<%= loginUser.getLoginId() %></div>
                </div>
            </div>

            <form method="post" action="<%= request.getContextPath() %>/profile" enctype="multipart/form-data" id="profileForm">
                <div class="form-group">
                    <label for="name">表示名</label>
                    <input type="text" id="name" name="name" value="<%= loginUser.getName() %>" maxlength="100" required>
                </div>

                <div class="form-group">
                    <label for="bio">自己紹介（任意）</label>
                    <textarea id="bio" name="bio" maxlength="1000" rows="5"
                              placeholder="例：担当、好きなこと、ひとことなど"><%= loginUser.getBio() != null ? loginUser.getBio() : "" %></textarea>
                    <p class="help-text" style="margin-top:8px;">※ 1000文字まで</p>
                </div>

                <div class="form-group">
                    <label>
                        <input type="checkbox" name="dmAllowed" value="1" <%= loginUser.isDmAllowed() ? "checked" : "" %>>
                        DMを許可する（ID検索からのDMを受け取れるようになります）
                    </label>
                    <p class="help-text" style="margin-top:8px;">※ デフォルトはONです</p>
                </div>

                <div class="form-group">
                    <label for="icon">アイコン画像（任意）</label>
                    <input type="file" id="icon" name="icon" accept="image/*">
                    <p class="help-text" style="margin-top:8px;">※ PNG/JPG/GIF/WebP を推奨</p>
                </div>

                <!-- トリミング結果（PNG data URL） -->
                <input type="hidden" id="croppedIconData" name="croppedIconData" value="">

                <div class="button-group">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">戻る</a>
                </div>
            </form>
        </section>

        <section class="dashboard-section">
            <h2>アカウント</h2>
            <p class="help-text">退会すると、アカウントは無効化され、アプリ上で表示されなくなります（データはDBに残ります）。</p>
            <div class="button-group" style="margin-top: 12px;">
                <form method="post" action="<%= request.getContextPath() %>/user/delete"
                      onsubmit="return confirm('本当に退会しますか？\\n※この操作は取り消せません。');">
                    <button type="submit" class="btn btn-danger">ユーザー削除（退会）</button>
                </form>
            </div>
        </section>
    </div>

    <script>
        function toggleMobileMenu() {
            var menu = document.getElementById('mobileMenu');
            menu.classList.toggle('show');
        }
    </script>

    <script>
        // アイコン編集（Discord風モーダル：ドラッグ＋ズーム＋適用/キャンセル）
        (function() {
            const fileInput = document.getElementById("icon");
            const form = document.getElementById("profileForm");
            const hidden = document.getElementById("croppedIconData");
            const previewIcon = document.querySelector(".profile-preview .profile-icon");

            // モーダルDOM（動的に追加）
            const modal = document.createElement("div");
            modal.id = "avatarModal";
            modal.className = "modal-overlay avatar-modal-overlay";
            modal.style.display = "none";
            modal.setAttribute("role", "dialog");
            modal.setAttribute("aria-modal", "true");
            modal.innerHTML = `
                <div class="modal avatar-editor">
                    <div class="modal-header">
                        <h3 id="avatarModalTitle">画像を編集</h3>
                        <button type="button" class="modal-close" id="avatarCloseBtn" aria-label="閉じる">×</button>
                    </div>
                    <div class="modal-body avatar-editor__body">
                        <div class="avatar-crop-stage" id="avatarStage"
                             style="width:min(360px, 90vw, 55vh);height:min(360px, 90vw, 55vh);max-width:100%;margin:0 auto;position:relative;overflow:hidden;background:#e5e7eb;border-radius:12px;border:1px solid #d1d5db;touch-action:none;">
                            <img id="avatarCropImage" alt="crop"
                                 style="display:none;position:absolute;top:0;left:0;transform-origin:0 0;user-select:none;-webkit-user-drag:none;">
                            <div class="avatar-crop-circle" aria-hidden="true"
                                 style="position:absolute;top:50%;left:50%;width:100%;height:100%;transform:translate(-50%,-50%);border-radius:50%;box-shadow:0 0 0 9999px rgba(0,0,0,0.35);border:2px solid rgba(255,255,255,0.85);pointer-events:none;"></div>
                        </div>
                        <div class="avatar-controls">
                            <input type="range" id="avatarZoom" min="1" max="3" step="0.01" value="1" aria-label="拡大率">
                            <button type="button" class="avatar-pick-btn" id="avatarPickBtn" aria-label="別の画像を選択">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                    <path d="M21 19V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2Z" stroke="#2c3e50" stroke-width="1.6"/>
                                    <path d="m8 13 2.2 2.2L14.5 11 20 16.5" stroke="#2c3e50" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                                    <path d="M8.5 9.2a1.3 1.3 0 1 0 0-2.6 1.3 1.3 0 0 0 0 2.6Z" fill="#2c3e50"/>
                                </svg>
                            </button>
                        </div>
                        <p class="help-text" style="text-align:center;">※ 画像をドラッグして位置調整できます</p>
                    </div>
                    <div class="avatar-footer">
                        <button type="button" class="btn btn-secondary" id="avatarResetBtn">リセット</button>
                        <div class="avatar-footer__right">
                            <button type="button" class="btn btn-secondary" id="avatarCancelBtn">キャンセル</button>
                            <button type="button" class="btn btn-primary" id="avatarApplyBtn">適用</button>
                        </div>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);

            const stage = modal.querySelector("#avatarStage");
            const img = modal.querySelector("#avatarCropImage");
            const zoom = modal.querySelector("#avatarZoom");
            const pickBtn = modal.querySelector("#avatarPickBtn");
            const resetBtn = modal.querySelector("#avatarResetBtn");
            const cancelBtn = modal.querySelector("#avatarCancelBtn");
            const applyBtn = modal.querySelector("#avatarApplyBtn");
            const closeBtn = modal.querySelector("#avatarCloseBtn");

            const OUTPUT = 256; // 保存用PNGサイズ
            let fitScale = 1;
            let state = {
                loaded: false,
                naturalW: 0,
                naturalH: 0,
                scale: 1,
                x: 0,
                y: 0,
                dragging: false,
                startX: 0,
                startY: 0,
                baseX: 0,
                baseY: 0
            };

            function clamp(v, min, max) { return Math.max(min, Math.min(max, v)); }
            function stageSize() {
                const w = stage.clientWidth;
                const h = stage.clientHeight;
                if (w > 0 && h > 0) return Math.min(w, h);
                if (w > 0) return w;
                // display:none 等で 0 になる場合のフォールバック
                const cs = window.getComputedStyle(stage);
                const px = parseFloat(cs.width || "0");
                return (isFinite(px) && px > 0) ? px : 360;
            }

            function clampPosition() {
                const box = stageSize();
                const w = state.naturalW * state.scale;
                const h = state.naturalH * state.scale;
                // fitScale以上なら基本的に w>=box or h>=box を満たすが、保険で分岐
                const minX = w <= box ? (box - w) / 2 : (box - w);
                const maxX = w <= box ? (box - w) / 2 : 0;
                const minY = h <= box ? (box - h) / 2 : (box - h);
                const maxY = h <= box ? (box - h) / 2 : 0;
                state.x = clamp(state.x, minX, maxX);
                state.y = clamp(state.y, minY, maxY);
            }

            function updateTransform() {
                if (!state.loaded) return;
                clampPosition();
                img.style.transform = "translate(" + state.x + "px," + state.y + "px) scale(" + state.scale + ")";
            }

            function resetToCenter() {
                const box = stageSize();
                state.scale = fitScale;
                const w = state.naturalW * state.scale;
                const h = state.naturalH * state.scale;
                state.x = (box - w) / 2;
                state.y = (box - h) / 2;
                zoom.value = String(fitScale);
                updateTransform();
            }

            function openModal() {
                modal.style.display = "flex";
                document.body.style.overflow = "hidden";
            }

            function closeModal() {
                modal.style.display = "none";
                document.body.style.overflow = "";
            }

            function clearState() {
                state.loaded = false;
                state.naturalW = 0;
                state.naturalH = 0;
                state.scale = 1;
                state.x = 0;
                state.y = 0;
                state.dragging = false;
                img.src = "";
                img.style.display = "none";
                hidden.value = "";
            }

            function loadFile(file) {
                if (!file) return;
                if (!file.type || !file.type.startsWith("image/")) return;

                const reader = new FileReader();
                reader.onload = function() {
                    img.onload = function() {
                        state.loaded = true;
                        state.naturalW = img.naturalWidth;
                        state.naturalH = img.naturalHeight;
                        img.style.display = "block";
                        // stageのサイズ取得にはレイアウトが必要なので、先に表示して次フレームで計算する
                        openModal();
                        requestAnimationFrame(function() {
                            const box = stageSize();
                            // 枠を埋めるスケール（短辺をstageに合わせる）
                            fitScale = Math.max(box / state.naturalW, box / state.naturalH);

                            // スライダーの範囲を画像に合わせて調整
                            zoom.min = String(fitScale);
                            zoom.max = String(Math.max(fitScale * 3, fitScale + 0.01));
                            zoom.value = String(fitScale);

                            resetToCenter();
                        });
                    };
                    img.src = reader.result;
                };
                reader.readAsDataURL(file);
            }

            // ファイル選択でモーダルを開く（適用するまではhiddenを空にする）
            fileInput.addEventListener("change", function() {
                const f = fileInput.files && fileInput.files[0];
                if (!f) {
                    clearState();
                    return;
                }
                hidden.value = "";
                loadFile(f);
            });

            // 同じ画像を再選択しても change が発火するように、選択前に値をクリアする
            // （ブラウザ仕様：同一ファイルだと change が発火しないことがある）
            fileInput.addEventListener("click", function() {
                fileInput.value = "";
            });

            // ズーム（中心固定）
            zoom.addEventListener("input", function() {
                if (!state.loaded) return;
                const box = stageSize();
                const newScale = parseFloat(zoom.value);
                const oldScale = state.scale;
                const cx = box / 2;
                const cy = box / 2;
                // 中心にある画像点を維持
                state.x = cx - (cx - state.x) / oldScale * newScale;
                state.y = cy - (cy - state.y) / oldScale * newScale;
                state.scale = newScale;
                updateTransform();
            });

            // リセット
            resetBtn.addEventListener("click", function() {
                if (!state.loaded) return;
                hidden.value = "";
                resetToCenter();
            });

            // 別画像選択
            pickBtn.addEventListener("click", function() {
                fileInput.click();
            });

            // ドラッグ移動（マウス・タッチ）
            function onDown(clientX, clientY) {
                if (!state.loaded) return;
                state.dragging = true;
                state.startX = clientX;
                state.startY = clientY;
                state.baseX = state.x;
                state.baseY = state.y;
            }
            function onMove(clientX, clientY) {
                if (!state.dragging) return;
                const dx = clientX - state.startX;
                const dy = clientY - state.startY;
                state.x = state.baseX + dx;
                state.y = state.baseY + dy;
                updateTransform();
            }
            function onUp() { state.dragging = false; }

            stage.addEventListener("mousedown", function(e) {
                e.preventDefault();
                onDown(e.clientX, e.clientY);
            });
            document.addEventListener("mousemove", function(e) { onMove(e.clientX, e.clientY); });
            document.addEventListener("mouseup", onUp);

            stage.addEventListener("touchstart", function(e) {
                if (!e.touches || e.touches.length === 0) return;
                const t = e.touches[0];
                onDown(t.clientX, t.clientY);
            }, { passive: true });
            document.addEventListener("touchmove", function(e) {
                if (!e.touches || e.touches.length === 0) return;
                const t = e.touches[0];
                onMove(t.clientX, t.clientY);
            }, { passive: true });
            document.addEventListener("touchend", onUp, { passive: true });

            // キャンセル（選択自体を無かったことにする）
            function cancelEditing() {
                fileInput.value = "";
                clearState();
                closeModal();
            }

            cancelBtn.addEventListener("click", cancelEditing);
            closeBtn.addEventListener("click", cancelEditing);
            modal.addEventListener("click", function(e) {
                if (e.target === modal) cancelEditing(); // 外側クリック
            });
            document.addEventListener("keydown", function(e) {
                if (modal.style.display !== "flex") return;
                if (e.key === "Escape") cancelEditing();
            });

            // 適用：PNG生成→hiddenへ格納→画面プレビュー更新
            applyBtn.addEventListener("click", function() {
                if (!state.loaded) return;
                const box = stageSize();
                const out = document.createElement("canvas");
                out.width = OUTPUT;
                out.height = OUTPUT;
                const ctx = out.getContext("2d");
                if (!ctx) return;

                const scaleToOut = OUTPUT / box;
                const w = state.naturalW * state.scale * scaleToOut;
                const h = state.naturalH * state.scale * scaleToOut;
                const x = state.x * scaleToOut;
                const y = state.y * scaleToOut;

                ctx.clearRect(0, 0, OUTPUT, OUTPUT);
                ctx.drawImage(img, x, y, w, h);

                const dataUrl = out.toDataURL("image/png");
                hidden.value = dataUrl;
                if (previewIcon) previewIcon.src = dataUrl;
                // dataURLを使うので、ファイル自体はクリアしてOK（同一ファイル再選択も可能にする）
                fileInput.value = "";
                closeModal();
            });

            // 保存クリック時：画像が選ばれていて、まだ適用していないならモーダルを開く
            form.addEventListener("submit", function(e) {
                const f = fileInput.files && fileInput.files[0];
                if (f && (!hidden.value || hidden.value.trim() === "")) {
                    e.preventDefault();
                    // まだ編集が読み込まれてない場合もあるので、ここで開き直す
                    if (!state.loaded) loadFile(f);
                    else openModal();
                }
            });
        })();
    </script>

    <%@ include file="/WEB-INF/jsp/parts/cookie_banner.jspf" %>
</body>
</html>


