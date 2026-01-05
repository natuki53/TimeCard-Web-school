// 右上スライド通知（グループチャット + DM）
(function() {
  const POLL_MS = 2000;

  function guessContextPath() {
    // 例: /timecard-web-school/dashboard -> /timecard-web-school
    // ROOT直下なら "" になる
    const p = window.location && window.location.pathname ? window.location.pathname : "/";
    if (!p || p === "/") return "";
    const idx = p.indexOf("/", 1);
    if (idx <= 0) return "";
    return p.substring(0, idx);
  }

  const CTX = (typeof window.__contextPath === "string" && window.__contextPath.length > 0)
    ? window.__contextPath
    : guessContextPath();
  const API = CTX + "/api/notifications";

  function ensureNotifyStylesApplied() {
    // CSSがキャッシュ/未読み込みでも最低限の見た目（右上・右からスライド）を保証する
    const id = "notifyInlineStyle";
    if (document.getElementById(id)) return;

    // 既存CSSが効いているなら注入しない
    const tmp = document.createElement("div");
    tmp.className = "notify-toasts";
    tmp.style.display = "none";
    document.body.appendChild(tmp);
    const pos = window.getComputedStyle(tmp).position;
    tmp.remove();
    if (pos === "fixed") return;

    const style = document.createElement("style");
    style.id = id;
    style.textContent = `
.notify-toasts{
  position:fixed !important;
  top:76px !important;
  right:16px !important;
  left:auto !important;
  bottom:auto !important;
  display:flex !important;
  flex-direction:column !important;
  gap:10px !important;
  z-index:9999 !important;
  max-width:min(360px, calc(100vw - 32px)) !important;
}
.notify-toast{
  width:100% !important;
  text-align:left !important;
  border:1px solid rgba(0,0,0,0.10) !important;
  border-radius:12px !important;
  background:#fff !important;
  padding:12px 12px !important;
  box-shadow:0 10px 20px rgba(0,0,0,0.12) !important;
  cursor:pointer !important;
  transform:translateX(110%) !important;
  opacity:0 !important;
  transition:transform 220ms ease, opacity 220ms ease !important;
}
.notify-toast.is-visible{
  transform:translateX(0) !important;
  opacity:1 !important;
}
.notify-toast{display:grid !important;grid-template-columns:44px 1fr !important;gap:12px !important;align-items:start !important;}
.notify-left{width:44px !important;}
.notify-icon{width:44px !important;height:44px !important;border-radius:12px !important;object-fit:cover !important;background:#f3f4f6 !important;border:1px solid rgba(0,0,0,0.08) !important;}
.notify-right{min-width:0 !important;}
.notify-title{font-weight:700 !important;color:#111827 !important;margin-bottom:4px !important;font-size:15px !important;line-height:1.25 !important;}
.notify-from{color:#111827 !important;font-weight:650 !important;font-size:14px !important;line-height:1.25 !important;margin-bottom:2px !important;word-break:break-word !important;}
.notify-preview{color:#374151 !important;font-size:14px !important;line-height:1.35 !important;word-break:break-word !important;}
    `.trim();
    document.head.appendChild(style);
  }

  function ensureContainer() {
    let c = document.getElementById("notifyToasts");
    if (c) return c;
    ensureNotifyStylesApplied();
    c = document.createElement("div");
    c.id = "notifyToasts";
    c.className = "notify-toasts";
    document.body.appendChild(c);
    return c;
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

  function showToast(item) {
    const c = ensureContainer();
    const el = document.createElement("button");
    el.type = "button";
    el.className = "notify-toast";
    const title = escapeHtml(item.title || "");
    const from = escapeHtml(item.from || "");
    const preview = escapeHtml(item.preview || "");
    const iconUrl = escapeHtml(item.iconUrl || (CTX + "/img/icon_black.png"));
    el.innerHTML =
      '<div class="notify-left">' +
        '<img class="notify-icon" alt="" src="' + iconUrl + '">' +
      '</div>' +
      '<div class="notify-right">' +
        '<div class="notify-title">' + title + '</div>' +
        '<div class="notify-from">' + from + '</div>' +
        '<div class="notify-preview">' + preview + '</div>' +
      '</div>';

    el.addEventListener("click", function() {
      if (item.url) {
        window.location.href = item.url;
      }
    });

    c.appendChild(el);
    // アニメ開始
    requestAnimationFrame(function() {
      el.classList.add("is-visible");
    });

    // 自動で閉じる
    setTimeout(function() {
      el.classList.remove("is-visible");
      setTimeout(function() {
        try { el.remove(); } catch (e) {}
      }, 250);
    }, 5500);
  }

  async function poll() {
    try {
      const res = await fetch(API, { headers: { "Accept": "application/json" } });
      if (!res.ok) return;
      const items = await res.json();
      if (!items || items.length === 0) return;
      // 最大3つまで表示（多すぎると邪魔なので）
      for (let i = 0; i < Math.min(3, items.length); i++) {
        showToast(items[i]);
      }
    } catch (e) {
      // silent
    }
  }

  // ログインしてないページでも404/401になるだけなのでOK
  poll();
  setInterval(poll, POLL_MS);
})();


