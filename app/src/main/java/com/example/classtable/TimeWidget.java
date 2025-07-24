package com.example.classtable;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.classtable.bean.Notify;
import com.example.classtable.sqlite.NotifyDBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 */
// TimeWidget.java
public class TimeWidget extends AppWidgetProvider {
    public static final String ACTION_UPDATE = "com.example.UPDATE_TIME";
    private NotifyDBHelper notifyDBHelper;
    private AlarmManager am;
    private Notify notify;
    private Context mContext;
    private SharedPreferences sharedPreferences;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.e("888","enable");
        sharedPreferences = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("widget_enabled", true).apply();
       /* context.getApplicationContext().stopService(new Intent(context.getApplicationContext(), MyService.class));*/
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.e("888","disable");
        sharedPreferences = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("widget_enabled", false).apply();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 获取当前时间
      /*  Log.e("888","update");
        notifyDBHelper = NotifyDBHelper.getInstance(context.getApplicationContext());
        notifyDBHelper.openRead();
        List<Notify> notifyList = notifyDBHelper.queryall();
        if (notifyList.size() == 0) {
            // 当列表为空时，直接返回 START_NOT_STICKY 表示服务不需要重新启动
            return;
        }
        am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        for (Notify item : notifyList) {
            initRemind(Integer.parseInt(item.getTime().split(":")[0]),
                    Integer.parseInt(item.getTime().split(":")[1]), item.getId(), context);
        }*/
    }

    @SuppressLint("ScheduleExactAlarm")
    private void initRemind(int hour, int minute, int id, Context mContext) {
        Intent intent = new Intent(mContext.getApplicationContext(),TimeWidget.class);
        intent.setAction("jjc.notify");
        intent.putExtra("id", id);
        PendingIntent pi = PendingIntent.getBroadcast(mContext.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
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
            am.setRepeating(AlarmManager.RTC_WAKEUP, selectTime, am.INTERVAL_DAY, pi);
        } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            am.setExact(AlarmManager.RTC_WAKEUP,
                    selectTime, pi);
        } else if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    selectTime, pi);
        }
        Log.e("888","send");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if ("jjc.notify".equals(intent.getAction())) {
            Log.e("888","reciver");
            notifyDBHelper = NotifyDBHelper.getInstance(context.getApplicationContext());
            notifyDBHelper.openRead();
            notify = notifyDBHelper.queryOne(intent.getIntExtra("id", 0));
            this.mContext = context;
            //这里判断是否空，因为可能长按删除了
            if (notify != null) {
                if (intent != null && intent.getAction().equals("jjc.notify") && notify.getSwitch_on() == 1) {
                    ShowNotify();
                    play_Sound();
                    Vibrate();
                }
            }
            onUpdate(context, null, null);
        }
        else if(ACTION_UPDATE.equals(intent.getAction()))
        {
            onUpdate(context, null, null);
        }
    }

    private void Vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator() || notify.getIfVibrate() != 1) {
            Log.d("Vibration", "振动条件不满足");
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ 高优先级振动
                VibrationEffect effect = VibrationEffect.createOneShot(1000, 255);
                VibrationAttributes attributes = new VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_ALARM)
                        .build();
                vibrator.vibrate(effect, attributes);
                Log.d("Vibration", "Android 13+ 振动已触发（高优先级）");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0~12 标准振动
                VibrationEffect effect = VibrationEffect.createOneShot(1000, 255);
                vibrator.vibrate(effect);
                Log.d("Vibration", "Android 8.0+ 振动已触发");

            } else {
                // 旧版本兼容
                vibrator.vibrate(1000);
                Log.d("Vibration", "旧版本振动已触发");
            }
        } catch (SecurityException e) {
            Log.e("Vibration", "权限不足: " + e.getMessage());
            // 引导用户去设置页开启权限
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e("Vibration", "未知错误: " + e.getMessage());
        }
    }

    private void ShowNotify() {
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(mContext, "jjc");
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX) // 关键：设置通知优先级
                .setContentTitle("通知") //指定通知栏的标题内容
                .setContentText("记得" + notify.getInfo())//通知的正文内容
                .setColor(Color.rgb(255, 0, 0))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(12)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM) // 推荐：设置为需要立即响应的类型（如 CALL
                /*     .setFullScreenIntent(pendingIntent, true)// 强制弹窗*/
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.table))
                .setSmallIcon(R.mipmap.table);//通知显示的小图标，只能用alpha图层的图片进行设置
        NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
        Notification notification = builder.build();
        notificationManager.notify(101, notification);
    }

    private void play_Sound() {
        if (notify.getIfSound() == 0) {
            return;
        }
        // 获取音频管理器
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        // 检查音量是否足够大，如果音量太小则设置为最大音量
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        // 设置音量为最大音量的一半
        int targetVolume = maxVolume / 2;
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolume, 0);
        // 创建媒体播放器并设置音频资源
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.weixin);
        // 开始播放
        mediaPlayer.start();
        // 为媒体播放器设置完成监听器，播放完成后释放资源
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 播放完成后释放媒体播放器资源
                mp.release();
            }
        });
    }
}