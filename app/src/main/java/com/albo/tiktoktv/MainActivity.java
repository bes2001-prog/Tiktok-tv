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

    // TikTok mobile URL - loads the mobile layout which works well on TV
    private static final String TIKTOK_URL = "https://www.tiktok.com/foryou";

    // Simulate a mobile browser so TikTok serves the proper vertical video UI
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

        // Enable JavaScript (required for TikTok)
        settings.setJavaScriptEnabled(true);

        // Enable DOM storage for login sessions to persist
        settings.setDomStorageEnabled(true);

        // Enable cookies so login is remembered
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // Mobile user agent so TikTok serves the vertical video UI
        settings.setUserAgentString(USER_AGENT);

        // Media settings
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);

        // Zoom
        settings.setSupportZoom(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Cache for better performance
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Hardware acceleration for video
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Keep all navigation inside the WebView
                String url = request.getUrl().toString();
                if (url.startsWith("https://www.tiktok.com") ||
                    url.startsWith("https://m.tiktok.com") ||
                    url.startsWith("https://accounts.tiktok.com")) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);

                // Inject JS to make the page respond better to D-pad/remote
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

        // Allow webview to get focus for key events
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    /**
     * Inject JavaScript to map D-pad UP/DOWN to TikTok's swipe gestures.
     * UP = previous video, DOWN = next video.
     */
    private void injectRemoteControlJS() {
        String js = "javascript:(function() {" +
            "if (window.__alboTVInjected) return;" +
            "window.__alboTVInjected = true;" +

            // Helper: simulate a vertical swipe (touch events)
            "function simulateSwipe(startY, endY) {" +
            "  var el = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);" +
            "  if (!el) el = document.body;" +
            "  var t = new Date().getTime();" +
            "  var touch1 = new Touch({identifier: t, target: el, clientX: window.innerWidth/2, clientY: startY, pageX: window.innerWidth/2, pageY: startY});" +
            "  el.dispatchEvent(new TouchEvent('touchstart', {touches:[touch1], changedTouches:[touch1], bubbles:true}));" +
            "  var touch2 = new Touch({identifier: t, target: el, clientX: window.innerWidth/2, clientY: endY, pageX: window.innerWidth/2, pageY: endY});" +
            "  el.dispatchEvent(new TouchEvent('touchmove', {touches:[touch2], changedTouches:[touch2], bubbles:true}));" +
            "  el.dispatchEvent(new TouchEvent('touchend', {touches:[], changedTouches:[touch2], bubbles:true}));" +
            "}" +

            // Listen for keyboard events (D-pad maps to arrow keys)
            "document.addEventListener('keydown', function(e) {" +
            "  if (e.key === 'ArrowDown') {" +
            "    e.preventDefault();" +
            "    simulateSwipe(window.innerHeight * 0.7, window.innerHeight * 0.1);" + // swipe up = next video
            "  } else if (e.key === 'ArrowUp') {" +
            "    e.preventDefault();" +
            "    simulateSwipe(window.innerHeight * 0.3, window.innerHeight * 0.9);" + // swipe down = prev video
            "  }" +
            "}, true);" +
            "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Next video - simulate swipe up
                webView.loadUrl("javascript:simulateSwipe(window.innerHeight*0.7, window.innerHeight*0.1)");
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                // Previous video - simulate swipe down
                webView.loadUrl("javascript:simulateSwipe(window.innerHeight*0.3, window.innerHeight*0.9)");
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // Play/pause - tap centre of screen
                webView.loadUrl("javascript:(function(){" +
                    "var el = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);" +
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
        // Save cookies on pause
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
