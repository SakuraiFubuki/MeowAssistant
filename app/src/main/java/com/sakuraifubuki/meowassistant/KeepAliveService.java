package com.sakuraifubuki.meowassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

public class KeepAliveService extends Service {
    public static final String ACTION_STOP_KEEP_ALIVE = "com.sakuraifubuki.meowassistant.STOP_KEEP_ALIVE";
    private static final String CHANNEL_ID = "keep_alive_channel";
    private static final int NOTIFY_ID = 1001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_KEEP_ALIVE.equals(intent.getAction())) {
            try {
                CatConfig cfg = CatConfig.load(this);
                cfg.enableKeepAlive = false;
                cfg.save(this);
            } catch (Exception e) {
            }
            if (Build.VERSION.SDK_INT >= 24) {
                stopForeground(true);
            } else {
                stopForeground(NOTIFY_ID);
            }
            stopSelf();
            return START_NOT_STICKY;
        }
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFY_ID, buildNotification(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFY_ID, buildNotification(this));
        }
        return START_STICKY;
    }

    private static Notification buildNotification(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "喵喵助手保活", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
        Intent open = new Intent(ctx, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(ctx, CHANNEL_ID) : new Notification.Builder(ctx);
        Intent stop = new Intent(ctx, KeepAliveService.class);
        stop.setAction(ACTION_STOP_KEEP_ALIVE);
        PendingIntent stopPi = PendingIntent.getService(ctx, 1, stop, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        b.setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("喵喵助手运行中")
                .setContentText("全局文本改写保活已开启")
                .setContentIntent(pi)
                .addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "关闭保活", stopPi).build())
                .setOngoing(true);
        return b.build();
    }

    public static void start(Context ctx) {
        Intent i = new Intent(ctx, KeepAliveService.class);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.startForegroundService(i);
            } else {
                ctx.startService(i);
            }
        } catch (Exception e) {
        }
    }

    public static void stop(Context ctx) {
        try {
            ctx.stopService(new Intent(ctx, KeepAliveService.class));
        } catch (Exception e) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
