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

        // Inject cookies BEFORE loading any URL
        injectCookies();

        setupWebView();
        webView.loadUrl(TIKTOK_URL);
    }

    private void setCookie(String name, String value) {
        CookieManager cm = CookieManager.getInstance();
        cm.setCookie(".tiktok.com", name + "=" + value + "; Domain=.tiktok.com; Path=/");
        cm.setCookie(".www.tiktok.com", name + "=" + value + "; Domain=.tiktok.com; Path=/");
    }

    private void injectCookies() {
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // All cookies from your PC TikTok session - injected before page load
        setCookie("tt_csrf_token", "yCzQy4EG-SISoZcuNUoL5cIu05_bdnHtda-A");
        setCookie("tt_chain_token", "Sbrb4AlAu8/Ib/mJ/L0lhw==");
        setCookie("tiktok_webapp_theme_source", "auto");
        setCookie("tiktok_webapp_theme", "dark");
        setCookie("passport_csrf_token", "7239bd5d90a4db46d6c272c9419ddee3");
        setCookie("passport_csrf_token_default", "7239bd5d90a4db46d6c272c9419ddee3");
        setCookie("s_v_web_id", "verify_ml43w946_kOqEtTuH_OIwd_4qyN_8aAb_K0C9h8y0Xdf9");
        setCookie("cookie-consent", "{%22optional%22:true%2C%22ga%22:true%2C%22af%22:true%2C%22fbp%22:true%2C%22lip%22:true%2C%22bing%22:true%2C%22ttads%22:true%2C%22reddit%22:true%2C%22hubspot%22:true%2C%22version%22:%22v10%22}");
        setCookie("passport_fe_beating_status", "true");
        setCookie("delay_guest_mode_vid", "5");
        setCookie("d_ticket", "38e5f7f7dd7ca6f7e1c13905291b566b924f9");
        setCookie("ttwid", "1%7CpL3xBCnvmQHIPzHv45q1-ThRCJH6VZxf-fsr-QUhF4c%7C1772806239%7Cd97d263aac539a9ade40a153d369cb0992278030fd267f99ace0846690ac9a21");
        setCookie("multi_sids", "6879442232019239938%3A075437bfcdc9d0b1cc98357625b922ea%7C7599839773492102147%3Ac82f8f20b556182101408650a9ba3a35");
        setCookie("cmpl_token", "AgQQAPOYF-RO0o9GXuVUYR078x474_MdP5QTYKDISA");
        setCookie("sid_guard", "075437bfcdc9d0b1cc98357625b922ea%7C1774644292%7C15552000%7CWed%2C+23-Sep-2026+20%3A44%3A52+GMT");
        setCookie("uid_tt", "bff5a2ce34fe4829f2c843c997be1013e8a0dcd5564fef1f75085c2185e6f83d");
        setCookie("uid_tt_ss", "bff5a2ce34fe4829f2c843c997be1013e8a0dcd5564fef1f75085c2185e6f83d");
        setCookie("sid_tt", "075437bfcdc9d0b1cc98357625b922ea");
        setCookie("sessionid", "075437bfcdc9d0b1cc98357625b922ea");
        setCookie("sessionid_ss", "075437bfcdc9d0b1cc98357625b922ea");
        setCookie("tt_session_tlb_tag", "sttt%7C4%7CB1Q3v83J0LHMmDV2Jbki6v_________7U5d00oxG0HkY1-WxDzJPqT7TOWduBCZR1KgfVo64_XA%3D");
        setCookie("sid_ucp_v1", "1.0.1-KGIxOGI5YjA0MzQ0MWUyNWEzYmJiMjY4MjE0Mzk3ZDk0MTk0ZWRiZDcKIgiCiM_irYisvF8QxNibzgYYswsgDDDY-pbOBjgHQPQHSAQQBRoEbm8xYSIgMDc1NDM3YmZjZGM5ZDBiMWNjOTgzNTc2MjViOTIyZWEyTgog8HRod9g2e5RVAKbtkfofNxSkKMiqwe13lOjxex1T1OgSIAKmI2QK8odO7R2a03Jw6TB_N-nmeAGSdNsLiP_V3nT3GAIiBnRpa3Rvaw");
        setCookie("ssid_ucp_v1", "1.0.1-KGIxOGI5YjA0MzQ0MWUyNWEzYmJiMjY4MjE0Mzk3ZDk0MTk0ZWRiZDcKIgiCiM_irYisvF8QxNibzgYYswsgDDDY-pbOBjgHQPQHSAQQBRoEbm8xYSIgMDc1NDM3YmZjZGM5ZDBiMWNjOTgzNTc2MjViOTIyZWEyTgog8HRod9g2e5RVAKbtkfofNxSkKMiqwe13lOjxex1T1OgSIAKmI2QK8odO7R2a03Jw6TB_N-nmeAGSdNsLiP_V3nT3GAIiBnRpa3Rvaw");
        setCookie("store-idc", "no1a");
        setCookie("store-country-code", "gb");
        setCookie("store-country-code-src", "uid");
        setCookie("tt-target-idc", "eu-ttp2");
        setCookie("tt-target-idc-sign", "MNWiD-y5qX0VbMORLtKLNq5XaMoAmawz1D4jB7SUz-Mb1RGl_knwvoqHLldRwfST_HSliE-dCZD6TQE3kMAs-wL_AdSGor9YguwIbDs38bGSLMyY9wkWAZ1GSw9gR4k_PlbhFvdfey5DgAt2hTJzZPJzmJ2H_wDtVPzKfGZC_S1JfuD-gHl96zuT61d3yHrq2qKbd7eSX_wVRf7uoZV60ypBzY23Smv7RWXFw27p63uswDbBsQSYluD7qYtUTXbWdtfMGEX6cbPHyPjw5TI6jIOEdXkS5uQ66rtVUWZhjII95q074d4erg2dljpmdI6yWqponVppkyflQv1vJEUat1j3sj3Kb-IETAcbAInXGquLeYYuzlj76Zyep49_lfpNaddyltPjE4JDhmkY_N6FNBk0aICAXH_hNRkDiio4UbndLe06_plVvxjAZWwXxyGJlOp99lL_bkyORo6iyclRR_RwBrY8Vc6wvNieisP8Ogb66thJvMtHa5BzXd3Xz8zG");
        setCookie("last_login_method", "QRcode");
        setCookie("odin_tt", "679c665e4b7702dd415c47ad53238c10df14ae15a2b879e62efe39aecba22f84f25d056042543e20e7468c4afbb99ceedfdb76b61308c927ec1769820d15c727de7b5155d1831459e8e1179f3d7c16d1");
        setCookie("msToken", "6GVmjrdYpjaTrOna51TpwvfyqjKuzd8D6WcyE9EE-VGOo3dm0RyPfGFOxgl_Uj7gMz3vspPEj0CcyR5DxynISAo0nReqQogqV36WdW6tRPxmGY5h7rhwZAdyoulqyg3lvX5N4N8liyCbXEdE0FEXUDyNyw==");
        setCookie("perf_feed_cache", "{%22expireTimestamp%22:1774814400000%2C%22itemIds%22:[%227621941575752486158%22%2C%227612610746278530326%22%2C%227621973459148623126%22]}");
        setCookie("store-country-sign", "MEIEDNRdJxA8MpvyBuux0AQgkDoE6HRM_aSi6XJHyIXDQB1RncS5SdHsc9QOskC4zGYEEEWjdqO3_t35s-ea8VO3nDM");

        CookieManager.getInstance().flush();
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
                if (url.contains("/ads/") || url.contains("/tracking/") || url.contains("/pixel/")) {
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

    private void injectJS(boolean forceReinject) {
        String js = "javascript:(function() {" +
            (forceReinject ? "window.__alboTV=false;" : "") +
            "if(window.__alboTV)return;" +
            "window.__alboTV=true;" +

            "try{Object.defineProperty(navigator,'webdriver',{get:()=>undefined});}catch(e){}" +
            "try{Object.defineProperty(navigator,'plugins',{get:()=>[" +
            "  {name:'Chrome PDF Plugin',filename:'internal-pdf-viewer'}," +
            "  {name:'Chrome PDF Viewer',filename:'mhjfbmdgcfjbbpaeojofohoefgiehjai'}," +
            "  {name:'Native Client',filename:'internal-nacl-plugin'}" +
            "]});}catch(e){}" +
            "try{Object.defineProperty(navigator,'languages',{get:()=>['en-GB','en-US','en']});}catch(e){}" +
            "window.chrome=window.chrome||{runtime:{}};" +

            "if(!document.getElementById('__alboStyle')){" +
            "  var s=document.createElement('style');" +
            "  s.id='__alboStyle';" +
            "  s.innerHTML='.__alboFocus{outline:3px solid #FE2C55!important;outline-offset:3px!important;border-radius:4px!important;z-index:99999!important;position:relative!important;}';" +
            "  document.head.appendChild(s);" +
            "}" +

            "window.__focusIdx=-1;" +
            "window.__focusMode=false;" +

            "var FOCUSABLE_SEL='a[href],button,input,select,textarea,[role=\"button\"],[role=\"link\"],[role=\"tab\"],[role=\"menuitem\"],[role=\"option\"],[tabindex]:not([tabindex=\"-1\"])';" +

            "function getFocusable(){" +
            "  return Array.from(document.querySelectorAll(FOCUSABLE_SEL)).filter(function(el){" +
            "    if(el.offsetWidth===0||el.offsetHeight===0)return false;" +
            "    if(el.disabled)return false;" +
            "    var r=el.getBoundingClientRect();" +
            "    return r.width>0&&r.height>0;" +
            "  });" +
            "}" +
            "function clearFocus(){document.querySelectorAll('.__alboFocus').forEach(function(el){el.classList.remove('__alboFocus');});}" +
            "function focusEl(idx){" +
            "  var els=getFocusable();" +
            "  if(!els.length)return;" +
            "  idx=Math.max(0,Math.min(idx,els.length-1));" +
            "  window.__focusIdx=idx;" +
            "  clearFocus();" +
            "  var el=els[idx];" +
            "  el.classList.add('__alboFocus');" +
            "  el.scrollIntoView({block:'nearest',inline:'nearest'});" +
            "  el.focus();" +
            "}" +
            "function clickFocused(){" +
            "  var els=getFocusable();" +
            "  if(window.__focusIdx>=0&&window.__focusIdx<els.length){" +
            "    var el=els[window.__focusIdx];" +
            "    el.click();" +
            "    el.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));" +
            "    el.dispatchEvent(new MouseEvent('mouseup',{bubbles:true}));" +
            "  }" +
            "}" +
            "function exitNavMode(){window.__focusMode=false;window.__focusIdx=-1;clearFocus();}" +

            "window.__scrollToNext=function(){window.scrollBy({top:window.innerHeight,behavior:'smooth'});};" +
            "window.__scrollToPrev=function(){window.scrollBy({top:-window.innerHeight,behavior:'smooth'});};" +

            "document.addEventListener('keydown',function(e){" +
            "  if(e.key==='ArrowLeft'||e.key==='ArrowRight'){" +
            "    e.preventDefault();e.stopPropagation();" +
            "    if(!window.__focusMode){window.__focusMode=true;window.__focusIdx=-1;}" +
            "    focusEl(e.key==='ArrowRight'?window.__focusIdx+1:window.__focusIdx-1);" +
            "    return;" +
            "  }" +
            "  if(e.key==='ArrowDown'){e.preventDefault();e.stopPropagation();if(window.__focusMode)focusEl(window.__focusIdx+1);else window.__scrollToNext();return;}" +
            "  if(e.key==='ArrowUp'){e.preventDefault();e.stopPropagation();if(window.__focusMode)focusEl(window.__focusIdx-1);else window.__scrollToPrev();return;}" +
            "  if(e.key==='Enter'){e.preventDefault();e.stopPropagation();if(window.__focusMode){clickFocused();}else{var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);if(el)el.click();}return;}" +
            "  if(e.key==='Escape'){e.preventDefault();exitNavMode();return;}" +
            "},true);" +

            "(function(){" +
            "  function onRouteChange(){exitNavMode();if(!document.getElementById('__alboStyle')){var s=document.createElement('style');s.id='__alboStyle';s.innerHTML='.__alboFocus{outline:3px solid #FE2C55!important;outline-offset:3px!important;border-radius:4px!important;z-index:99999!important;position:relative!important;}';document.head.appendChild(s);}var pt=setInterval(dismissPopups,800);setTimeout(function(){clearInterval(pt);},10000);}" +
            "  var origPush=history.pushState.bind(history);" +
            "  history.pushState=function(){origPush.apply(history,arguments);onRouteChange();};" +
            "  var origReplace=history.replaceState.bind(history);" +
            "  history.replaceState=function(){origReplace.apply(history,arguments);onRouteChange();};" +
            "  window.addEventListener('popstate',onRouteChange);" +
            "})();" +

            "function removeAds(){document.querySelectorAll('*').forEach(function(el){if(el.children.length===0&&el.innerText&&el.innerText.trim()==='Sponsored'){var card=el.closest('[class*=\"DivItemContainer\"]')||el.closest('article')||el.parentElement;if(card)card.style.display='none';}});}" +
            "removeAds();" +
            "new MutationObserver(removeAds).observe(document.body,{childList:true,subtree:true});" +

            "function dismissPopups(){" +
            "  ['[data-e2e=\"cookie-banner\"] button','[data-e2e=\"modal-close-inner-button\"]'," +
            "   'button[class*=\"decline\"]','button[class*=\"reject\"]','[aria-label=\"Close\"]','[aria-label=\"Dismiss\"]'" +
            "  ].forEach(function(sel){document.querySelectorAll(sel).forEach(function(btn){var txt=(btn.innerText||'').toLowerCase().trim();if(!txt||txt==='x'||txt.includes('decline')||txt.includes('close')||txt.includes('later'))btn.click();});});" +
            "}" +
            "dismissPopups();" +
            "var pt=setInterval(dismissPopups,800);setTimeout(function(){clearInterval(pt);},15000);" +

        "})();";

        webView.loadUrl(js);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_PAGE_DOWN:
                webView.loadUrl("javascript:if(window.__focusMode){var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"]')).filter(e=>e.offsetWidth>0);window.__focusIdx=Math.min((window.__focusIdx||0)+1,els.length-1);document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});}}else{window.__scrollToNext&&window.__scrollToNext();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_PAGE_UP:
                webView.loadUrl("javascript:if(window.__focusMode){var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"]')).filter(e=>e.offsetWidth>0);window.__focusIdx=Math.max((window.__focusIdx||0)-1,0);document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});}}else{window.__scrollToPrev&&window.__scrollToPrev();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                webView.loadUrl("javascript:window.__focusMode=true;var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]')).filter(e=>e.offsetWidth>0);window.__focusIdx=Math.max((window.__focusIdx>=0?window.__focusIdx:1)-1,0);document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});els[window.__focusIdx].focus();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                webView.loadUrl("javascript:window.__focusMode=true;var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]')).filter(e=>e.offsetWidth>0);window.__focusIdx=Math.min((window.__focusIdx>=0?window.__focusIdx:-1)+1,els.length-1);document.querySelectorAll('.__alboFocus').forEach(e=>e.classList.remove('__alboFocus'));if(els[window.__focusIdx]){els[window.__focusIdx].classList.add('__alboFocus');els[window.__focusIdx].scrollIntoView({block:'nearest'});els[window.__focusIdx].focus();}");
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.loadUrl("javascript:if(window.__focusMode){var els=Array.from(document.querySelectorAll('a[href],button,[role=\"button\"],[tabindex]')).filter(e=>e.offsetWidth>0);if(els[window.__focusIdx]){var el=els[window.__focusIdx];el.click();el.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));el.dispatchEvent(new MouseEvent('mouseup',{bubbles:true}));}}else{var el=document.elementFromPoint(window.innerWidth/2,window.innerHeight/2);if(el)el.click();}");
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
        injectJS(false);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
