package com.textwidget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingActivity extends Activity {
    SharedPreferences b;
    TextView t;
    EditText e;
    String c;
    int id, bgcolor, tpcolor, color, gravity, size, space, line;
    SeekBar s, s1, s2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= 18) {
                actionBar.setHomeAsUpIndicator(R.drawable.close);
            }
        }
        setContentView(R.layout.setting);
        id = getIntent().getIntExtra("appWidgetId", -1);
        b = getSharedPreferences(String.valueOf(id), 0);
        bgcolor = b.getInt("bgcolor", 16777215);
        color = b.getInt("color", -1);
        tpcolor = -1;
        gravity = b.getInt("gravity", 17);
        size = b.getInt("size", 16);
        space = b.getInt("space", 10);
        line = b.getInt("line", 4);
        c = b.getString("content", null);
        t = findViewById(R.id.t);
        e = findViewById(R.id.e);
        e.setText(c);
        e.requestFocus();
        Button bf = findViewById(R.id.button_fontColor);
        bf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
                View v = View.inflate(SettingActivity.this, R.layout.color, null);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.getWindow().getAttributes().alpha = 0.85f;
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                SeekBar f = v.findViewById(R.id.m);
                SeekBar e = v.findViewById(R.id.o);
                SeekBar d = v.findViewById(R.id.q);
                SeekBar b = v.findViewById(R.id.u);
                SeekBar fg = v.findViewById(R.id.mb);
                SeekBar eg = v.findViewById(R.id.ob);
                SeekBar dg = v.findViewById(R.id.qb);
                SeekBar bg = v.findViewById(R.id.ub);
                SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                        color = (b.getProgress() << 24) | (f.getProgress() << 16) | (e.getProgress() << 8) | d.getProgress();
                        bgcolor = (bg.getProgress() << 24) | (fg.getProgress() << 16) | (eg.getProgress() << 8) | dg.getProgress();
                        updat();
                    }
                };
                f.setProgress(Color.red(color));
                e.setProgress(Color.green(color));
                d.setProgress(Color.blue(color));
                b.setProgress(color >>> 24);
                fg.setProgress(Color.red(bgcolor));
                eg.setProgress(Color.green(bgcolor));
                dg.setProgress(Color.blue(bgcolor));
                bg.setProgress(bgcolor >>> 24);
                for (SeekBar q : new SeekBar[]{f, e, d, b, fg, eg, dg, bg}) {
                    q.setOnSeekBarChangeListener(l);
                }
                dialog.setView(v);
                dialog.show();
            }
        });
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updat();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

        });
        s = findViewById(R.id.mainSeekBar1);
        s.setProgress(size);
        s1 = findViewById(R.id.mainSeekBar2);
        s1.setProgress(space);
        s2 = findViewById(R.id.mainSeekBar3);
        s2.setProgress(line);
        findViewById(R.id.sp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                s1.setProgress(10);
            }
        });
        findViewById(R.id.si).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                s.setProgress(16);
            }
        });
        SeekBar.OnSeekBarChangeListener cl = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                updat();
            }
        };
        for (SeekBar I : new SeekBar[]{s, s1, s2}) {
            I.setOnSeekBarChangeListener(cl);
        }
        updat();
        Button bt = findViewById(R.id.button_format);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView listView = new ListView(SettingActivity.this);
                listView.setAdapter(ArrayAdapter.createFromResource(SettingActivity.this, R.array.formats, android.R.layout.simple_spinner_dropdown_item));
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                final PopupWindow popupWindow = new PopupWindow(listView, displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2, true);
                popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view2, int i, long j) {
                        addFormatTad(i);
                        popupWindow.dismiss();
                    }
                });
                popupWindow.showAsDropDown(bt);
            }
        });
        findViewById(R.id.button_gravity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
                View v = View.inflate(SettingActivity.this, R.layout.gravity, null);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.getWindow().getAttributes().alpha = 0.85f;
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                View.OnClickListener ol = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gravity = Integer.parseInt(String.valueOf(view.getTag()));
                        dialog.dismiss();
                        updat();
                    }
                };
                for (Integer i : new int[]{R.id.b51, R.id.b49, R.id.b53, R.id.b17, R.id.b21, R.id.b19, R.id.b81, R.id.b83, R.id.b85}) {
                    v.findViewById(i).setOnClickListener(ol);
                }
                dialog.setView(v);
                dialog.show();
            }
        });
    }

    private void updat() {
        t.setBackgroundColor(bgcolor);
        t.setTextColor(color);
        t.setGravity(gravity);
        t.setText(Html.fromHtml(e.getText().toString().replace("\n", "<br>")));
        t.setTextSize(s.getProgress());
        if (Build.VERSION.SDK_INT >= 21)
            t.setLetterSpacing((s1.getProgress() - 10f) / 100);
        t.setLineSpacing(1f, (0.7f + (s2.getProgress() * 0.1f)));
    }

    public void ok() {
        b.edit().putInt("size", s.getProgress()).putInt("gravity", gravity).putInt("color", color).putInt("bgcolor", bgcolor).putInt("line", s2.getProgress()).putInt("space", s1.getProgress()).putString("content", e.getText().toString()).apply();
        setResult(-1, new Intent().putExtra("appWidgetId", id));
        sendBroadcast(new Intent(this, Widget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", new int[]{id}));
        finish();
    }

    public void addFormatTad(int i) {
        switch (i) {
            case 0:
                in("<b>");
                break;
            case 1:
                in("<i>");
                break;
            case 2:
                in("<u>");
                break;
            case 3:
                in("<small>");
                break;
            case 4:
                in("<big>");
                break;
            case 5:
                in("<sub>");
                break;
            case 6:
                in("<sup>");
                break;
            case 7:
                final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
                View v = View.inflate(SettingActivity.this, R.layout.tecolor, null);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.getWindow().getAttributes().alpha = 0.85f;
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                SeekBar m = v.findViewById(R.id.m1);
                SeekBar o = v.findViewById(R.id.o1);
                SeekBar q = v.findViewById(R.id.q1);
                SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                        tpcolor = (255 << 24) | (m.getProgress() << 16) | (o.getProgress() << 8) | q.getProgress();
                        ((TextView) v.findViewById(R.id.tt)).setTextColor(tpcolor);
                    }
                };
                for (SeekBar p : new SeekBar[]{m, o, q}) {
                    p.setOnSeekBarChangeListener(l);
                    p.setProgress(255);
                }
                v.findViewById(R.id.bb).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        in("<font color=\"" + String.format("#%06X", tpcolor & 16777215) + "\">");
                    }
                });
                dialog.setView(v);
                dialog.show();
                break;
            case 8:
                in("<strike>");
                break;
            default:
                break;
        }
    }

    public void in(String s2) {
        int B = e.getSelectionStart();
        int C = e.getSelectionEnd();
        e.getText().insert(e.getSelectionEnd(), "</>").insert(B, s2);
        e.setSelection(B + s2.length(), C + s2.length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.set, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add) ok();
        else finish();
        return super.onMenuItemSelected(i, menuItem);
    }
}
