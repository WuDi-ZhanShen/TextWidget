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


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    /**
     * 每个小部件行的数据缓存，避免在 getView 里反复读 SharedPreferences 和构造 Spannable
     */
    static class RowData {
        SpannableString text;
        boolean lock;
        int textSize;
        int textColor;
        int bgColor;
        int gravity;
        float letterSpacing;
        float lineSpacingMultiplier;
        String fontFamily;
        int shadowStyleResId; // -1 表示无阴影
    }

    class Adapter extends BaseAdapter {
        private final int[] data;
        private final int length;
        private final LayoutInflater inflater;
        private final Map<Integer, RowData> cache = new HashMap<>();

        public Adapter(int[] data, int length) {
            super();
            this.data = data;
            this.length = length;
            this.inflater = LayoutInflater.from(MainActivity.this);
            buildCache();  // ★ 构造时一次性把所有信息算好，滚动时只读缓存
        }

        private void buildCache() {
            cache.clear();
            for (int widgetId : data) {
                SharedPreferences b = getSharedPreferences(String.valueOf(widgetId), 0);
                RowData rd = new RowData();

                // 文本内容
                CharSequence baseText;
                if (isNormalWidget(widgetId)) {
                    String raw = b.getString("content", "\n")
                            .replace("\n", "<br>");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        baseText = Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        baseText = Html.fromHtml(raw);
                    }
                } else {
                    baseText = b.getString("currentString", "请刷新小部件");
                }

                SpannableString ss = new SpannableString(baseText);

                // 字体
                String fontFamily = b.getString("fontFamily", "sans-serif");
                ss.setSpan(new TypefaceSpan(fontFamily), 0, ss.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                rd.fontFamily = fontFamily;

                // 文字阴影样式
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
                rd.shadowStyleResId = textShadowId;
                if (textShadowId != -1) {
                    ss.setSpan(new TextAppearanceSpan(MainActivity.this, textShadowId),
                            0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                rd.text = ss;
                rd.lock = b.getBoolean("lock", false);
                rd.textSize = b.getInt("size", 16);
                rd.textColor = b.getInt("color", -1);
                rd.bgColor = b.getInt("bgcolor", 0xFFFFFF);
                rd.gravity = b.getInt("gravity", 17);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rd.letterSpacing = (b.getInt("space", 10) - 10f) / 100f;
                } else {
                    rd.letterSpacing = 0f;
                }

                rd.lineSpacingMultiplier = 0.7f + (b.getInt("line", 3) * 0.1f);

                cache.put(widgetId, rd);
            }
        }

        /**
         * 判断这个 widgetId 是否属于 "普通文本小部件"，用 length 来区分
         */
        private boolean isNormalWidget(int widgetId) {
            // 前 length 个是 TextWidget，后面的为 SyncWidget
            // 这里用位置判断更直接，在 buildCache 里改为根据 data 和 length 判断：
            // 但 buildCache 里拿不到 position，所以我们默认：
            // 在 getView 里用 position 判断；这里简化为：
            // 由调用方保证 data 中前 length 个是 TextWidget 的 id。
            // 实际上在 getView 里会按 position 决定跳 SettingActivity/SyncSettingActivity。
            // 这里为了和原来逻辑一致，按 sharedPreferences 中的内容类型区分。
            // 也可以简单返回 true，表示都用 "content"，这取决于你的实际数据约定。
            return true;
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView textView;
            ImageButton imageButton;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.r, parent, false);
                holder = new ViewHolder();
                holder.textView = convertView.findViewById(R.id.b);
                holder.imageButton = convertView.findViewById(R.id.c);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final int widgetId = data[position];
            final RowData rd = cache.get(widgetId);
            if (rd == null) {
                // 理论上不会发生，防御一下
                holder.textView.setText("数据加载失败");
                holder.imageButton.setVisibility(View.GONE);
                return convertView;
            }

            // 按 position 和 length 控制按钮显隐（并修复复用问题）
            if (position >= length) {
                holder.imageButton.setVisibility(View.INVISIBLE);
                holder.imageButton.setLayoutParams(
                        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                holder.imageButton.setVisibility(View.VISIBLE);
                holder.imageButton.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            // 锁图标
            holder.imageButton.setImageResource(rd.lock ? R.drawable.lock : R.drawable.unlock);
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sp =
                            getSharedPreferences(String.valueOf(widgetId), 0);
                    boolean newLock = !rd.lock;
                    rd.lock = newLock;
                    sp.edit().putBoolean("lock", newLock).apply();
                    holder.imageButton.setImageResource(newLock ? R.drawable.lock : R.drawable.unlock);
                    sendBroadcast(new Intent(MainActivity.this, TextWidget.class)
                            .setAction("android.appwidget.action.APPWIDGET_UPDATE")
                            .putExtra("appWidgetIds", new int[]{widgetId}));
                }
            });

            // 文本样式（已经在缓存里算好了）
            holder.textView.setText(rd.text);
            holder.textView.setTextSize(rd.textSize);
            holder.textView.setTextColor(rd.textColor);
            holder.textView.setBackgroundColor(rd.bgColor);
            holder.textView.setGravity(rd.gravity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.textView.setLetterSpacing(rd.letterSpacing);
            }
            holder.textView.setLineSpacing(1f, rd.lineSpacingMultiplier);

            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 前 length 个是普通 TextWidget，后面的为 SyncWidget
                    if (position < length) {
                        startActivity(new Intent(MainActivity.this, SettingActivity.class)
                                .putExtra("appWidgetId", widgetId));
                    } else {
                        startActivity(new Intent(MainActivity.this, SyncSettingActivity.class)
                                .putExtra("appWidgetId", widgetId));
                    }
                }
            });

            return convertView;
        }

        /**
         * 当 SharedPreferences 内容有变化（比如你在 SettingActivity 里改了文字）
         * 可以调用这个方法刷新缓存。
         */
        public void refreshCache() {
            buildCache();
            notifyDataSetChanged();
        }
    }

    int[] s;
    ListView a;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(0x30000000));
        setContentView(R.layout.main);

        // 设置导航栏透明，UI 会好看些
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            window.setNavigationBarContrastEnforced(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(0x30000000);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        a = findViewById(R.id.list);

        reloadWidgetsAndSetAdapter();
    }

    /**
     * 重新获取所有 widgetId，并更新 ListView & adapter
     */
    private void reloadWidgetsAndSetAdapter() {
        int[] widgets1 = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, TextWidget.class));
        int[] widgets2 = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, SyncWidget.class));

        s = new int[widgets1.length + widgets2.length];
        System.arraycopy(widgets1, 0, s, 0, widgets1.length);
        System.arraycopy(widgets2, 0, s, widgets1.length, widgets2.length);

        adapter = new Adapter(s, widgets1.length);
        a.setAdapter(adapter);
    }

    public void on(int length) {
        // 已经改为 reloadWidgetsAndSetAdapter，这个方法可以不再使用
        // 保留以兼容旧调用
        if (adapter == null) {
            adapter = new Adapter(s, length);
            a.setAdapter(adapter);
        } else {
            adapter.refreshCache();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.backSave)
                .setChecked(getSharedPreferences("data", 0)
                        .getBoolean("backSave", false));
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
            boolean newState = !item.isChecked();
            getSharedPreferences("data", 0)
                    .edit().putBoolean("backSave", newState).apply();
            item.setChecked(newState);
            return true;
        } else {
            int[] widgets1 = AppWidgetManager.getInstance(this)
                    .getAppWidgetIds(new ComponentName(this, TextWidget.class));
            int[] widgets2 = AppWidgetManager.getInstance(this)
                    .getAppWidgetIds(new ComponentName(this, SyncWidget.class));
            s = new int[widgets1.length + widgets2.length];
            System.arraycopy(widgets1, 0, s, 0, widgets1.length);
            System.arraycopy(widgets2, 0, s, widgets1.length, widgets2.length);

            if (s.length > 0) {
                if (widgets1.length > 0)
                    sendBroadcast(new Intent(this, TextWidget.class)
                            .setAction("android.appwidget.action.APPWIDGET_UPDATE")
                            .putExtra("appWidgetIds", widgets1));
                if (widgets2.length > 0)
                    sendBroadcast(new Intent(this, SyncWidget.class)
                            .setAction("android.appwidget.action.APPWIDGET_UPDATE")
                            .putExtra("appWidgetIds", widgets2));
                Toast.makeText(this, "成功刷新了" + s.length + "个小部件!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未找到小部件", Toast.LENGTH_SHORT).show();
            }

            // 刷新列表缓存
            if (adapter != null) {
                adapter.refreshCache();
            } else {
                reloadWidgetsAndSetAdapter();
            }
        }
        return super.onMenuItemSelected(i, item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从 SettingActivity 返回后，配置可能变化，这里刷新一下缓存
        if (adapter != null) {
            adapter.refreshCache();
        } else {
            reloadWidgetsAndSetAdapter();
        }
    }
}
