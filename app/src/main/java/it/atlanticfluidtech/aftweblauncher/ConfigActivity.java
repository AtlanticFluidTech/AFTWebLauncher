package it.atlanticfluidtech.aftweblauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class ConfigActivity extends Activity {

    public static final String PREFS = "AFT_WEB_LAUNCHER_CONFIG";
    public static final String KEY_URL = "url";
    public static final String KEY_KEYBOARD = "keyboard_enabled";
    public static final String KEY_ORIENTATION = "orientation";

    public static final String ORIENTATION_PORTRAIT = "portrait";
    public static final String ORIENTATION_LANDSCAPE = "landscape";

    private EditText txtUrl;
    private RadioButton rbKeyboardOff;
    private RadioButton rbKeyboardOn;
    private RadioButton rbPortrait;
    private RadioButton rbLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_config);

        txtUrl = findViewById(R.id.txtUrl);
        rbKeyboardOff = findViewById(R.id.rbKeyboardOff);
        rbKeyboardOn = findViewById(R.id.rbKeyboardOn);
        rbPortrait = findViewById(R.id.rbPortrait);
        rbLandscape = findViewById(R.id.rbLandscape);
        Button btnSave = findViewById(R.id.btnSave);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        String currentUrl = prefs.getString(KEY_URL, "");
        boolean keyboardEnabled = prefs.getBoolean(KEY_KEYBOARD, false);
        String orientation = prefs.getString(KEY_ORIENTATION, ORIENTATION_PORTRAIT);

        if (currentUrl == null) currentUrl = "";
        txtUrl.setText(currentUrl);

        if (keyboardEnabled) rbKeyboardOn.setChecked(true);
        else rbKeyboardOff.setChecked(true);

        if (ORIENTATION_LANDSCAPE.equals(orientation)) rbLandscape.setChecked(true);
        else rbPortrait.setChecked(true);

        btnSave.setOnClickListener(v -> saveAndStart());
    }

    private void saveAndStart() {
        String url = txtUrl.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(this, "Inserire l'URL completo.", Toast.LENGTH_SHORT).show();
            txtUrl.requestFocus();
            return;
        }

        Uri parsed = Uri.parse(url);
        String scheme = parsed.getScheme();

        if (scheme == null ||
                (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            Toast.makeText(
                    this,
                    "L'URL deve iniziare con http:// oppure https://",
                    Toast.LENGTH_LONG
            ).show();
            txtUrl.requestFocus();
            return;
        }

        boolean keyboardEnabled = rbKeyboardOn.isChecked();
        String orientation = rbLandscape.isChecked()
                ? ORIENTATION_LANDSCAPE
                : ORIENTATION_PORTRAIT;

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_URL, url)
                .putBoolean(KEY_KEYBOARD, keyboardEnabled)
                .putString(KEY_ORIENTATION, orientation)
                .apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
