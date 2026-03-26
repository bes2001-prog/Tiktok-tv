package com.albo.tiktoktv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;

    private static final String TIKTOK_URL = "https://www.tiktok.com/foryou";

    // Mobile user agent so TikTok serves the vertical video UI
    private static final String USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen, no status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        setupWebView();
        webView.loadUrl(TIKTOK_URL);
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Persist cookies so login is remembered between sessions
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        settings.setUserAgentString(USER_AGENT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // Block intent:// deep-links (cause ERR_UNKNOWN_URL_SCHEME crash)
                if (url.startsWith("intent://") || url.startsWith("snssdk")) {
                    return true; // swallow it, do nothing
                }

                // Allow TikTok and login pages, block everything else
                if (url.startsWith("https://www.tiktok.com") ||
                    url.startsWith("https://m.tiktok.com") ||
                    url.startsWith("https://accounts.tiktok.com") ||
                    url.startsWith("https://www.google.com") ||      // Google login
                    url.startsWith("https://accounts.google.com") || // Google OAuth
                    url.startsWith("https://appleid.apple.com")) {   // Apple login
                    return false; // let WebView handle it normally
                }

                // Block all other external URLs
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                injectJS();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    private void injectJS() {
        String js = "javascript:(function() {" +
            "if (window.__alboTVInjected) return;" +
            "window.__alboTVInjected = true;" +

            // --- Block the "Get the full app experience" popup ---
            // Hide it immediately and keep watching for it
            "function blockAppPopup() {" +
            "  var selectors = [" +
            "    '[class*=\"AppPromo\"]'," +
            "    '[class*=\"app-promo\"]'," +
            "    '[class*=\"modal\"]'," +
            "    '[class*=\"Modal\"]'," +
            "    '[class*=\"dialog\"]'," +
            "    '[class*=\"Dialog\"]'," +
            "    '[class*=\"download\"]'," +
            "    '[class*=\"Download\"]'," +
            "    '[class*=\"banner\"]'" +
            "  ];" +
            "  selectors.forEach(function(sel) {" +
            "    document.querySelectorAll(sel).forEach(function(el) {" +
            "      var txt = el.innerText || '';" +
            "      if (txt.indexOf('Open TikTok') >= 0 || txt.indexOf('full app') >= 0 || txt.indexOf('Get the') >= 0) {" +
            "        el.style.display = 'none';" +
            "      }" +
            "    });" +
            "  });" +
            // Also click 'Not now' or X buttons automatically
            "  document.querySelectorAll('button, [role=\"button\"]').forEach(function(btn) {" +
            "    var t = btn.innerText || btn.getAttribute('aria-label') || '';" +
            "    if (t.indexOf('Not now') >= 0 || t.indexOf('not now') >= 0) {" +
            "      btn.click();" +
            "    }" +
            "  });" +
            "}" +

            // Run on load and keep checking every 2 seconds
            "blockAppPopup();" +
            "setInterval(blockAppPopup, 2000);" +

            // Also use MutationObserver to catch popups as they appear
            "var observer = new MutationObserver(function() { blockAppPopup(); });" +
            "observer.observe(document.body, { childList: true, subtree: true });" +

            // --- D-pad swipe simulation ---
            "function simulateSwipe(startY, endY) {" +
            "  var el = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);" +
            "  if (!el) el = document.body;" +
            "  var t = Date.now();" +
            "  function mkTouch(id, y) {" +
            "    return new Touch({identifier:id, target:el, clientX:window.innerWidth/2, clientY:y, pageX:window.innerWidth/2, pageY:y});" +
            "  }" +
            "  var t1 = mkTouch(t, startY);" +
            "  el.dispatchEvent(new TouchEvent('touchstart', {touches:[t1], changedTouches:[t1], bubbles:true}));" +
            "  var t2 = mkTouch(t, (startY+endY)/2);" +
            "  el.dispatchEvent(new TouchEvent('touchmove', {touches:[t2], changedTouches:[t2], bubbles:true}));" +
            "  var t3 = mkTouch(t, endY);" +
            "  el.dispatchEvent(new TouchEvent('touchend', {touches:[], changedTouches:[t3], bubbles:true}));" +
            "}" +
            "window.__swipe = simulateSwipe;" +

            // D-pad arrow key listener
            "document.addEventListener('keydown', function(e) {" +
            "  if (e.key === 'ArrowDown') { e.preventDefault(); simulateSwipe(window.innerHeight*0.7, window.innerHeight*0.1); }" +
            "  else if (e.key === 'ArrowUp') { e.preventDefault(); simulateSwipe(window.innerHeight*0.3, window.innerHeight*0.9); }" +
            "}, true);" +

            "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                webView.loadUrl("javascript:window.__swipe && window.__swipe(window.innerHeight*0.7, window.innerHeight*0.1)");
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                webView.loadUrl("javascript:window.__swipe && window.__swipe(window.innerHeight*0.3, window.innerHeight*0.9)");
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.loadUrl("javascript:(function(){var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);if(el)el.click();})()");
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (webView.canGoBack()) { webView.goBack(); return true; }
                return super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
