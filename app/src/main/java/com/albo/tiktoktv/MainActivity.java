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

    // The full JS we inject - built once and reused
    private String injectedJS = null;

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
                // Full re-inject on every page load (covers hard navigations)
                injectJS(true);
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

    /**
     * Builds and injects JS.
     * forceReinject = true on full page loads (clears __alboTV flag)
     * forceReinject = false when called from key events (only reinjects if not present)
     */
    private void injectJS(boolean forceReinject) {

        // The core JS - runs once per page but survives SPA navigation via history API hooks
        String js = "javascript:(function() {" +

            // On full page reload, always re-run
            (forceReinject ? "window.__alboTV = false;" : "") +

            "if (window.__alboTV) return;" +
            "window.__alboTV = true;" +

            // ===================== ANTI-DETECTION =====================
            "try{Object.defineProperty(navigator,'webdriver',{get:()=>undefined});}catch(e){}" +
            "try{Object.defineProperty(navigator,'plugins',{get:()=>[" +
            "  {name:'Chrome PDF Plugin',filename:'internal-pdf-viewer'}," +
            "  {name:'Chrome PDF Viewer',filename:'mhjfbmdgcfjbbpaeojofohoefgiehjai'}," +
            "  {name:'Native Client',filename:'internal-nacl-plugin'}" +
            "]});}catch(e){}" +
            "try{Object.defineProperty(navigator,'languages',{get:()=>['en-GB','en-US','en']});}catch(e){}" +
            "window.chrome=window.chrome||{runtime:{}};" +

            // ===================== FOCUS STYLE =====================
            "if (!document.getElementById('__alboStyle')) {" +
            "  var s=document.createElement('style');" +
            "  s.id='__alboStyle';" +
            "  s.innerHTML='.__alboFocus{outline:3px solid #FE2C55!important;outline-offset:3px!important;border-radius:4px!important;z-index:99999!important;position:relative!important;}';" +
            "  document.head.appendChild(s);" +
            "}" +

            // ===================== STATE =====================
            "window.__focusIdx = -1;" +
            "window.__focusMode = false;" +

            // ===================== FOCUSABLE ELEMENTS =====================
            // Broad selector to catch everything on any TikTok page/overlay/modal
            "var FOCUSABLE_SEL = [" +
            "  'a[href]','button','input','select','textarea'," +
            "  '[role=\"button\"]','[role=\"link\"]','[role=\"tab\"]','[role=\"menuitem\"]'," +
            "  '[role=\"option\"]','[role=\"checkbox\"]','[role=\"radio\"]'," +
            "  '[tabindex]:not([tabindex=\"-1\"])'" +
            "].join(',');" +

            "function getFocusable() {" +
            "  return Array.from(document.querySelectorAll(FOCUSABLE_SEL)).filter(function(el) {" +
            "    if (el.offsetWidth===0 || el.offsetHeight===0) return false;" +
            "    if (el.style.display==='none' || el.style.visibility==='hidden') return false;" +
            "    if (el.disabled) return false;" +
            "    var r=el.getBoundingClientRect();" +
            "    return r.width>0 && r.height>0;" +
            "  });" +
            "}" +

            "function clearFocus() {" +
            "  document.querySelectorAll('.__alboFocus').forEach(function(el){" +
            "    el.classList.remove('__alboFocus');" +
            "  });" +
            "}" +

            "function focusEl(idx) {" +
            "  var els=getFocusable();" +
            "  if (!els.length) return;" +
            "  idx=Math.max(0,Math.min(idx,els.length-1));" +
            "  window.__focusIdx=idx;" +
            "  clearFocus();" +
            "  var el=els[idx];" +
            "  el.classList.add('__alboFocus');" +
            "  el.scrollIntoView({block:'nearest',inline:'nearest'});" +
            "  el.focus();" +
            "}" +

            "function clickFocused() {" +
            "  var els=getFocusable();" +
            "  if (window.__focusIdx>=0 && window.__focusIdx<els.length) {" +
            "    var el=els[window.__focusIdx];" +
            "    el.click();" +
            "    el.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));" +
            "    el.dispatchEvent(new MouseEvent('mouseup',{bubbles:true}));" +
            "  }" +
            "}" +

            "function exitNavMode() {" +
            "  window.__focusMode=false;" +
            "  window.__focusIdx=-1;" +
            "  clearFocus();" +
            "}" +

            // ===================== SCROLL (video mode) =====================
            "window.__scrollToNext=function(){window.scrollBy({top:window.innerHeight,behavior:'smooth'});};" +
            "window.__scrollToPrev=function(){window.scrollBy({top:-window.innerHeight,behavior:'smooth'});};" +

            // ===================== KEY HANDLER =====================
            "document.addEventListener('keydown',function(e){" +

            // LEFT / RIGHT — enter nav mode, move between elements
            "  if(e.key==='ArrowLeft'||e.key==='ArrowRight'){" +
            "    e.preventDefault();e.stopPropagation();" +
            "    if(!window.__focusMode){window.__focusMode=true;window.__focusIdx=-1;}" +
            "    focusEl(e.key==='ArrowRight'?window.__focusIdx+1:window.__focusIdx-1);" +
            "    return;" +
            "  }" +

            // UP / DOWN — scroll videos OR navigate in nav mode
            "  if(e.key==='ArrowDown'){" +
            "    e.preventDefault();e.stopPropagation();" +
            "    if(window.__focusMode) focusEl(window.__focusIdx+1);" +
            "    else window.__scrollToNext();" +
            "    return;" +
            "  }" +
            "  if(e.key==='ArrowUp'){" +
            "    e.preventDefault();e.stopPropagation();" +
            "    if(window.__focusMode) focusEl(window.__focusIdx-1);" +
            "    else window.__scrollToPrev();" +
            "    return;" +
            "  }" +

            // ENTER — click focused or play/pause
            "  if(e.key==='Enter'){" +
            "    e.preventDefault();e.stopPropagation();" +
            "    if(window.__focusMode){" +
            "      clickFocused();" +
            "    } else {" +
            "      var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);" +
            "      if(el) el.click();" +
            "    }" +
            "    return;" +
            "  }" +

            // ESCAPE / BACK — exit nav mode
            "  if(e.key==='Escape'){" +
            "    e.preventDefault();" +
            "    exitNavMode();" +
            "    return;" +
            "  }" +

            "},true);" + // useCapture=true so we catch events before TikTok does

            // ===================== SPA NAVIGATION HOOK =====================
            // TikTok is a single-page app — sub-pages and overlays don't trigger
            // onPageFinished. We hook pushState/replaceState and popstate to
            // re-run our setup after every route change.
            "(function() {" +
            "  function onRouteChange() {" +
            "    exitNavMode();" +
            "" +
            "    if (!document.getElementById('__alboStyle')) {" +
            "      var s=document.createElement('style');" +
            "      s.id='__alboStyle';" +
            "      s.innerHTML='.__alboFocus{outline:3px solid #FE2C55!important;outline-offset:3px!important;border-radius:4px!important;z-index:99999!important;position:relative!important;}';" +
            "      document.head.appendChild(s);" +
            "    }" +
            "" +
            "    var pt=setInterval(dismissPopups,800);" +
            "    setTimeout(function(){clearInterval(pt);},10000);" +
            "  }" +
            "  var origPush=history.pushState.bind(history);" +
            "  history.pushState=function(){origPush.apply(history,arguments);onRouteChange();};" +
            "  var origReplace=history.replaceState.bind(history);" +
            "  history.replaceState=function(){origReplace.apply(history,arguments);onRouteChange();};" +
            "  window.addEventListener('popstate',onRouteChange);" +
            "})();" +

            // ===================== AD REMOVAL =====================
            "function removeAds(){" +
            "  document.querySelectorAll('*').forEach(function(el){" +
            "    if(el.children.length===0&&el.innerText&&el.innerText.trim()==='Sponsored'){" +
            "      var card=el.closest('[class*=\"DivItemContainer\"]')||el.closest('article')||el.parentElement;" +
            "      if(card) card.style.display='none';" +
            "    }" +
            "  });" +
            "}" +
            "removeAds();" +
            "new MutationObserver(removeAds).observe(document.body,{childList:true,subtree:true});" +

            // ===================== POPUP DISMISSAL =====================
            "function dismissPopups(){" +
            "  ['[data-e2e=\"cookie-banner\"] button'," +
            "   '[data-e2e=\"modal-close-inner-button\"]'," +
            "   'button[class*=\"decline\"]','button[class*=\"reject\"]'," +
            "   '[aria-label=\"Close\"]','[aria-label=\"Dismiss\"]'" +
            "  ].forEach(function(sel){" +
            "    document.querySelectorAll(sel).forEach(function(btn){" +
            "      var txt=(btn.innerText||'').toLowerCase().trim();" +
            "      if(!txt||txt==='x'||txt.includes('decline')||txt.includes('close')||" +
            "         txt.includes('later')||txt.includes('dismiss'))btn.click();" +
            "    });" +
            "  });" +
            "}" +
            "dismissPopups();" +
            "var pt=setInterval(dismissPopups,800);" +
            "setTimeout(function(){clearInterval(pt);},15000);" +

        "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_PAGE_DOWN:
                webView.loadUrl("javascript:if(window.__focusMode){var els=document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]').values ? Array.from(document.querySelectorAll('a[href],button,[role=\"button\"]')).filter(e=>e.offsetWidth>0&&e.offsetHeight>0) : [];window.__focusIdx=Math.min((window.__focusIdx||0)+1,els.length-1);if(els[window.__focusIdx]){document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});}}else{window.__scrollToNext&&window.__scrollToNext();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_PAGE_UP:
                webView.loadUrl("javascript:if(window.__focusMode){var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"]')).filter(e=>e.offsetWidth>0&&e.offsetHeight>0);window.__focusIdx=Math.max((window.__focusIdx||0)-1,0);if(els[window.__focusIdx]){document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});}}else{window.__scrollToPrev&&window.__scrollToPrev();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                webView.loadUrl("javascript:window.__focusMode=true;var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]')).filter(e=>e.offsetWidth>0&&e.offsetHeight>0);window.__focusIdx=Math.max((window.__focusIdx>=0?window.__focusIdx:1)-1,0);document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});els[window.__focusIdx].focus();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                webView.loadUrl("javascript:window.__focusMode=true;var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]')).filter(e=>e.offsetWidth>0&&e.offsetHeight>0);window.__focusIdx=Math.min((window.__focusIdx>=0?window.__focusIdx:-1)+1,els.length-1);document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});els[window.__focusIdx].focus();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.loadUrl("javascript:if(window.__focusMode){var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]')).filter(e=>e.offsetWidth>0&&e.offsetHeight>0);if(els[window.__focusIdx]){var el=els[window.__focusIdx];el.click();el.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));el.dispatchEvent(new MouseEvent('mouseup',{bubbles:true}));}}else{var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);if(el)el.click();}");
                return true;
            case KeyEvent.KEYCODE_BACK:
                webView.loadUrl("javascript:if(window.__focusMode){window.__focusMode=false;window.__focusIdx=-1;document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));}");
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
        // Re-inject on resume in case TikTok cleared our JS state
        injectJS(false);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
