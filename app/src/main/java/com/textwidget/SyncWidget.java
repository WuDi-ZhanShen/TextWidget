package com.textwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class SyncWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences sp = context.getSharedPreferences(String.valueOf(appWidgetId), 0);
            RemoteViews rv;
            switch (sp.getInt("line", 3)) {
                case 0:
                    rv = new RemoteViews(context.getPackageName(), R.layout.widgea);
                    break;
                case 1:
                    rv = new RemoteViews(context.getPackageName(), R.layout.widgeb);
                    break;
                case 2:
                    rv = new RemoteViews(context.getPackageName(), R.layout.widgec);
                    break;
                default:
                    rv = new RemoteViews(context.getPackageName(), R.layout.widget);
                    break;
            }
            int[] r = {R.id.topleft, R.id.topcenter, R.id.topright, R.id.centerleft, R.id.centercenter, R.id.centerright, R.id.bottomleft, R.id.bottomcenter, R.id.bottomright};
            for (Integer i : r) {
                rv.setViewVisibility(i, View.GONE);
            }
            int s;
            switch (sp.getInt("gravity", 17)) {
                case 51:
                    s = R.id.topleft;
                    break;
                case 49:
                    s = R.id.topcenter;
                    break;
                case 53:
                    s = R.id.topright;
                    break;
                case 19:
                    s = R.id.centerleft;
                    break;
                case 21:
                    s = R.id.centerright;
                    break;
                case 83:
                    s = R.id.bottomleft;
                    break;
                case 81:
                    s = R.id.bottomcenter;
                    break;
                case 85:
                    s = R.id.bottomright;
                    break;
                default:
                    s = R.id.centercenter;
                    break;
            }
            rv.setViewVisibility(s, View.VISIBLE);
            rv.setFloat(s, "setTextSize", (float) sp.getInt("size", 16));
            rv.setInt(s, "setTextColor", sp.getInt("color", -1));
            rv.setInt(s, "setBackgroundColor", sp.getInt("bgcolor", 16777215));
            if (Build.VERSION.SDK_INT >= 21)
                rv.setFloat(s, "setLetterSpacing", (sp.getInt("space", 10) - 10f) / 100);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(sp.getString("url", "https://v1.jinrishici.com/rensheng.txt"));
                        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                        httpsURLConnection.setReadTimeout(5000);
                        httpsURLConnection.setRequestMethod("GET");

                        StringBuilder text = new StringBuilder();
                        if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream = httpsURLConnection.getInputStream();
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                            InputStreamReader isr;

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                isr = new InputStreamReader(bufferedInputStream);
                            } else
                                isr = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
                            BufferedReader reader = new BufferedReader(isr);
                            String str = reader.readLine();
                            if (str != null) text.append(str);
                            while ((str = reader.readLine()) != null) {
                                text.append("\n").append(str);
                            }
                            sp.edit().putString("currentString", String.valueOf(text)).apply();
                            reader.close();
                            inputStream.close();
                            bufferedInputStream.close();
                            isr.close();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }).start();
            SpannableString ss = new SpannableString(sp.getString("currentString", "请刷新小部件"));
            ss.setSpan(new TypefaceSpan(sp.getString("fontFamily", "sans-serif")), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int textShadowId;
            switch (sp.getInt("shadowType", 0)) {
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
                ss.setSpan(new TextAppearanceSpan(context, textShadowId), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            rv.setTextViewText(s, ss);
            rv.setOnClickPendingIntent(s, PendingIntent.getBroadcast(context, new Random().nextInt(), new Intent(context, SyncWidget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", new int[]{appWidgetId}), PendingIntent.FLAG_IMMUTABLE));
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            context.getSharedPreferences(String.valueOf(appWidgetId), 0).edit().clear().apply();
        }
        Toast.makeText(context, "已删除文字缓存", Toast.LENGTH_SHORT).show();
    }
}
