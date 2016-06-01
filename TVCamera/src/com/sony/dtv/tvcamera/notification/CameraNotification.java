package com.sony.dtv.tvcamera.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.EnjoyActivity;

public class CameraNotification {
    public static final int CAMERA_NOTIFY_MIRROR = 1;
    private NotificationManager mNotificationManager;
    private static Context mContext;

    public CameraNotification(Context context) {
        mContext = context;
    }

    public Notification buildRecommendationPicture(int type) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) (mContext.getSystemService(Context.NOTIFICATION_SERVICE));
        }
        if (type == CAMERA_NOTIFY_MIRROR) {
            final int mirror_id = (int) System.currentTimeMillis();
            Notification notification = new NotificationCompat.BigPictureStyle(
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(mContext.getString(R.string.content_title))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setOngoing(true)
                            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.home_recommend_ecapps))
                            .setSmallIcon(R.drawable.home_recommend_clear_small_icon)
                            .setContentText(mContext.getString(R.string.card_description))
                            .setContentInfo(mContext.getString(R.string.content_title))
                            .setColor(mContext.getResources().getColor(R.color.recommendation_card))
                            .setContentIntent(buildPendingIntent((long) mirror_id, type)))
                    .build();
            notification.category = notification.CATEGORY_RECOMMENDATION;
            mNotificationManager.notify(mirror_id, notification);
            return notification;
        }
        return null;
    }

    public void cancelRecommendation(){
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)(mContext.getSystemService(Context.NOTIFICATION_SERVICE));
        }
        mNotificationManager.cancelAll();
    }

    private PendingIntent buildPendingIntent(long id, int type) {
        Intent detailsIntent = null;
        PendingIntent intent = null;
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        if(type == CAMERA_NOTIFY_MIRROR) {
            detailsIntent = new Intent(mContext, EnjoyActivity.class);
            stackBuilder.addParentStack(EnjoyActivity.class);
            detailsIntent.putExtra("id", id);
            stackBuilder.addNextIntent(detailsIntent);
            detailsIntent.setAction(Long.toString(id));

            intent = stackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return intent;
    }
}

