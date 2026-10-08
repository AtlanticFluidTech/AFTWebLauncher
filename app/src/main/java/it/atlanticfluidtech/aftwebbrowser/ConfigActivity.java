package it.atlanticfluidtech.aftwebbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ConfigActivity extends Activity {

    private ArrayList<TabItem> tabs;
    private LinearLayout listContainer;
    private RadioButton rbPortrait;
    private RadioButton rbLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_config);

        listContainer = findViewById(R.id.listContainer);
        rbPortrait = findViewById(R.id.rbPortrait);
        rbLandscape = findViewById(R.id.rbLandscape);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnSave = findViewById(R.id.btnSave);

        tabs = AppConfig.loadTabs(this);

        String orientation = AppConfig.loadOrientation(this);
        if (AppConfig.ORIENTATION_LANDSCAPE.equals(orientation)) {
            rbLandscape.setChecked(true);
        } else {
            rbPortrait.setChecked(true);
        }

        renderList();

        btnAdd.setOnClickListener(v -> editTab(-1));

        btnSave.setOnClickListener(v -> {
            if (tabs.isEmpty()) {
                Toast.makeText(this, "Aggiungere almeno una pagina.", Toast.LENGTH_SHORT).show();
                return;
            }

            AppConfig.saveTabs(this, tabs);
            AppConfig.saveOrientation(
                    this,
                    rbLandscape.isChecked()
                            ? AppConfig.ORIENTATION_LANDSCAPE
                            : AppConfig.ORIENTATION_PORTRAIT
            );

            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void renderList() {
        listContainer.removeAllViews();

        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            TabItem item = tabs.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(10), dp(12), dp(10));

            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardLp.setMargins(0, 0, 0, dp(10));
            card.setLayoutParams(cardLp);
            card.setBackgroundColor(0xFFECECEC);

            TextView title = new TextView(this);
            title.setText((i + 1) + ". " + item.name);
            title.setTextSize(18);
            title.setTypeface(null, Typeface.BOLD);
            card.addView(title);

            TextView url = new TextView(this);
            url.setText(item.url);
            url.setTextSize(12);
            url.setPadding(0, dp(4), 0, dp(3));
            card.addView(url);

            TextView keyboard = new TextView(this);
            keyboard.setText("Tastiera Android: " + (item.keyboardEnabled ? "SI" : "NO"));
            keyboard.setTextSize(14);
            card.addView(keyboard);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.END);

            Button up = smallButton("↑");
            Button down = smallButton("↓");
            Button edit = smallButton("MODIFICA");
            Button delete = smallButton("ELIMINA");

            up.setEnabled(index > 0);
            down.setEnabled(index < tabs.size() - 1);

            up.setOnClickListener(v -> {
                TabItem t = tabs.remove(index);
                tabs.add(index - 1, t);
                renderList();
            });

            down.setOnClickListener(v -> {
                TabItem t = tabs.remove(index);
                tabs.add(index + 1, t);
                renderList();
            });

            edit.setOnClickListener(v -> editTab(index));

            delete.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("Elimina tab")
                            .setMessage("Eliminare "" + item.name + ""?")
                            .setPositiveButton("ELIMINA", (d, which) -> {
                                tabs.remove(index);
                                renderList();
                            })
                            .setNegativeButton("ANNULLA", null)
                            .show()
            );

            actions.addView(up);
            actions.addView(down);
            actions.addView(edit);
            actions.addView(delete);
            card.addView(actions);

            listContainer.addView(card);
        }
    }

    private Button smallButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setMinWidth(0);
        b.setMinHeight(0);
        b.setPadding(dp(8), 0, dp(8), 0);
        return b;
    }

    private void editTab(int index) {
        boolean editing = index >= 0;
        TabItem current = editing ? tabs.get(index) : new TabItem("", "", false);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(8), dp(20), 0);

        TextView n1 = new TextView(this);
        n1.setText("Nome tab");
        box.addView(n1);

        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setText(current.name);
        name.setHint("es. Avanzamento");
        box.addView(name);

        TextView n2 = new TextView(this);
        n2.setText("URL completo");
        n2.setPadding(0, dp(8), 0, 0);
        box.addView(n2);

        EditText url = new EditText(this);
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        url.setSingleLine(false);
        url.setMinLines(2);
        url.setMaxLines(4);
        url.setText(current.url);
        url.setHint("http://server:porta/pagina.aspx");
        box.addView(url);

        CheckBox keyboard = new CheckBox(this);
        keyboard.setText("Abilita tastiera virtuale Android");
        keyboard.setChecked(current.keyboardEnabled);
        keyboard.setPadding(0, dp(10), 0, 0);
        box.addView(keyboard);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Modifica pagina" : "Nuova pagina")
                .setView(box)
                .setPositiveButton("SALVA", null)
                .setNegativeButton("ANNULLA", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String n = name.getText().toString().trim();
                    String u = url.getText().toString().trim();

                    if (n.isEmpty()) {
                        name.setError("Inserire il nome");
                        return;
                    }

                    if (u.isEmpty()) {
                        url.setError("Inserire l'URL");
                        return;
                    }

                    String scheme = Uri.parse(u).getScheme();
                    if (scheme == null ||
                            (!scheme.equalsIgnoreCase("http") &&
                             !scheme.equalsIgnoreCase("https"))) {
                        url.setError("Usare http:// oppure https://");
                        return;
                    }

                    TabItem value = new TabItem(n, u, keyboard.isChecked());

                    if (editing) tabs.set(index, value);
                    else tabs.add(value);

                    dialog.dismiss();
                    renderList();
                }));

        dialog.show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
