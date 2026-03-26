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

    // Ad/tracker domains to block at network level
    private static final List<String> BLOCKED_DOMAINS = Arrays.asList(
        "ads.tiktok.com",
        "ad.tiktok.com",
        "analytics.tiktok.com",
        "log.tiktok.com",
        "mon.tiktok.com",
        "mcs.tiktok.com",
        "mon16.tiktok.com",
        "webcast-adcolony.tiktok.com",
        "doubleclick.net",
        "googlesyndication.com",
        "googletagmanager.com",
        "googletagservices.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "tiktok-event.com",
        "byteoversea.com",
        "bytetracking.com"
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

                // Block ad/tracker domains at network level - return empty response
                for (String blocked : BLOCKED_DOMAINS) {
                    if (url.contains(blocked)) {
                        return new WebResourceResponse(
                            "text/plain", "utf-8",
                            new ByteArrayInputStream("".getBytes())
                        );
                    }
                }

                // Block ad-related URL patterns
                if (url.contains("/ads/") || url.contains("/advert") ||
                    url.contains("sponsor") || url.contains("promoted") ||
                    url.contains("/tracking/") || url.contains("/pixel/") ||
                    url.contains("collect?") || url.contains("beacon")) {
                    return new WebResourceResponse(
                        "text/plain", "utf-8",
                        new ByteArrayInputStream("".getBytes())
                    );
                }

                return null; // Allow everything else
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

            // --- DOM ad blocker ---
            // Hide sponsored/ad video containers in TikTok's feed
            "function removeAds() {" +
            "  var adSelectors = [" +
            // Sponsored label containers
            "    '[data-e2e=\"video-desc\"] a[href*=\"/ad/\"]'," +
            "    'div[class*=\"DivAdContainer\"]'," +
            "    'div[class*=\"ad-container\"]'," +
            "    'div[class*=\"AdContainer\"]'," +
            // Sponsored tag
            "    'span[class*=\"SpanBadge\"]'," +
            // Any element with 'Sponsored' text - walk up and hide the video card
            "  ];" +

            // Hide elements matching selectors
            "  adSelectors.forEach(function(sel) {" +
            "    try {" +
            "      document.querySelectorAll(sel).forEach(function(el) {" +
            "        var card = el.closest('[class*=\"DivItemContainer\"]') || " +
            "                   el.closest('[class*=\"video-feed-item\"]') || el;" +
            "        card.style.display = 'none';" +
            "      });" +
            "    } catch(e){}" +
            "  });" +

            // Find and hide anything labelled 'Sponsored'
            "  document.querySelectorAll('*').forEach(function(el) {" +
            "    if (el.children.length === 0 && " +
            "        el.innerText && el.innerText.trim() === 'Sponsored') {" +
            "      var card = el.closest('[class*=\"DivItemContainer\"]') || " +
            "                 el.closest('article') || el.parentElement;" +
            "      if (card) card.style.display = 'none';" +
            "    }" +
            "  });" +
            "}" +

            // Run ad removal on load and watch for new content (infinite scroll)
            "removeAds();" +
            "var adObserver = new MutationObserver(function() { removeAds(); });" +
            "adObserver.observe(document.body, {childList:true, subtree:true});" +

            // --- Popup dismissal ---
            "function dismissPopups() {" +
            "  var selectors = [" +
            "    '[data-e2e=\"cookie-banner\"] button'," +
            "    '[data-e2e=\"modal-close-inner-button\"]'," +
            "    'button[class*=\"decline\"]'," +
            "    'button[class*=\"reject\"]'," +
            "    '[aria-label=\"Close\"]'," +
            "    '[aria-label=\"Dismiss\"]'" +
            "  ];" +
            "  selectors.forEach(function(sel) {" +
            "    document.querySelectorAll(sel).forEach(function(btn) {" +
            "      var txt = (btn.innerText||'').toLowerCase().trim();" +
            "      if (!txt || txt==='x' || txt.includes('decline') || " +
            "          txt.includes('reject') || txt.includes('close') || " +
            "          txt.includes('later') || txt.includes('dismiss')) {" +
            "        btn.click();" +
            "      }" +
            "    });" +
            "  });" +
            "}" +
            "dismissPopups();" +
            "var popupTimer = setInterval(dismissPopups, 800);" +
            "setTimeout(function(){ clearInterval(popupTimer); }, 15000);" +

            // --- Remote control ---
            "window.__scrollToNext = function() {" +
            "  window.scrollBy({top: window.innerHeight, behavior: 'smooth'});" +
            "};" +
            "window.__scrollToPrev = function() {" +
            "  window.scrollBy({top: -window.innerHeight, behavior: 'smooth'});" +
            "};" +

            "document.addEventListener('keydown', function(e) {" +
            "  if (e.key==='ArrowDown') { e.preventDefault(); window.__scrollToNext(); }" +
            "  else if (e.key==='ArrowUp') { e.preventDefault(); window.__scrollToPrev(); }" +
            "}, true);" +

        "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_PAGE_DOWN:
                webView.loadUrl("javascript:window.__scrollToNext && window.__scrollToNext()");
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_PAGE_UP:
                webView.loadUrl("javascript:window.__scrollToPrev && window.__scrollToPrev()");
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.loadUrl("javascript:(function(){" +
                    "var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);" +
                    "if(el) el.click();" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                webView.loadUrl("javascript:(function(){" +
                    "var like=document.querySelector('[data-e2e=\"like-icon\"],button[aria-label*=\"ike\"]');" +
                    "if(like) like.click();" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_BACK:
                if (webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
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
