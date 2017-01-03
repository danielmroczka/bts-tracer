package com.labs.dm.bts;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class BtsService extends Service {

    private Notification notification;

    public BtsService() {

    }

    @Override
    public void onStart(Intent i, int startId) {
        super.onStart(i, startId);
        notification = buildNotify("Start service");
        startForeground(123, notification);
    }

    private Notification buildNotify(String contentText) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent .setAction(Intent.ACTION_MAIN);
        intent .addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setTicker("bts")
                .setOngoing(true)
                //.setContentInfo("aaaa")
                .setContentText(contentText)
                .setContentTitle("BTS Tracer")
                .setSmallIcon(R.drawable.ic_app)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);
        //.setStyle(new Notification.BigTextStyle().bigText("bts").setBigContentTitle(getText(R.string.app_name)));
        notification = builder.build();
        return  notification;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
