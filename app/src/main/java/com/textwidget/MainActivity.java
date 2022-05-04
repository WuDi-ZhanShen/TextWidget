package com.textwidget;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
    int[] s;
    ListView a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        s = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, Widget.class));
        a = getListView();
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
        a.setAdapter(new adapter(this,s));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem menuItem) {
        s = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, Widget.class));
        if (s.length>0){
            sendBroadcast(new Intent(this, Widget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", s));
            Toast.makeText(this, "成功刷新了"+s.length+"个小部件!", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "未找到小部件", Toast.LENGTH_SHORT).show();
        on();
        return super.onMenuItemSelected(i, menuItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        on();
    }
}