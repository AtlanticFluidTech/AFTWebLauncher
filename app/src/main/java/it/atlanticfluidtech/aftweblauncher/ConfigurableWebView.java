package it.atlanticfluidtech.aftweblauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class ConfigurableWebView extends WebView {

    private boolean keyboardEnabled = true;

    public ConfigurableWebView(Context context) {
        super(context);
    }

    public ConfigurableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConfigurableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setKeyboardEnabled(boolean enabled) {
        keyboardEnabled = enabled;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return keyboardEnabled ? super.onCheckIsTextEditor() : false;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return keyboardEnabled ? super.onCreateInputConnection(outAttrs) : null;
    }
}
