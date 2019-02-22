package com.hew.second.gathering;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hew.second.gathering.activities.MainActivity;

import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService {

    private final static String TAG = MyFcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage message){ // FCMメッセージを受信したときに呼び出される
        String from = message.getFrom();
        Map data = message.getData();

        Log.d(TAG, "from:" + from);
        Log.d(TAG, "data:" + data.toString());

        String msg = data.get("data").toString();
        sendNotification(msg);
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Push通知のタイトル")
                .setSubText("Push通知のサブタイトル")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 , notificationBuilder.build());
    }
}
