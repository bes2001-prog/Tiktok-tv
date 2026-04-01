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
        setCookie("store-country-code", "gb");
        setCookie("store-country-code-src", "uid");
        setCookie("tt-target-idc", "eu-ttp2");
        setCookie("last_login_method", "QRcode");
        setCookie("multi_sids", "6879442232019239938%3A2e3f807a4314f6a57933477309fab8b7%7C7599839773492102147%3Ac82f8f20b556182101408650a9ba3a35");
        setCookie("cmpl_token", "AgQQAPOYF-RO0o9GXuVUYR078x474_MdP5QTYKD1oA");
        setCookie("sid_guard", "2e3f807a4314f6a57933477309fab8b7%7C1775083494%7C15552000%7CMon%2C+28-Sep-2026+22%3A44%3A54+GMT");
        setCookie("uid_tt", "32323e878c1af9ceafe0f7fc49127b0598dc88c870459d61b5974c17fa59a9b0");
        setCookie("uid_tt_ss", "32323e878c1af9ceafe0f7fc49127b0598dc88c870459d61b5974c17fa59a9b0");
        setCookie("sid_tt", "2e3f807a4314f6a57933477309fab8b7");
        setCookie("sessionid", "2e3f807a4314f6a57933477309fab8b7");
        setCookie("sessionid_ss", "2e3f807a4314f6a57933477309fab8b7");
        setCookie("tt_session_tlb_tag", "sttt%7C1%7CLj-AekMU9qV5M0dzCfq4t_________-unv1iKnB_cNbr1iEyhmXaCgedAEBASWmrufUo4rasMs8%3D");
        setCookie("sid_ucp_v1", "1.0.1-KGJiNjcyZmM4ZWRiYWRiN2I1YzE0M2I1NDM1ZTIwNjdhMzIwNTc5NDUKIgiCiM_irYisvF8Q5r-2zgYYswsgDDDZ-pbOBjgHQPQHSAQQBRoEbm8xYSIgMmUzZjgwN2E0MzE0ZjZhNTc5MzM0NzczMDlmYWI4YjcyTgogFboR1gNp1p7cuf7j7hTc1EeOTScOrtASnA1YjP-efXoSIKqLiqTjtexMnrSWKZSVC-uxMq15ukGUPWmsd9I3d9rKGAEiBnRpa3Rvaw");
        setCookie("ssid_ucp_v1", "1.0.1-KGJiNjcyZmM4ZWRiYWRiN2I1YzE0M2I1NDM1ZTIwNjdhMzIwNTc5NDUKIgiCiM_irYisvF8Q5r-2zgYYswsgDDDZ-pbOBjgHQPQHSAQQBRoEbm8xYSIgMmUzZjgwN2E0MzE0ZjZhNTc5MzM0NzczMDlmYWI4YjcyTgogFboR1gNp1p7cuf7j7hTc1EeOTScOrtASnA1YjP-efXoSIKqLiqTjtexMnrSWKZSVC-uxMq15ukGUPWmsd9I3d9rKGAEiBnRpa3Rvaw");
        setCookie("store-idc", "no1a");
        setCookie("tt-target-idc-sign", "JivxbBBCjQib1cTmJN-j4khZ5pyZU_JqTRyrI_prl4cG18VAXesKQX4u3aG6wlnAiqxxaDzw_0Ju8HsnhxG-bt6WQYRq9oYmUKcXLAB4tMd0ByQhB4P8-nSb496lf7ywyl2WDf5Nvw-XJ3_KBnV_0MoSdFAZ68wgn9_n935aREzMY3Ibsz5MZ-Z42gYZ-X5z0IwCVS65FYl9jL67B1HQxB0hj9qP7ypxwCsHx0q-xFw-P87UfJVbfe8Fs-n2dby4PSKlj4viH458ZKKHvX_IXUtFwHWtWVnIQFAoBnykTqb2jVgbrqc0k31Puf5wlILrraPktRunhw2PnP8qo-J8ed4WhTmL9KE8d0zMqx-JvgtmOginJ3-YCYWI8LNWh1NT6VL0fKHFdzmbi2X_MX4AgfQoPnl0ye-4tXQVamXNerOgPmehU63SZ_O0XFiLhkVOuXG96dLRAjODNGm0pULUFM8Qjqg0-ajzbT2EVqJVE268N1KdVRloLEepbQ037hR_");
        setCookie("msToken", "3UlCNf38712M7tU-pCqX4ENuNVFRqDh1wUCsD3TilBASDe0RkUOp_IvWWy43pKJS9bqxPM_d8J9KA37Yh3Q-l6ca-sVzrZrxVdJwfAqEeCG4bSLyjlFWBNNvugjeR_uh3zFXqAHuS5GYXg==");
        setCookie("odin_tt", "72acd1dc55ebc746d1c827b78815f504ece26e4825c9b298c18a6c7d304ebdb7bc8a79954e5dabd72e3ab3aaba2035cfc950dbd95acabb9ee54920298402a29ef31a4edd99b2afc9d92259a97e96a841");
        setCookie("perf_feed_cache", "{%22expireTimestamp%22:1775253600000%2C%22itemIds%22:[%227622823507633736982%22%2C%227623090119519554830%22%2C%227618946810656148750%22]}");
        setCookie("msToken", "BpFQMGORsgn04FWopIyxctGHzDWpWqxPTHJeAjfsh8-1wVitcO9qZiRX_TosEQe5G_nTFEnj2HiCv19ZDpgbabghs7LGkm4XUQmIvTdC1IJpoHIh_mweCE3ny4Aan5zadNew4w_CJnOYlg==");
        setCookie("store-country-sign", "MEIEDPq3uwq0kYNn06iYGgQgd5SeUnDuOQctO_0IvqQt8DliRTBDBS88SycLAE2gEiIEEDvL8EUK61thjhepvCoxB4o");

        CookieManager.getInstance().flush();
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

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
        setCookie("store-country-code", "gb");
        setCookie("store-country-code-src", "uid");
        setCookie("tt-target-idc", "eu-ttp2");
        setCookie("last_login_method", "QRcode");
        setCookie("multi_sids", "6879442232019239938%3A2e3f807a4314f6a57933477309fab8b7%7C7599839773492102147%3Ac82f8f20b556182101408650a9ba3a35");
        setCookie("cmpl_token", "AgQQAPOYF-RO0o9GXuVUYR078x474_MdP5QTYKD1oA");
        setCookie("sid_guard", "2e3f807a4314f6a57933477309fab8b7%7C1775083494%7C15552000%7CMon%2C+28-Sep-2026+22%3A44%3A54+GMT");
        setCookie("uid_tt", "32323e878c1af9ceafe0f7fc49127b0598dc88c870459d61b5974c17fa59a9b0");
        setCookie("uid_tt_ss", "32323e878c1af9ceafe0f7fc49127b0598dc88c870459d61b5974c17fa59a9b0");
        setCookie("sid_tt", "2e3f807a4314f6a57933477309fab8b7");
        setCookie("sessionid", "2e3f807a4314f6a57933477309fab8b7");
        setCookie("sessionid_ss", "2e3f807a4314f6a57933477309fab8b7");
        setCookie("tt_session_tlb_tag", "sttt%7C1%7CLj-AekMU9qV5M0dzCfq4t_________-unv1iKnB_cNbr1iEyhmXaCgedAEBASWmrufUo4rasMs8%3D");
        setCookie("sid_ucp_v1", "1.0.1-KGJiNjcyZmM4ZWRiYWRiN2I1YzE0M2I1NDM1ZTIwNjdhMzIwNTc5NDUKIgiCiM_irYisvF8Q5r-2zgYYswsgDDDZ-pbOBjgHQPQHSAQQBRoEbm8xYSIgMmUzZjgwN2E0MzE0ZjZhNTc5MzM0NzczMDlmYWI4YjcyTgogFboR1gNp1p7cuf7j7hTc1EeOTScOrtASnA1YjP-efXoSIKqLiqTjtexMnrSWKZSVC-uxMq15ukGUPWmsd9I3d9rKGAEiBnRpa3Rvaw");
        setCookie("ssid_ucp_v1", "1.0.1-KGJiNjcyZmM4ZWRiYWRiN2I1YzE0M2I1NDM1ZTIwNjdhMzIwNTc5NDUKIgiCiM_irYisvF8Q5r-2zgYYswsgDDDZ-pbOBjgHQPQHSAQQBRoEbm8xYSIgMmUzZjgwN2E0MzE0ZjZhNTc5MzM0NzczMDlmYWI4YjcyTgogFboR1gNp1p7cuf7j7hTc1EeOTScOrtASnA1YjP-efXoSIKqLiqTjtexMnrSWKZSVC-uxMq15ukGUPWmsd9I3d9rKGAEiBnRpa3Rvaw");
        setCookie("store-idc", "no1a");
        setCookie("tt-target-idc-sign", "JivxbBBCjQib1cTmJN-j4khZ5pyZU_JqTRyrI_prl4cG18VAXesKQX4u3aG6wlnAiqxxaDzw_0Ju8HsnhxG-bt6WQYRq9oYmUKcXLAB4tMd0ByQhB4P8-nSb496lf7ywyl2WDf5Nvw-XJ3_KBnV_0MoSdFAZ68wgn9_n935aREzMY3Ibsz5MZ-Z42gYZ-X5z0IwCVS65FYl9jL67B1HQxB0hj9qP7ypxwCsHx0q-xFw-P87UfJVbfe8Fs-n2dby4PSKlj4viH458ZKKHvX_IXUtFwHWtWVnIQFAoBnykTqb2jVgbrqc0k31Puf5wlILrraPktRunhw2PnP8qo-J8ed4WhTmL9KE8d0zMqx-JvgtmOginJ3-YCYWI8LNWh1NT6VL0fKHFdzmbi2X_MX4AgfQoPnl0ye-4tXQVamXNerOgPmehU63SZ_O0XFiLhkVOuXG96dLRAjODNGm0pULUFM8Qjqg0-ajzbT2EVqJVE268N1KdVRloLEepbQ037hR_");
        setCookie("msToken", "3UlCNf38712M7tU-pCqX4ENuNVFRqDh1wUCsD3TilBASDe0RkUOp_IvWWy43pKJS9bqxPM_d8J9KA37Yh3Q-l6ca-sVzrZrxVdJwfAqEeCG4bSLyjlFWBNNvugjeR_uh3zFXqAHuS5GYXg==");
        setCookie("odin_tt", "72acd1dc55ebc746d1c827b78815f504ece26e4825c9b298c18a6c7d304ebdb7bc8a79954e5dabd72e3ab3aaba2035cfc950dbd95acabb9ee54920298402a29ef31a4edd99b2afc9d92259a97e96a841");
        setCookie("perf_feed_cache", "{%22expireTimestamp%22:1775253600000%2C%22itemIds%22:[%227622823507633736982%22%2C%227623090119519554830%22%2C%227618946810656148750%22]}");
        setCookie("msToken", "BpFQMGORsgn04FWopIyxctGHzDWpWqxPTHJeAjfsh8-1wVitcO9qZiRX_TosEQe5G_nTFEnj2HiCv19ZDpgbabghs7LGkm4XUQmIvTdC1IJpoHIh_mweCE3ny4Aan5zadNew4w_CJnOYlg==");
        setCookie("store-country-sign", "MEIEDPq3uwq0kYNn06iYGgQgd5SeUnDuOQctO_0IvqQt8DliRTBDBS88SycLAE2gEiIEEDvL8EUK61thjhepvCoxB4o");

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
