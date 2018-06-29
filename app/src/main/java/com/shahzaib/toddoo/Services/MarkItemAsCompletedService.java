package com.shahzaib.toddoo.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.shahzaib.toddoo.DataUtils.DbContract;
import com.shahzaib.toddoo.AlarmUtils.Tasks_AlarmService;

public class MarkItemAsCompletedService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long itemID = intent.getLongExtra(Tasks_AlarmService.KEY_ITEM_ID,-1);

        Log.i("123456","Item received: "+itemID);

        //**** cancel the notification when user click on action button
        Cursor cursor = getBaseContext().getContentResolver().query(
                DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(""+itemID).build(),
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int notificationID =  cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_NOTIFICATION_ID));
        if(notificationID>0)
        {
            NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationID);
        }



        // mark task as completed
        ContentValues values = new ContentValues();
        values.put(DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE,1);
        int result = getBaseContext().getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(""+itemID).build(),
                values,null,null);
        getBaseContext().getContentResolver().notifyChange(DbContract.Tasks.CONTENT_URI,null);



        if(result>0)
        {
            Log.i("123456","Task Completed");
            Toast.makeText(this, "Task Completed", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Failed To Mark as Completed", Toast.LENGTH_SHORT).show();
            Log.i("123456","Failed To Mark");
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

}
