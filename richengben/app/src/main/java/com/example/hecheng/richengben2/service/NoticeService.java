package com.example.hecheng.richengben2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.hecheng.richengben2.LoginActivity;
import com.example.hecheng.richengben2.R;

/**
 * 日程通知
 * Created by HeCheng on 2017/4/17.
 */

public class NoticeService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,final int startId) {
        new Thread(){
            @Override
            public void run() {
                Log.d("noticeService", "onStartCommand");
                String title = (String) intent.getSerializableExtra("title");
                String detail = (String) intent.getSerializableExtra("detail");
                String scheduleId = (String) intent.getSerializableExtra("scheduleId");
                int notificateId = scheduleId.hashCode();
                Log.d("noticeService", title);
                Log.d("noticeService", detail);
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder1 = new Notification.Builder(getApplicationContext());
                builder1.setSmallIcon(R.mipmap.app_icon); //设置图标
                builder1.setTicker("日程本");
                builder1.setContentTitle(title); //设置标题
                builder1.setContentText(detail); //消息内容
                builder1.setWhen(System.currentTimeMillis()); //通知上显示的时间
                builder1.setDefaults(Notification.DEFAULT_ALL); //设置默认的提示音，振动方式，灯光
                builder1.setAutoCancel(true);//打开程序后图标消失
                Intent intent2 = new Intent(NoticeService.this, LoginActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificateId, intent2, PendingIntent.FLAG_ONE_SHOT);
                builder1.setContentIntent(pendingIntent);
                Notification notification1 = builder1.build();
                notificationManager.notify( notificateId, notification1); // 通过通知管理器发送通知
            }
        }.start();
        return START_NOT_STICKY;
    }
}
