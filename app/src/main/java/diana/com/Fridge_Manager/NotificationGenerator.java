package diana.com.Fridge_Manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import diana.com.Fridge_Manager.FridgeManagerActivity;
import diana.com.R;

public class NotificationGenerator {
    private static final int NOTIFICATION_ID_OPEN_ACTIVITY=9;
    public static void openActivityNotification(Context  context){
        NotificationCompat.Builder nc=new NotificationCompat.Builder(context);
        NotificationManager nm= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent= new Intent(context, FridgeManagerActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =PendingIntent.getActivity(context,0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        nc.setContentIntent(pendingIntent);
        nc.setSmallIcon(R.mipmap.ic_launcher);
        nc.setAutoCancel(true);
        nc.setContentTitle("Your produs has expired");
        nc.setContentText("Go to your fridge and delete it");
        nm.notify(NOTIFICATION_ID_OPEN_ACTIVITY,nc.build());
    }
}
