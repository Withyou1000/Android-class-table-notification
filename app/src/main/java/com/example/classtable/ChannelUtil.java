package com.example.classtable;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.example.classtable.bean.Notify;

public class ChannelUtil {
    @SuppressLint("NewApi")
    public static void Creat_Channel(Context mContext)
    {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    /* *//*   Uri soundUri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.weixin);*//*
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {        //Android 8.0适配
            //信息通知渠道
            NotificationChannel channel = new NotificationChannel("jjc",
                    "提醒",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            channel.setDescription("点我设置通知方式");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);

            //前台显示渠道
            NotificationChannel fore_channel = new NotificationChannel(
                    "前台显示渠道",
                    "前台服务",
                    NotificationManager.IMPORTANCE_LOW);
            fore_channel.setDescription("用于显示前台服务的通知");
            notificationManager.createNotificationChannel(fore_channel);
        }
    }
   /* public static void delete_channel(Context context,String id)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 调用 deleteNotificationChannel 方法删除指定 ID 的通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(id);
        }
    }*/
}
