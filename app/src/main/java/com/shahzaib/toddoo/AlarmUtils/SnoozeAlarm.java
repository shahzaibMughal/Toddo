package com.shahzaib.toddoo.AlarmUtils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.shahzaib.toddoo.DataUtils.Add_Update_Tasks;
import com.shahzaib.toddoo.DataUtils.DbContract;
import com.shahzaib.toddoo.Dialogs.ClockDialog;
import com.shahzaib.toddoo.R;
import com.shahzaib.toddoo.Tasks;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class SnoozeAlarm extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {


    String itemID;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snooze_alarm);
        long receivedItemID = getIntent().getLongExtra(Tasks_AlarmService.KEY_ITEM_ID,-1);
        if(receivedItemID == -1) return;
        itemID = ""+receivedItemID;


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

    }


    private void updateAlarm(String itemID,long reminderTime)
    {
        // get the alarm id
        SharedPreferences sharedPreferences = getSharedPreferences(Add_Update_Tasks.ALARM_SP,MODE_PRIVATE);
        int alarmID = sharedPreferences.getInt(Add_Update_Tasks.KEY_ALARM_ID,0);
        sharedPreferences.edit().putInt(Add_Update_Tasks.KEY_ALARM_ID, ++alarmID).apply();



        ContentValues values = new ContentValues();
        values.put(DbContract.Tasks.COLUMN_ALARM_ID,alarmID);
        values.put(DbContract.Tasks.COLUMN_REMINDER_TIME,reminderTime);
        getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);



        //*** its time to schedule the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, Tasks_AlarmService.class);
        alarmIntent.putExtra(Tasks_AlarmService.KEY_ALARM_ID,alarmID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }

        Toast.makeText(this, "Alarm is set For \n "+formatDate(reminderTime), Toast.LENGTH_LONG).show();
        finish();
    }

    public void customSnooze(View view) {
        ClockDialog clockDialog = new ClockDialog();
        clockDialog.show(getSupportFragmentManager(),"clock_dialog");
    }

    public void snoozeForTwoHour(View view) {
        calendar = Calendar.getInstance();

        Log.i("123456","Current Hour: "+calendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)+2);
        Log.i("123456","after setting,2 Hour: "+calendar.get(Calendar.HOUR_OF_DAY));


        updateAlarm(itemID,calendar.getTimeInMillis());
    }

    public void snoozeForOneHour(View view) {
        calendar = Calendar.getInstance();

        Log.i("123456","Current Hour: "+calendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)+1);
        Log.i("123456","after setting, Hour: "+calendar.get(Calendar.HOUR_OF_DAY));


        updateAlarm(itemID,calendar.getTimeInMillis());
    }

    public void snoozeForFifteenMinutes(View view) {
        calendar = Calendar.getInstance();

        Log.i("123456","Current Minute: "+calendar.get(Calendar.MINUTE));
        calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE)+15);
        Log.i("123456","after setting, Minutes: "+calendar.get(Calendar.MINUTE));

        updateAlarm(itemID,calendar.getTimeInMillis());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);

        Calendar currentCalendar = Calendar.getInstance();
        if(calendar.after(currentCalendar))
        {
            updateAlarm(itemID,calendar.getTimeInMillis());
        }
        else
        {
            Toast.makeText(this, "You Can't select the previous time", Toast.LENGTH_SHORT).show();
        }
    }



    public  String formatDate(long timeInMillis) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.setTimeInMillis(timeInMillis);


        if (reminderCalendar.after(currentCalendar)) {
            String day, year, hour, minute, am_pm;
            int month;
            day = "" + reminderCalendar.get(Calendar.DAY_OF_MONTH);
            month = (reminderCalendar.get(Calendar.MONTH));
            year = "" + reminderCalendar.get(Calendar.YEAR);

            hour = "" + (reminderCalendar.get(Calendar.HOUR));
            if (reminderCalendar.get(Calendar.HOUR) == 0) hour = "12";

            minute = "" + reminderCalendar.get(Calendar.MINUTE);
            if (reminderCalendar.get(Calendar.MINUTE) <= 9) minute = "0" + minute;

            am_pm = "AM";
            if (reminderCalendar.get(Calendar.AM_PM) == 1) am_pm = "PM";


            if ((reminderCalendar.get(Calendar.DAY_OF_MONTH) - currentCalendar.get(Calendar.DAY_OF_MONTH)) == 0) {
                return "Today at " + hour + ":" + minute + " " + am_pm;
            } else if ((reminderCalendar.get(Calendar.DAY_OF_MONTH) - currentCalendar.get(Calendar.DAY_OF_MONTH)) == 1) {// note this is normal case, if tomorrow is first day of month then it will not work,So handle it
                return "Tomorrow at " + hour + ":" + minute + " " + am_pm;
            } else {
                return "at "
                        + hour + ":" + minute + " " + am_pm + " , " +
                        day + "/" + getMonthForInt(month) + "/" + year;
            }
        }

        return null;
    }
    private String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }

}
