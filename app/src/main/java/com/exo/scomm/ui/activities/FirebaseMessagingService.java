package com.exo.scomm.ui.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.exo.scomm.R;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();

        String message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String channelId = remoteMessage.getNotification().getChannelId();
        String fromUserId = remoteMessage.getData().get("user_id");
        String username = Objects.requireNonNull(remoteMessage.getData()).get("username");

        assert fromUserId != null;
        Log.e("UID", fromUserId);
        assert username != null;
//        Log.e("UID", username);


        assert channelId != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent =  new Intent(click_action);

        resultIntent.putExtra("uid", fromUserId);
        resultIntent.putExtra("username", username);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

}