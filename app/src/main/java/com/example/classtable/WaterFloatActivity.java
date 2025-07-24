package com.example.classtable;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class WaterFloatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_water_float);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences firstItem = this.getSharedPreferences("firstItem", this.MODE_PRIVATE);
        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            SharedPreferences.Editor editor = firstItem.edit();
            TextView textView = findViewById(R.id.etNumber); // 获取输入框的文本并转换为 String
            String input = textView.getText().toString();
            if (!input.isEmpty()) { // 检查输入是否为空
                int min = Integer.parseInt(input); // 将 String 转换为 int
                if(min==0)
                {
                    Toast.makeText(this, "0分钟，想啥呢，老哔登", Toast.LENGTH_SHORT).show();
                    return;
                }
                //取消之前的
                Intent intent = new Intent(this, NotifyReceiver.class); // AlarmReceiver 是你的广播接收器
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);

                editor.putString("everyTime", min + " min");
                SwitchMaterial sound = findViewById(R.id.switchSound);
                SwitchMaterial vibrate = findViewById(R.id.switchVibrate);
                editor.putInt("sound", sound.isChecked() ? 1 : 0);
                editor.putInt("vibrate", vibrate.isChecked() ? 1 : 0);
                editor.apply();
                finish();
            } else {
                Toast.makeText(this, "apple U,输入不能空着撒", Toast.LENGTH_SHORT).show();
            }

        });
    }
}