package com.textwidget;

import android.app.ActionBar;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    class adapter extends BaseAdapter {
        private final int[] data;
        private final int length;

        public adapter(int[] data, int length) {
            super();
            this.data = data;
            this.length = length;
        }

        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.r, null);
                holder = new ViewHolder();
                holder.textView = convertView.findViewById(R.id.b);
                holder.imageButton = convertView.findViewById(R.id.c);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            SharedPreferences b = getSharedPreferences(String.valueOf(data[position]), 0);
            if (position >= length) {
                holder.imageButton.setVisibility(View.INVISIBLE);
                holder.imageButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            holder.imageButton.setImageResource(b.getBoolean("lock", false) ? R.drawable.lock : R.drawable.unlock);
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    b.edit().putBoolean("lock", !b.getBoolean("lock", false)).apply();
                    holder.imageButton.setImageResource(b.getBoolean("lock", false) ? R.drawable.lock : R.drawable.unlock);
                    sendBroadcast(new Intent(MainActivity.this, TextWidget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", new int[]{data[position]}));
                }
            });
            SpannableString ss = new SpannableString(position < length ? Html.fromHtml(b.getString("content", "\n").replace("\n", "<br>")) : b.getString("currentString", "请刷新小部件"));
            ss.setSpan(new TypefaceSpan(b.getString("fontFamily", "sans-serif")), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int textShadowId;
            switch (b.getInt("shadowType", 0)) {
                case 0:
                    textShadowId = -1;
                    break;
                case 1:
                    textShadowId = R.style.textShadow1;
                    break;
                case 2:
                    textShadowId = R.style.textShadow2;
                    break;
                case 3:
                    textShadowId = R.style.textShadow3;
                    break;
                default:
                    textShadowId = R.style.textShadow4;
                    break;
            }
            if (textShadowId != -1)
                ss.setSpan(new TextAppearanceSpan(MainActivity.this, textShadowId), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.textView.setText(ss);
            holder.textView.setTextSize(b.getInt("size", 16));
            holder.textView.setTextColor(b.getInt("color", -1));
            holder.textView.setBackgroundColor(b.getInt("bgcolor", 16777215));
            holder.textView.setGravity(b.getInt("gravity", 17));
            if (Build.VERSION.SDK_INT >= 21)
                holder.textView.setLetterSpacing((b.getInt("space", 10) - 10f) / 100);
            holder.textView.setLineSpacing(1f, 0.7f + (b.getInt("line", 3) * 0.1f));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(position < length ? new Intent(MainActivity.this, SettingActivity.class).putExtra("appWidgetId", data[position]) : new Intent(MainActivity.this, SyncSettingActivity.class).putExtra("appWidgetId", data[position]));
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView textView;
            ImageButton imageButton;
        }

    }


    int[] s;
    ListView a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(805306368));
        setContentView(R.layout.main);
        //设置导航栏透明，UI会好看些
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            window.setNavigationBarContrastEnforced(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(805306368);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        int[] widgets1 = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, TextWidget.class));
        int[] widgets2 = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, SyncWidget.class));
        s = new int[widgets1.length + widgets2.length];
        System.arraycopy(widgets1, 0, s, 0, widgets1.length);
        System.arraycopy(widgets2, 0, s, widgets1.length, widgets2.length);
        a = findViewById(R.id.list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        on(widgets1.length);
                    }
                });
            }
        }).start();
    }


    public void on(int length) {
        a.setAdapter(new adapter(s, length));
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.backSave).setChecked(getSharedPreferences("data", 0).getBoolean("backSave", false));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.backSave) {
            getSharedPreferences("data", 0).edit().putBoolean("backSave", !item.isChecked()).apply();
            item.setChecked(!item.isChecked());
            return true;
        } else {
            int[] widgets1 = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, TextWidget.class));
            int[] widgets2 = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, SyncWidget.class));
            s = new int[widgets1.length + widgets2.length];
            System.arraycopy(widgets1, 0, s, 0, widgets1.length);
            System.arraycopy(widgets2, 0, s, widgets1.length, widgets2.length);
            if (s.length > 0) {
                if (widgets1.length > 0)
                    sendBroadcast(new Intent(this, TextWidget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", widgets1));
                if (widgets2.length > 0)
                    sendBroadcast(new Intent(this, SyncWidget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", widgets2));
                Toast.makeText(this, "成功刷新了" + s.length + "个小部件!", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "未找到小部件", Toast.LENGTH_SHORT).show();
            on(widgets1.length);
        }
        return super.onMenuItemSelected(i, item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        on(AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, TextWidget.class)).length);
    }
}