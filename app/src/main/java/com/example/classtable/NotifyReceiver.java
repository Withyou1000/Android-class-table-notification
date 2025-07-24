package com.example.classtable;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
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


import androidx.core.app.NotificationCompat;

import com.example.classtable.bean.Notify;
import com.example.classtable.sqlite.NotifyDBHelper;

public class NotifyReceiver extends BroadcastReceiver {
    private Context mContext;
    private NotifyDBHelper dbHelper;
    private Notify item;

    public NotifyReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        dbHelper = NotifyDBHelper.getInstance(context);
        dbHelper.openRead();
        this.mContext = context;
      /*  PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // 创建唤醒锁
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "AlarmReceiver:WakeLock");
        // 锁定唤醒锁
        wakeLock.acquire(10 * 1000); // 锁定 10 秒
        //重启后继续保持
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MyService.class));
            }
        }*/
        item = dbHelper.queryOne(intent.getIntExtra("id", -1));
        //可能长按删除了
        if (item != null) {
            if (intent != null && intent.getAction().equals("jjc.notify") && item.getSwitch_on() == 1) {
                ShowNotify();
                if (item.getIfSound() == 1) {
                    play_Sound();
                }
                if (item.getIfVibrate() == 1) {
                    Vibrate();
                }
            }
        }
        SharedPreferences firstItem = mContext.getSharedPreferences("firstItem", Context.MODE_PRIVATE);
        if (intent != null && intent.getAction().equals("jjc.notify.first") && firstItem.getInt("switch", 0) == 1) {
            NotificationCompat.Builder builder;
            builder = new NotificationCompat.Builder(mContext, "jjc");
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_MAX) // 关键：设置通知优先级
                    .setContentTitle("通知") //指定通知栏的标题内容
                    .setContentText("记得停下来喝口水，给自己充充电！\uD83C\uDF31\uD83D\uDCA7")//通知的正文内容
                    .setColor(Color.rgb(255, 0, 0))
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(1)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCategory(NotificationCompat.CATEGORY_ALARM) // 推荐：设置为需要立即响应的类型（如 CALL
                    /*     .setFullScreenIntent(pendingIntent, true)// 强制弹窗*/
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.loveyou))
                    .setSmallIcon(R.mipmap.table);//通知显示的小图标，只能用alpha图层的图片进行设置
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            Notification notification = builder.build();
            notificationManager.notify(101, notification);
            ;

            if (intent.getIntExtra("sound", 0) == 1) {
                play_Sound();
            }
            if (intent.getIntExtra("vibrate", 0) == 1) {
                Vibrate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, MyService.class));
        }
    }

    private void Vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
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
     /*   Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT
        );*/
        /* Uri soundUri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.weixin);*/
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(mContext, "jjc");
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX) // 关键：设置通知优先级
                .setContentTitle("通知") //指定通知栏的标题内容
                .setContentText("记得" + item.getInfo())//通知的正文内容
                .setColor(Color.rgb(255, 0, 0))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(1)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM) // 推荐：设置为需要立即响应的类型（如 CALL
                /*     .setFullScreenIntent(pendingIntent, true)// 强制弹窗*/
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.loveyou))
                .setSmallIcon(R.mipmap.table);//通知显示的小图标，只能用alpha图层的图片进行设置
        NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
        Notification notification = builder.build();
        notificationManager.notify(101, notification);
    }

    private void play_Sound() {
        // 获取音频管理器
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);

// 创建媒体播放器并设置流类型
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.weixin);
        if (mediaPlayer == null) {
            Log.e("MediaPlayer", "Failed to create MediaPlayer");
            return;
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

// 开始播放
        mediaPlayer.start();

// 释放资源监听
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            Log.d("MediaPlayer", "MediaPlayer released");
        });
    }
}