package com.shahzaib.toddoo.AlarmUtils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shahzaib.toddoo.DataUtils.DbContract;
import com.shahzaib.toddoo.R;
import com.shahzaib.toddoo.Services.MarkItemAsCompletedService;
import com.shahzaib.toddoo.Tasks;

import static android.content.Context.MODE_PRIVATE;

public class Tasks_AlarmService extends BroadcastReceiver{

    public static final String INTENT_KEY_NOTIFICATION_ID= "notificationID";

    public static final String KEY_ALARM_ID = "alarmID";
    public static final String KEY_ITEM_ID = "ItemID";
    private static final String NOTIFICATION_COUNT_SP = "TasksNotificationCountSP";
    private static final String TEMP_SP = "TEMP_SP";
    private static final String KEY_SP_NOTIFICATION_ID = "taskNotificationID";
    private static final String KEY_SP_TEMP = "taskNotificationID";
    final String NOTIFICATION_CHANNEL_ID = "TasksAlarmChannel";
    final String NOTIFICATION_CHANNEL_NAME = "Tasks";
    String notificationTitle = null;
    long itemID = -1;
    Context context;





    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        int alarmID = intent.getIntExtra(KEY_ALARM_ID, -1);
        Log.i("123456","Task_Alarm_ID: "+alarmID);



        String where = DbContract.Tasks.COLUMN_ALARM_ID + "=" + alarmID;
        Cursor cursor = context.getContentResolver().query(
                DbContract.Tasks.CONTENT_URI,
                null,
                where,
                null,
                null
        );
        if(cursor.moveToFirst()) {
            notificationTitle = cursor.getString(cursor.getColumnIndex(DbContract.Tasks.COLUMN_TASKS));
            itemID = cursor.getLong(cursor.getColumnIndex(DbContract.Tasks._ID));
        }



        // Build the Content of notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification);
        if (notificationTitle != null) builder.setContentTitle(notificationTitle + " " + itemID);
        builder.setContentText("Reminder");
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setColor(context.getResources().getColor(R.color.colorPrimary));
        builder.setAutoCancel(true); // when user click , it disappear
        builder.setContentIntent(contentIntent()); // open tasks list when user click on notification
        builder.addAction(R.drawable.ic_alarm_snooze, "Snooze", snoozeAlarm());
        builder.addAction(R.drawable.ic_done, "Done", taskCompleted());
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // show the Notification & also create channel for devices running android O
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel nChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(nChannel);
        }



        SharedPreferences sharedPreferences = context.getSharedPreferences(NOTIFICATION_COUNT_SP, MODE_PRIVATE);
        int NOTIFICATION_ID = sharedPreferences.getInt(KEY_SP_NOTIFICATION_ID, 0);
        sharedPreferences.edit().putInt(KEY_SP_NOTIFICATION_ID, ++NOTIFICATION_ID).apply();
        notificationManager.notify(NOTIFICATION_ID, builder.build());







            //*** after showing the notification, delete the alarm data from database
            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_ALARM_ID, 0);
            values.put(DbContract.Tasks.COLUMN_REMINDER_TIME, 0);
            values.put(DbContract.Tasks.COLUMN_NOTIFICATION_ID,NOTIFICATION_ID); // it will help to cancel notification on action click
            context.getContentResolver().update(
                    DbContract.Tasks.CONTENT_URI.buildUpon().appendPath("" + itemID).build(),
                    values,
                    null,
                    null);
            context.getContentResolver().notifyChange(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath("" + itemID).build(), null);
            Log.i("123456","Alarm is Canceled");
    }


    private PendingIntent contentIntent()
    {// open tasks list when user click on notification
        Cursor cursor = context.getContentResolver().query(
                DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(""+itemID).build(),
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst())
        {
            String itemListID = cursor.getString(cursor.getColumnIndex(DbContract.Tasks.COLUMN_LIST_ID));
            Intent intent = new Intent(context,Tasks.class);
            intent.putExtra(Tasks.KEY_ITEM_LIST_ID,itemListID);
            return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        }
        else
        {
            Log.i("123456","Notification Cursor is Empty");
            return null;
        }
    }

    private PendingIntent taskCompleted()
    {

        Intent taskDoneIntent = new Intent(context,MarkItemAsCompletedService.class);
        taskDoneIntent.putExtra(KEY_ITEM_ID,itemID);

        // following will help, to cancel notification when user click on action button
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEMP_SP, MODE_PRIVATE);
        int temp = sharedPreferences.getInt(KEY_SP_TEMP, 0);
        sharedPreferences.edit().putInt(KEY_SP_TEMP, ++temp).apply();

        return PendingIntent.getService(context,temp,taskDoneIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent snoozeAlarm()
    {
        Intent intent = new Intent(context,SnoozeAlarm.class);
        intent.putExtra(KEY_ITEM_ID,itemID);

        // following will help, to cancel notification when user click on action button
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEMP_SP, MODE_PRIVATE);
        int temp = sharedPreferences.getInt(KEY_SP_TEMP, 0);
        sharedPreferences.edit().putInt(KEY_SP_TEMP, ++temp).apply();

        return PendingIntent.getActivity(context,temp,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
