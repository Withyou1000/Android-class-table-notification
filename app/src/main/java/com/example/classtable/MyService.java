package com.example.classtable;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.classtable.bean.Notify;
import com.example.classtable.sqlite.NotifyDBHelper;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import cn.jpush.android.service.AlarmReceiver;
import cn.jpush.android.service.JCommonService;

public class MyService extends Service {
    private AlarmManager am;
    private NotifyDBHelper notifyDBHelper;
    private Notification notification;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建通知
        notification = new NotificationCompat.Builder(this, "前台显示渠道")
                .setSmallIcon(R.mipmap.table)
                .setContentTitle("正在为您效劳...")
                .setContentText("莫要遣退臣下")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);*/
        //处理喝水提醒
        am = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        SharedPreferences firstItem = this.getSharedPreferences("firstItem", Context.MODE_PRIVATE);
        /*  if (firstItem.getInt("switch", 0) == 1) {*/
        String s = firstItem.getString("everyTime", "20 min");
        String result = s.replaceAll("\\D+", ""); // 去除非数字字符
        int minutes = Integer.parseInt(result);
        long intervalMillis = minutes * 60 * 1000; // 将分钟转换为毫秒
       /* // 启动前台服务
        startForeground(1, notification);*/

        Intent intent2 = new Intent(getApplicationContext(), NotifyReceiver.class);
        intent2.setAction("jjc.notify.first");
        intent2.putExtra("sound", firstItem.getInt("sound", 0));
        intent2.putExtra("vibrate", firstItem.getInt("vibrate", 0));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, -1, intent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT < 19) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, 0, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            am.setExact(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + intervalMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + intervalMillis, pendingIntent);
        }

        //通知提醒
        notifyDBHelper = NotifyDBHelper.getInstance(this);
        notifyDBHelper.openRead();
        List<Notify> notifyList = notifyDBHelper.queryall();
        int flag = 0;
        if (notifyList.size() != 0) {
            for (Notify item : notifyList) {
                initRemind(Integer.parseInt(item.getTime().split(":")[0]),
                        Integer.parseInt(item.getTime().split(":")[1]), item);
                if (item.getSwitch_on() == 1) {
                    flag = 1;
                }
            }
        }
        if (flag==1 || firstItem.getInt("switch", 0) == 1) {
            // 启动前台服务
            startForeground(1, notification);
        }

        return START_STICKY;
    }

    @SuppressLint("ScheduleExactAlarm")
    private void initRemind(int hour, int minute, Notify item) {
        Intent intent = new Intent(getApplicationContext(), NotifyReceiver.class);
        intent.setAction("jjc.notify");
       /* intent.setComponent(new ComponentName("com.example.classtable", "com.example.classtable.NotifyReceiver"));
        intent.setClassName("com.example.classtable", "com.example.classtable.NotifyReceiver");*/
        intent.putExtra("id", item.getId());
        PendingIntent pi = PendingIntent.getBroadcast(this, item.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long systemTime = System.currentTimeMillis();//java.lang.System.currentTimeMillis()，它返回从 UTC 1970 年 1 月 1 日午夜开始经过的毫秒数。
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); //  这里时区需要设置一下，不然会有8个小时的时间差
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);//
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);            //选择的定时时间
        long selectTime = calendar.getTimeInMillis();    //计算出设定的时间
        //  如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }// 进行闹铃注册
        if (Build.VERSION.SDK_INT < 19) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, selectTime, 0, pi);
        } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            am.setExact(AlarmManager.RTC_WAKEUP,
                    selectTime, pi);
        } else if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    selectTime, pi);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}