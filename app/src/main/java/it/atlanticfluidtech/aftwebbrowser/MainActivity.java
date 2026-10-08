package it.atlanticfluidtech.aftwebbrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<TabItem> tabs;
    private final ArrayList<ConfigurableWebView> webViews = new ArrayList<>();
    private final ArrayList<Button> tabButtons = new ArrayList<>();

    private LinearLayout tabContainer;
    private FrameLayout webContainer;
    private int activeIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String orientation = AppConfig.loadOrientation(this);
        if (AppConfig.ORIENTATION_LANDSCAPE.equals(orientation)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        hideSystemUi();

        tabContainer = findViewById(R.id.tabContainer);
        webContainer = findViewById(R.id.webContainer);
        Button btnConfig = findViewById(R.id.btnConfig);

        btnConfig.setOnClickListener(v -> openConfiguration());

        tabs = AppConfig.loadTabs(this);

        if (tabs.isEmpty()) {
            openConfiguration();
            return;
        }

        createTabsAndWebViews();
        switchToTab(0);
    }

    private void createTabsAndWebViews() {

        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            TabItem item = tabs.get(i);

            Button tabButton = new Button(this);
            tabButton.setText(item.name);
            tabButton.setAllCaps(false);
            tabButton.setTextSize(15);
            tabButton.setSingleLine(true);
            tabButton.setMinHeight(0);
            tabButton.setMinWidth(0);
            tabButton.setPadding(dp(18), 0, dp(18), 0);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            lp.setMargins(0, 0, dp(2), 0);
            tabButton.setLayoutParams(lp);

            tabButton.setOnClickListener(v -> switchToTab(index));

            tabContainer.addView(tabButton);
            tabButtons.add(tabButton);

            ConfigurableWebView webView = new ConfigurableWebView(this);
            webView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            webView.setVisibility(View.GONE);
            webView.setKeyboardEnabled(item.keyboardEnabled);
            configureWebView(webView, item);

            webContainer.addView(webView);
            webViews.add(webView);

            webView.loadUrl(item.url);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void configureWebView(ConfigurableWebView webView, TabItem item) {

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setBackgroundColor(Color.WHITE);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (!item.keyboardEnabled) {
                    disableKeyboardInHtml(view);
                    hideSoftKeyboard(webView);
                }
            }
        });

        if (!item.keyboardEnabled) {
            webView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_UP) {
                    webView.postDelayed(() -> hideSoftKeyboard(webView), 50);
                    webView.postDelayed(() -> hideSoftKeyboard(webView), 200);
                }
                return false;
            });
        }
    }

    private void switchToTab(int index) {

        if (index < 0 || index >= webViews.size()) return;

        for (int i = 0; i < webViews.size(); i++) {
            boolean selected = i == index;
            webViews.get(i).setVisibility(selected ? View.VISIBLE : View.GONE);

            Button b = tabButtons.get(i);
            if (selected) {
                b.setTypeface(null, Typeface.BOLD);
                b.setBackgroundColor(0xFFFFFFFF);
            } else {
                b.setTypeface(null, Typeface.NORMAL);
                b.setBackgroundColor(0xFFD0D0D0);
            }
        }

        activeIndex = index;

        TabItem active = tabs.get(index);

        if (active.keyboardEnabled) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        } else {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            );
            hideSoftKeyboard(webViews.get(index));
        }

        webViews.get(index).requestFocus();
    }

    private void disableKeyboardInHtml(WebView view) {
        String js =
                "(function() {" +
                " function aftNoKeyboard() {" +
                "  var f=document.querySelectorAll('input,textarea');" +
                "  for(var i=0;i<f.length;i++) {" +
                "   f[i].setAttribute('inputmode','none');" +
                "   f[i].setAttribute('autocomplete','off');" +
                "  }" +
                " }" +
                " aftNoKeyboard();" +
                " if(!window.__aftKeyboardObserver) {" +
                "  window.__aftKeyboardObserver=new MutationObserver(function(){aftNoKeyboard();});" +
                "  if(document.body) window.__aftKeyboardObserver.observe(document.body,{childList:true,subtree:true});" +
                " }" +
                "})();";

        view.evaluateJavascript(js, null);
    }

    private void hideSoftKeyboard(WebView view) {
        try {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (imm != null && view != null) {
                imm.hideSoftInputFromWindow(
                        view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
            }
        } catch (Exception ignored) {
        }
    }

    private void openConfiguration() {
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

        if (activeIndex >= 0 && activeIndex < tabs.size() &&
                !tabs.get(activeIndex).keyboardEnabled) {
            ConfigurableWebView w = webViews.get(activeIndex);
            w.postDelayed(() -> hideSoftKeyboard(w), 100);
        }
    }

    @Override
    public void onBackPressed() {
        if (activeIndex >= 0 && activeIndex < webViews.size()) {
            ConfigurableWebView w = webViews.get(activeIndex);
            if (w.canGoBack()) {
                w.goBack();
                return;
            }
        }

        hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        for (ConfigurableWebView w : webViews) {
            try {
                w.stopLoading();
                w.destroy();
            } catch (Exception ignored) {
            }
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
