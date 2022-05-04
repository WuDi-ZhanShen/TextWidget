package com.textwidget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class adapter extends BaseAdapter {
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
        SharedPreferences b = mContext.getSharedPreferences(String.valueOf(data[position]),0);
        holder.imageButton.setImageResource(b.getBoolean("lock", false) ? R.drawable.lock : R.drawable.unlock);
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.edit().putBoolean("lock",!b.getBoolean("lock",false)).apply();
                holder.imageButton.setImageResource(b.getBoolean("lock", false) ? R.drawable.lock : R.drawable.unlock);
                mContext.sendBroadcast(new Intent(mContext, Widget.class).setAction("android.appwidget.action.APPWIDGET_UPDATE").putExtra("appWidgetIds", new int[]{data[position]}));
            }
        });
        holder.textView.setText(Html.fromHtml(b.getString("content","\n").replace("\n", "<br>")));
        holder.textView.setTextSize(b.getInt("size",16));
        holder.textView.setTextColor(b.getInt("color",-1));
        holder.textView.setBackgroundColor(b.getInt("bgcolor",16777215));
        holder.textView.setGravity(b.getInt("gravity",17));
        if (Build.VERSION.SDK_INT >= 21)
            holder.textView.setLetterSpacing((b.getInt("space",10)-10f)/100);
        holder.textView.setLineSpacing(1f,0.7f+(b.getInt("line",3)*0.1f));
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

