package com.albo.tiktoktv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private boolean jsInjected = false;

    private static final String TIKTOK_URL = "https://www.tiktok.com/foryou";

    // Real Chrome on Android user agent - looks identical to a real phone browser
    private static final String USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7 Build/TQ3A.230901.001) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.6099.144 Mobile Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        // Accept all cookies including third party
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // Real Chrome UA - critical for passing TikTok's bot detection
        settings.setUserAgentString(USER_AGENT);

        // Make WebView behave like a real browser
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Viewport
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);

        // Cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);

        // Hardware acceleration for smooth video
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // Add extra headers to every request to look like a real browser
                return null;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("https://www.tiktok.com") ||
                    url.startsWith("https://m.tiktok.com") ||
                    url.startsWith("https://accounts.tiktok.com") ||
                    url.startsWith("https://sf16") ||
                    url.startsWith("https://lf16")) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                jsInjected = false;
                injectAntiDetectionJS();
                injectRemoteControlJS();
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
     * Overwrite WebView fingerprinting properties so TikTok thinks
     * it's talking to a real Chrome browser on Android.
     */
    private void injectAntiDetectionJS() {
        if (jsInjected) return;
        jsInjected = true;

        String js = "javascript:(function() {" +

            // Remove webdriver flag - most important anti-bot check
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +

            // Fake plugins array (real browsers have plugins, WebView has none)
            "Object.defineProperty(navigator, 'plugins', {get: () => [" +
            "  {name:'Chrome PDF Plugin', filename:'internal-pdf-viewer', description:'Portable Document Format'}," +
            "  {name:'Chrome PDF Viewer', filename:'mhjfbmdgcfjbbpaeojofohoefgiehjai', description:''}," +
            "  {name:'Native Client', filename:'internal-nacl-plugin', description:''}" +
            "]});" +

            // Fake languages
            "Object.defineProperty(navigator, 'languages', {get: () => ['en-GB','en']});" +

            // Fake platform
            "Object.defineProperty(navigator, 'platform', {get: () => 'Linux aarch64'});" +

            // Fake hardwareConcurrency (real phones show 8 cores)
            "Object.defineProperty(navigator, 'hardwareConcurrency', {get: () => 8});" +

            // Fake deviceMemory
            "Object.defineProperty(navigator, 'deviceMemory', {get: () => 8});" +

            // Fake connection type
            "if (navigator.connection) {" +
            "  Object.defineProperty(navigator.connection, 'effectiveType', {get: () => '4g'});" +
            "}" +

            // Override chrome object to look like real Chrome
            "window.chrome = {runtime: {}, loadTimes: function(){}, csi: function(){}};" +

            // Fake touch support (TikTok expects a touch device)
            "Object.defineProperty(navigator, 'maxTouchPoints', {get: () => 5});" +

            // Prevent TikTok from detecting automation via toString checks
            "const origToString = Function.prototype.toString;" +
            "Function.prototype.toString = function() {" +
            "  if (this === Function.prototype.toString) return origToString.call(this);" +
            "  if (this === Object.defineProperty) return 'function defineProperty() { [native code] }';" +
            "  return origToString.call(this);" +
            "};" +

            "console.log('Anti-detection active');" +
        "})();";

        webView.loadUrl(js);
    }

    /**
     * Map D-pad keys to TikTok swipe gestures.
     * DOWN = next video (swipe up), UP = previous video (swipe down).
     */
    private void injectRemoteControlJS() {
        String js = "javascript:(function() {" +
            "if (window.__alboTVInjected) return;" +
            "window.__alboTVInjected = true;" +

            "function simulateSwipe(startY, endY) {" +
            "  var cx = window.innerWidth / 2;" +
            "  var el = document.elementFromPoint(cx, window.innerHeight/2) || document.body;" +
            "  var t = Date.now();" +
            "  function makeTouch(id, y) {" +
            "    return new Touch({identifier:id, target:el, clientX:cx, clientY:y, pageX:cx, pageY:y, radiusX:10, radiusY:10, force:1});" +
            "  }" +
            "  var steps = 8;" +
            "  var t1 = makeTouch(t, startY);" +
            "  el.dispatchEvent(new TouchEvent('touchstart',{touches:[t1],changedTouches:[t1],bubbles:true,cancelable:true}));" +
            "  for (var i=1; i<=steps; i++) {" +
            "    (function(step){" +
            "      setTimeout(function(){" +
            "        var y = startY + (endY - startY) * step / steps;" +
            "        var tm = makeTouch(t, y);" +
            "        el.dispatchEvent(new TouchEvent('touchmove',{touches:[tm],changedTouches:[tm],bubbles:true,cancelable:true}));" +
            "      }, step * 20);" +
            "    })(i);" +
            "  }" +
            "  setTimeout(function(){" +
            "    var t2 = makeTouch(t, endY);" +
            "    el.dispatchEvent(new TouchEvent('touchend',{touches:[],changedTouches:[t2],bubbles:true,cancelable:true}));" +
            "  }, steps * 20 + 30);" +
            "}" +

            // Make swipe function globally accessible for onKeyDown
            "window.swipeNext = function() { simulateSwipe(window.innerHeight*0.75, window.innerHeight*0.15); };" +
            "window.swipePrev = function() { simulateSwipe(window.innerHeight*0.25, window.innerHeight*0.85); };" +

            "document.addEventListener('keydown', function(e) {" +
            "  if (e.key==='ArrowDown') { e.preventDefault(); window.swipeNext(); }" +
            "  else if (e.key==='ArrowUp') { e.preventDefault(); window.swipePrev(); }" +
            "}, true);" +

            "console.log('Remote control active');" +
        "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                webView.loadUrl("javascript:window.swipeNext && window.swipeNext()");
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                webView.loadUrl("javascript:window.swipePrev && window.swipePrev()");
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.loadUrl("javascript:(function(){" +
                    "var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);" +
                    "if(el){el.click();}" +
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
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
