package it.atlanticfluidtech.aftweblauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private ConfigurableWebView webView;
    private boolean keyboardEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs =
                getSharedPreferences(ConfigActivity.PREFS, MODE_PRIVATE);

        String url = prefs.getString(ConfigActivity.KEY_URL, "");
        keyboardEnabled = prefs.getBoolean(ConfigActivity.KEY_KEYBOARD, false);

        String orientation = prefs.getString(
                ConfigActivity.KEY_ORIENTATION,
                ConfigActivity.ORIENTATION_PORTRAIT
        );

        if (ConfigActivity.ORIENTATION_LANDSCAPE.equals(orientation)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (!keyboardEnabled) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            );
        } else {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }

        setContentView(R.layout.activity_main);
        hideSystemUi();

        webView = findViewById(R.id.webView);
        View configHotspot = findViewById(R.id.configHotspot);

        webView.setKeyboardEnabled(keyboardEnabled);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        configHotspot.setOnLongClickListener(v -> {
            openConfiguration();
            return true;
        });

        if (url == null || url.trim().isEmpty()) {
            openConfiguration();
            return;
        }

        configureWebView();
        webView.loadUrl(url.trim());
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void configureWebView() {

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setBackgroundColor(Color.WHITE);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (!keyboardEnabled) {
                    disableKeyboardInHtml(view);
                    hideSoftKeyboard();
                }
            }
        });

        if (!keyboardEnabled) {
            webView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_UP) {
                    webView.postDelayed(this::hideSoftKeyboard, 50);
                    webView.postDelayed(this::hideSoftKeyboard, 200);
                }
                return false;
            });
        } else {
            webView.setOnTouchListener(null);
        }
    }

    private void disableKeyboardInHtml(WebView view) {
        String javascript =
                "(function() {" +
                " function aftDisableSoftKeyboard() {" +
                "  var fields=document.querySelectorAll('input, textarea');" +
                "  for(var i=0;i<fields.length;i++) {" +
                "   fields[i].setAttribute('inputmode','none');" +
                "   fields[i].setAttribute('autocomplete','off');" +
                "  }" +
                " }" +
                " aftDisableSoftKeyboard();" +
                " if(!window.__aftKeyboardObserver) {" +
                "  window.__aftKeyboardObserver=new MutationObserver(function(){aftDisableSoftKeyboard();});" +
                "  if(document.body) window.__aftKeyboardObserver.observe(document.body,{childList:true,subtree:true});" +
                " }" +
                "})();";

        view.evaluateJavascript(javascript, null);
    }

    private void hideSoftKeyboard() {
        if (keyboardEnabled) return;

        try {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (imm != null && webView != null) {
                imm.hideSoftInputFromWindow(
                        webView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
            }
        } catch (Exception ignored) {
        }
    }

    private void openConfiguration() {
        Toast.makeText(this, "Configurazione Web Launcher", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ConfigActivity.class));
        finish();
    }

    private void hideSystemUi() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(
                        WindowInsets.Type.statusBars() |
                        WindowInsets.Type.navigationBars()
                );
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();

        if (!keyboardEnabled && webView != null) {
            webView.postDelayed(this::hideSoftKeyboard, 100);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            hideSystemUi();
        }
    }
}
