package com.textwidget;

import android.app.ActionBar;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    static class adapter extends BaseAdapter {
        private final int[] data;
        private final Context mContext;

        public adapter(Context mContext, int[] data) {
            super();
            this.mContext = mContext;
            this.data = data;
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
            LayoutInflater inflater = LayoutInflater.from(mContext);
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
            SharedPreferences b = mContext.getSharedPreferences(String.valueOf(data[position]), 0);
            holder.imageButton.setImageResource(b.getBoolean("lock", false) ? R.drawable.lock : R.drawable.unlock);
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    b.edit().putBoolean("lock", !b.getBoolean("lock", false)).apply();
                    holder.imageButton.setImageResource(b.getBoolean("lock", false) ? R.drawable.lock : R.drawable.unlock);
                    mContext.sendBroadcast(new Intent(mContext, Widget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", new int[]{data[position]}));
                }
            });
            SpannableString ss = new SpannableString(Html.fromHtml(b.getString("content", "\n").replace("\n", "<br>")));
            ss.setSpan(new TypefaceSpan(b.getString("fontFamily","sans-serif")),0,ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int textShadowId;
            switch (b.getInt("shadowType",0)) {
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
                ss.setSpan(new TextAppearanceSpan(mContext, textShadowId), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                    mContext.startActivity(new Intent(mContext, SettingActivity.class).putExtra("appWidgetId", data[position]));
                }
            });
            return convertView;
        }

        static class ViewHolder {
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
        s = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, Widget.class));
        a = findViewById(R.id.list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        on();
                    }
                });
            }
        }).start();
    }


    public void on() {
        a.setAdapter(new adapter(this, s));
        a.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.backSave).setChecked(getSharedPreferences("data",0).getBoolean("backSave", false));
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
            getSharedPreferences("data",0).edit().putBoolean("backSave", !item.isChecked()).apply();
            item.setChecked(!item.isChecked());
            return true;
        } else {
            s = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, Widget.class));
            if (s.length > 0) {
                sendBroadcast(new Intent(this, Widget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", s));
                Toast.makeText(this, "成功刷新了" + s.length + "个小部件!", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "未找到小部件", Toast.LENGTH_SHORT).show();
            on();
        }
        return super.onMenuItemSelected(i, item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        on();
    }
}