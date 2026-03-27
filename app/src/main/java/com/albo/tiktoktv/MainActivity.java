package com.albo.tiktoktv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;

    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/122.0.0.0 Safari/537.36";

    private static final String TIKTOK_URL = "https://www.tiktok.com/foryou";

    private static final List<String> BLOCKED_DOMAINS = Arrays.asList(
        "ads.tiktok.com", "ad.tiktok.com", "analytics.tiktok.com",
        "log.tiktok.com", "mon.tiktok.com", "mcs.tiktok.com",
        "doubleclick.net", "googlesyndication.com", "googletagmanager.com",
        "adservice.google.com", "pagead2.googlesyndication.com",
        "bytetracking.com", "byteoversea.com"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
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
        settings.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        settings.setUserAgentString(USER_AGENT);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                for (String blocked : BLOCKED_DOMAINS) {
                    if (url.contains(blocked)) {
                        return new WebResourceResponse("text/plain", "utf-8",
                            new ByteArrayInputStream("".getBytes()));
                    }
                }
                if (url.contains("/ads/") || url.contains("/advert") ||
                    url.contains("/tracking/") || url.contains("/pixel/")) {
                    return new WebResourceResponse("text/plain", "utf-8",
                        new ByteArrayInputStream("".getBytes()));
                }
                return null;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("tiktok.com") || url.contains("tiktokcdn.com")) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
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
            "if (window.__alboTV) return;" +
            "window.__alboTV = true;" +

            // --- Anti-detection ---
            "try { Object.defineProperty(navigator,'webdriver',{get:()=>undefined}); } catch(e){}" +
            "try { Object.defineProperty(navigator,'plugins',{get:()=>[" +
            "  {name:'Chrome PDF Plugin',filename:'internal-pdf-viewer'}," +
            "  {name:'Chrome PDF Viewer',filename:'mhjfbmdgcfjbbpaeojofohoefgiehjai'}," +
            "  {name:'Native Client',filename:'internal-nacl-plugin'}" +
            "]}); } catch(e){}" +
            "try { Object.defineProperty(navigator,'languages',{get:()=>['en-GB','en-US','en']}); } catch(e){}" +
            "window.chrome = window.chrome || {runtime:{}};" +

            // --- TV Navigation System ---
            // All clickable elements TikTok uses
            "var FOCUSABLE = [" +
            "  'a','button','input','select','textarea'," +
            "  '[role=\"button\"]','[role=\"link\"]','[role=\"tab\"]'," +
            "  '[tabindex]','[data-e2e]'" +
            "].join(',');" +

            // Highlight style injected once
            "var styleEl = document.createElement('style');" +
            "styleEl.innerHTML = '.__alboFocus { outline: 3px solid #FE2C55 !important; outline-offset: 2px !important; border-radius: 4px !important; }';" +
            "document.head.appendChild(styleEl);" +

            // Current focused element index
            "window.__focusIdx = -1;" +
            "window.__focusMode = false;" + // false = scroll mode, true = nav mode

            // Get all visible focusable elements
            "function getFocusable() {" +
            "  var all = Array.from(document.querySelectorAll(FOCUSABLE));" +
            "  return all.filter(function(el) {" +
            "    var r = el.getBoundingClientRect();" +
            "    return r.width > 0 && r.height > 0 && " +
            "           r.top >= -50 && r.bottom <= window.innerHeight + 50;" +
            "  });" +
            "}" +

            // Clear all highlights
            "function clearFocus() {" +
            "  document.querySelectorAll('.__alboFocus').forEach(function(el){" +
            "    el.classList.remove('__alboFocus');" +
            "  });" +
            "}" +

            // Focus element at index
            "function focusEl(idx) {" +
            "  var els = getFocusable();" +
            "  if (!els.length) return;" +
            "  idx = Math.max(0, Math.min(idx, els.length-1));" +
            "  window.__focusIdx = idx;" +
            "  clearFocus();" +
            "  var el = els[idx];" +
            "  el.classList.add('__alboFocus');" +
            "  el.scrollIntoView({block:'nearest', inline:'nearest'});" +
            "  el.focus();" +
            "}" +

            // Click currently focused element
            "function clickFocused() {" +
            "  var els = getFocusable();" +
            "  if (window.__focusIdx >= 0 && window.__focusIdx < els.length) {" +
            "    els[window.__focusIdx].click();" +
            "  }" +
            "}" +

            // --- Scroll mode (default - up/down moves between videos) ---
            "window.__scrollToNext = function() {" +
            "  window.scrollBy({top: window.innerHeight, behavior: 'smooth'});" +
            "};" +
            "window.__scrollToPrev = function() {" +
            "  window.scrollBy({top: -window.innerHeight, behavior: 'smooth'});" +
            "};" +

            // --- Key handler ---
            // UP/DOWN = scroll videos (scroll mode) or navigate elements (nav mode)
            // LEFT/RIGHT = always navigate elements (enters nav mode automatically)
            // OK/Enter = click focused element or play/pause
            // BACK exits nav mode back to scroll mode
            "document.addEventListener('keydown', function(e) {" +

            "  if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {" +
            "    e.preventDefault();" +
            "    if (!window.__focusMode) {" +
            "      window.__focusMode = true;" +
            "      window.__focusIdx = -1;" +
            "    }" +
            "    if (e.key === 'ArrowRight') {" +
            "      focusEl(window.__focusIdx + 1);" +
            "    } else {" +
            "      focusEl(window.__focusIdx - 1);" +
            "    }" +
            "    return;" +
            "  }" +

            "  if (e.key === 'ArrowDown') {" +
            "    e.preventDefault();" +
            "    if (window.__focusMode) {" +
            "      focusEl(window.__focusIdx + 1);" +
            "    } else {" +
            "      window.__scrollToNext();" +
            "    }" +
            "    return;" +
            "  }" +

            "  if (e.key === 'ArrowUp') {" +
            "    e.preventDefault();" +
            "    if (window.__focusMode) {" +
            "      focusEl(window.__focusIdx - 1);" +
            "    } else {" +
            "      window.__scrollToPrev();" +
            "    }" +
            "    return;" +
            "  }" +

            "  if (e.key === 'Enter') {" +
            "    e.preventDefault();" +
            "    if (window.__focusMode) {" +
            "      clickFocused();" +
            "    } else {" +
            "      var el = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);" +
            "      if (el) el.click();" +
            "    }" +
            "    return;" +
            "  }" +

            "  if (e.key === 'Escape' || e.key === 'Backspace') {" +
            "    if (window.__focusMode) {" +
            "      e.preventDefault();" +
            "      window.__focusMode = false;" +
            "      clearFocus();" +
            "      window.__focusIdx = -1;" +
            "    }" +
            "    return;" +
            "  }" +

            "}, true);" +

            // --- Ad removal ---
            "function removeAds() {" +
            "  document.querySelectorAll('*').forEach(function(el) {" +
            "    if (el.children.length === 0 && el.innerText && " +
            "        el.innerText.trim() === 'Sponsored') {" +
            "      var card = el.closest('[class*=\"DivItemContainer\"]') || " +
            "                 el.closest('article') || el.parentElement;" +
            "      if (card) card.style.display = 'none';" +
            "    }" +
            "  });" +
            "}" +
            "removeAds();" +
            "new MutationObserver(removeAds).observe(document.body,{childList:true,subtree:true});" +

            // --- Popup dismissal ---
            "function dismissPopups() {" +
            "  ['[data-e2e=\"cookie-banner\"] button'," +
            "   '[data-e2e=\"modal-close-inner-button\"]'," +
            "   'button[class*=\"decline\"]','button[class*=\"reject\"]'," +
            "   '[aria-label=\"Close\"]','[aria-label=\"Dismiss\"]'" +
            "  ].forEach(function(sel) {" +
            "    document.querySelectorAll(sel).forEach(function(btn) {" +
            "      var txt = (btn.innerText||'').toLowerCase().trim();" +
            "      if (!txt||txt==='x'||txt.includes('decline')||txt.includes('close')||" +
            "          txt.includes('later')||txt.includes('dismiss')) btn.click();" +
            "    });" +
            "  });" +
            "}" +
            "dismissPopups();" +
            "var pt = setInterval(dismissPopups, 800);" +
            "setTimeout(function(){clearInterval(pt);}, 15000);" +

        "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_PAGE_DOWN:
                webView.loadUrl("javascript:(function(){" +
                    "if(window.__focusMode){" +
                    "  var els=document.querySelectorAll('a,button,input,[role=\"button\"],[data-e2e]');" +
                    "  window.__focusIdx=Math.min(window.__focusIdx+1,els.length-1);" +
                    "  if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].focus();}" +
                    "} else { window.__scrollToNext && window.__scrollToNext(); }" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_PAGE_UP:
                webView.loadUrl("javascript:(function(){" +
                    "if(window.__focusMode){" +
                    "  window.__focusIdx=Math.max(window.__focusIdx-1,0);" +
                    "} else { window.__scrollToPrev && window.__scrollToPrev(); }" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                webView.loadUrl("javascript:(function(){" +
                    "window.__focusMode=true;" +
                    "if(window.__focusIdx<0) window.__focusIdx=0;" +
                    "else window.__focusIdx=Math.max(window.__focusIdx-1,0);" +
                    "var els=document.querySelectorAll('a,button,[role=\"button\"],[data-e2e]');" +
                    "document.querySelectorAll('.__alboFocus').forEach(function(e){e.classList.remove('__alboFocus');});" +
                    "if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});els[window.__focusIdx].focus();}" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                webView.loadUrl("javascript:(function(){" +
                    "window.__focusMode=true;" +
                    "if(window.__focusIdx<0) window.__focusIdx=0;" +
                    "else window.__focusIdx=window.__focusIdx+1;" +
                    "var els=document.querySelectorAll('a,button,[role=\"button\"],[data-e2e]');" +
                    "window.__focusIdx=Math.min(window.__focusIdx,els.length-1);" +
                    "document.querySelectorAll('.__alboFocus').forEach(function(e){e.classList.remove('__alboFocus');});" +
                    "if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});els[window.__focusIdx].focus();}" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.loadUrl("javascript:(function(){" +
                    "if(window.__focusMode){" +
                    "  var els=document.querySelectorAll('a,button,[role=\"button\"],[data-e2e]');" +
                    "  if(els[window.__focusIdx]) els[window.__focusIdx].click();" +
                    "} else {" +
                    "  var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);" +
                    "  if(el) el.click();" +
                    "}" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_BACK:
                // Back exits nav mode first, then goes back in history
                webView.loadUrl("javascript:(function(){" +
                    "if(window.__focusMode){" +
                    "  window.__focusMode=false;" +
                    "  window.__focusIdx=-1;" +
                    "  document.querySelectorAll('.__alboFocus').forEach(function(e){e.classList.remove('__alboFocus');});" +
                    "}" +
                "})()");
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                return true;

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
