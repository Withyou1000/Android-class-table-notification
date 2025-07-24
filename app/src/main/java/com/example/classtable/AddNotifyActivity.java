package com.example.classtable;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.classtable.bean.Notify;
import com.example.classtable.custom.CalTime;
import com.example.classtable.custom.PickerView;
import com.example.classtable.sqlite.NotifyDBHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddNotifyActivity extends AppCompatActivity {

    private PickerView hour_pv;
    private PickerView minute_pv;
    private Calendar mCalendar;
    private int mHour, mMinuts;
    private String timeDitance;
    private int minute, hour;
    private TextView distance;
    private ConstraintLayout soundChoose;
    private TextView soundType;
    private ConstraintLayout vibrateChoose;
    private TextView ifvibrat;
    private ImageButton addYes, addCancel;
    private NotifyDBHelper myDatabaseHelper;
    private EditText et_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notify); // 加载布局

        // 初始化视图
        hour_pv = findViewById(R.id.hour_pv);
        minute_pv = findViewById(R.id.minute_pv);
        distance = findViewById(R.id.distance);
        et_info = findViewById(R.id.et_info);
        soundChoose = findViewById(R.id.SoundChoose);
        soundType = findViewById(R.id.soundContent);
        vibrateChoose = findViewById(R.id.vibrateChoose);
        ifvibrat = findViewById(R.id.vibrateContent);
        addYes = findViewById(R.id.addYes);
        addCancel = findViewById(R.id.addCancel);

        // 初始化数据
        List<String> hours = new ArrayList<>();
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(i < 10 ? "0" + i : "" + i);
        }
        for (int i = 0; i < 60; i++) {
            minutes.add(i < 10 ? "0" + i : "" + i);
        }

        minute=30;
        hour=12;
        timeDitance=new CalTime().cal(minute,hour);
        distance.setText(timeDitance);
        hour_pv.setData(hours);
        minute_pv.setData(minutes);

        hour_pv.setOnSelectListener(text -> {
            hour = Integer.parseInt(text);
            timeDitance = new CalTime().cal(hour, minute);
            distance.setText(timeDitance);
        });

        minute_pv.setOnSelectListener(text -> {
            minute = Integer.parseInt(text);
            timeDitance = new CalTime().cal(hour, minute);
            distance.setText(timeDitance);
        });

        soundChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{"是", "否"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddNotifyActivity.this);
                builder.setTitle("是否开启提示音");

                // 添加列表项
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        soundType.setText(items[i]);
                    }
                });
                // 创建并显示
                builder.create().show();
            }
        });

        vibrateChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{"是", "否"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddNotifyActivity.this);
                builder.setTitle("是否开启振动");

                // 添加列表项
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ifvibrat.setText(items[i]);
                    }
                });
                // 创建并显示
                builder.create().show();
            }
        });

        myDatabaseHelper = new NotifyDBHelper(this);
        addYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_info.getText().toString().equals("")) {
                    Toast.makeText(AddNotifyActivity.this, "你小子，是不是忘了什么", Toast.LENGTH_SHORT).show();
                } else {
                    Notify notify = new Notify();
                    if (minute < 10) {
                        notify.setTime(hour + ":0" + minute);
                    } else {
                        notify.setTime(hour + ":" + minute);
                    }
                    notify.setCreat_time(System.currentTimeMillis() + "");
                    notify.setIfSound(soundType.getText().toString().equals("是") ? 1 : 0);
                    notify.setSwitch_on(1);
                    notify.setIfVibrate(ifvibrat.getText().toString().equals("是") ? 1 : 0);
                    notify.setInfo(et_info.getText().toString());
                    myDatabaseHelper.add_notify(notify);
                    Toast.makeText(AddNotifyActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭当前 Activity
                }
            }
        });

        addCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前 Activity
            }
        });
    }
}