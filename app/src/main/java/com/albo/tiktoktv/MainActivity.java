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

    // Desktop Chrome on Windows - TikTok serves full desktop site, no bot limits
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/122.0.0.0 Safari/537.36";

    // Desktop TikTok URL
    private static final String TIKTOK_URL = "https://www.tiktok.com/foryou";

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

        // Cookies - essential for login and session persistence
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // Desktop Chrome UA - bypasses mobile bot detection entirely
        settings.setUserAgentString(USER_AGENT);

        // Desktop viewport - tells TikTok to render the PC layout
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);

        // Media - allow autoplay without gesture
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Hardware acceleration for smooth video playback
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Keep all TikTok URLs inside the WebView
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

            // ---- Anti-detection ----
            // Remove webdriver flag
            "try { Object.defineProperty(navigator,'webdriver',{get:()=>undefined}); } catch(e){}" +

            // Fake plugins so TikTok thinks it's a real browser
            "try { Object.defineProperty(navigator,'plugins',{get:()=>[" +
            "  {name:'Chrome PDF Plugin',filename:'internal-pdf-viewer'}," +
            "  {name:'Chrome PDF Viewer',filename:'mhjfbmdgcfjbbpaeojofohoefgiehjai'}," +
            "  {name:'Native Client',filename:'internal-nacl-plugin'}" +
            "]}); } catch(e){}" +

            // Languages
            "try { Object.defineProperty(navigator,'languages',{get:()=>['en-GB','en-US','en']}); } catch(e){}" +

            // Chrome object
            "window.chrome = window.chrome || {runtime:{}};" +

            // Block cookie/tracking popups by auto-declining them
            "function dismissPopups() {" +
            "  var selectors = [" +
            "    '[data-e2e=\"cookie-banner\"] button'," +
            "    '.cookie-banner button'," +
            "    'button[class*=\"decline\"]'," +
            "    'button[class*=\"reject\"]'," +
            "    'button[class*=\"close\"]'," +
            "    '[aria-label=\"Close\"]'," +
            "    '[data-e2e=\"modal-close-inner-button\"]'" +
            "  ];" +
            "  selectors.forEach(function(sel) {" +
            "    var btns = document.querySelectorAll(sel);" +
            "    btns.forEach(function(btn) {" +
            "      var txt = (btn.innerText||'').toLowerCase();" +
            "      if (!txt || txt.includes('decline') || txt.includes('reject') || " +
            "          txt.includes('close') || txt.includes('later') || txt === 'x') {" +
            "        btn.click();" +
            "      }" +
            "    });" +
            "  });" +
            "}" +

            // Run popup dismissal repeatedly for first 10 seconds
            "dismissPopups();" +
            "var popupInterval = setInterval(dismissPopups, 800);" +
            "setTimeout(function(){ clearInterval(popupInterval); }, 10000);" +

            // ---- Remote control: arrow keys scroll the page ----
            // Desktop TikTok uses scroll, not swipe
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
                // Click the centre of the screen (play/pause)
                webView.loadUrl("javascript:(function(){" +
                    "var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);" +
                    "if(el) el.click();" +
                "})()");
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // Like the current video
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
