// Cookie利用通知・同意バナー（初回のみ表示）
(function () {
  function init() {
    var KEY = "cookieConsentAccepted";

    function getAccepted() {
      try {
        return window.localStorage && window.localStorage.getItem(KEY) === "1";
      } catch (e) {
        return false;
      }
    }

    function setAccepted() {
      try {
        if (window.localStorage) window.localStorage.setItem(KEY, "1");
      } catch (e) {}
    }

    function hide() {
      var el = document.getElementById("cookieBanner");
      if (!el) return;
      el.classList.remove("is-visible");
      el.classList.add("is-hidden");
      el.setAttribute("aria-hidden", "true");
    }

    function show() {
      var el = document.getElementById("cookieBanner");
      if (!el) return;
      el.classList.remove("is-hidden");
      // 次フレームで visible を付けてトランジションが確実に走るようにする
      requestAnimationFrame(function () {
        el.classList.add("is-visible");
      });
      el.setAttribute("aria-hidden", "false");
    }

    if (!getAccepted()) {
      show();
      var accept = document.getElementById("cookieAccept");
      var later = document.getElementById("cookieLater");

      if (accept) {
        accept.addEventListener("click", function () {
          setAccepted();
          hide();
        });
      }
      if (later) {
        later.addEventListener("click", function () {
          hide();
        });
      }
    } else {
      hide();
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();


