package com.example.classtable;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.classtable.custom.ShareUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MyPagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private String[] titles = new String[]{
            "照片",
            "课表",
            "提醒"};
    private int[] icons = new int[]{
            R.drawable.pic,
            R.drawable.course,
            R.drawable.alert};
    private List<View> tabviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Boolean is_first = ShareUtil.getInstance(this).read("first", true);
        if(is_first)
        {
            SharedPreferences firstItem = this.getSharedPreferences("firstItem", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = firstItem.edit();
            editor.putInt("switch", 0);
            editor.putInt("sound",1);
            editor.putInt("vibrate",1);
            editor.putString("everyTime","20 min");
            editor.apply();
            ShareUtil.getInstance(this).write("first",false);
        }
        tabLayout = findViewById(R.id.ac_navigation_tab);

        for (int i = 0; i < 3; i++) {
            View v = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
            ImageView img = v.findViewById(R.id.tv_img);
            TextView tv = v.findViewById(R.id.tab_tv);
            tv.setText(titles[i]);
            img.setBackgroundResource(icons[i]);
            tabLayout.addTab(tabLayout.newTab().setCustomView(v));
            tabviews.add(v);
        }
        // 初始化 ViewPager2
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        // 设置默认显示的 Fragment（位置为 1，即 MainFragment）
        viewPager.setCurrentItem(1, false);

        // 使用 TabLayoutMediator 自动创建 Tab 并设置自定义视图
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setCustomView(tabviews.get(position)));
        mediator.attach();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(this.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // 没有权限，请求权限
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.VIBRATE},
                    1);
        }
        ChannelUtil.Creat_Channel(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
