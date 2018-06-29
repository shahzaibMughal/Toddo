package com.shahzaib.toddoo.DataUtils;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.shahzaib.toddoo.Dialogs.CalendarDialog;
import com.shahzaib.toddoo.Dialogs.ClockDialog;
import com.shahzaib.toddoo.AlarmUtils.Tasks_AlarmService;
import com.shahzaib.toddoo.R;
import com.shahzaib.toddoo.Tasks;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class Add_Update_Tasks extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    public static final String KEY_IS_TASK_UPDATE = "isTaskItemUpdate";
    public static final String KEY_ITEM_ID = "ItemID"; // in case of item update
    public static final String ALARM_SP = "tasksAlarmSP"; // in case of item update
    public static final String KEY_ALARM_ID = "tasksAlarmID"; // in case of item update

    EditText addTaskET;
    TextView reminderStatus;
    SwitchCompat reminderToggleBtn;
    boolean isReminderActive = false, isItemNew = false;
    boolean isTodayReminder, isTomorrowReminder, isCustomReminder;
    long reminderTime;
    Calendar calendar;
    String ItemID, itemListID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__update__tasks);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        addTaskET = findViewById(R.id.addTaskET);
        reminderToggleBtn = findViewById(R.id.reminderToggleBtn);
        reminderStatus = findViewById(R.id.reminderStatus);


        /* Data Binding  *****************/
        if (getIntent().getBooleanExtra(KEY_IS_TASK_UPDATE, false))
        {
            getSupportActionBar().setTitle("Update Task");

            // check reminder is already set or not
            // - if set then turn on the toggle button
            ItemID = getIntent().getStringExtra(KEY_ITEM_ID);
            Cursor cursor = getContentResolver().query(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(ItemID).build(),
                    null,
                    null,
                    null,
                    null);
            if(cursor.moveToFirst())
            {
                addTaskET.setText(cursor.getString(cursor.getColumnIndex(DbContract.Tasks.COLUMN_TASKS)));
                itemListID =""+ cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_LIST_ID));
                int alarmID = cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_ALARM_ID));
                if (alarmID != 0) {// if alarm is active
                    reminderToggleBtn.setChecked(true);
                    reminderToggleBtn.setVisibility(View.VISIBLE);
                    isReminderActive = true;
                    reminderStatus.setText(formatDate(cursor.getLong(cursor.getColumnIndex(DbContract.Tasks.COLUMN_REMINDER_TIME))));
                }
                cursor.close();
            }
            isItemNew = false; // optional, just for some simplicity
        }
        else {
            getSupportActionBar().setTitle("Create New Task");
            itemListID = getIntent().getStringExtra(Tasks.KEY_ITEM_LIST_ID);
            isItemNew = true;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_update_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_done:
                // get the data and set the alarm when done clicked
                String data = addTaskET.getText().toString();
                if (data.length() > 0) {

                    if(isItemNew)
                    {
                        insertData(data);
                    }
                    else
                    {
                        updateData(data);
                    }

                    finish();
                }
                else
                {
                    Toast.makeText(this, "First Enter data", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }




    public void openTodayClock(View view) {
        isTodayReminder = true;
        isTomorrowReminder = false;
        isCustomReminder = false;
        ClockDialog clockDialog = new ClockDialog();
        clockDialog.show(getSupportFragmentManager(), "today_clock_dialog");
    }

    public void openTomorrowClock(View view) {
        isTodayReminder = false;
        isTomorrowReminder = true;
        isCustomReminder = false;
        ClockDialog clockDialog = new ClockDialog();
        clockDialog.show(getSupportFragmentManager(), "tomorrow_clock_dialog");
    }

    public void openCustomCalendarAndClock(View view) {
        isTodayReminder = false;
        isTomorrowReminder = false;
        isCustomReminder = true;
        CalendarDialog calendarDialog = new CalendarDialog();
        calendarDialog.show(getSupportFragmentManager(), "custom_calendar_dialog");
    }

    public void cancelReminder(View view) {
        isReminderActive = false;
        reminderTime = 0;
        reminderStatus.setText("");
        reminderToggleBtn.setChecked(false);
        reminderToggleBtn.setVisibility(View.GONE);
        if(!isItemNew)
        {
            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_REMINDER_TIME,0);
            values.put(DbContract.Tasks.COLUMN_ALARM_ID,0);
            getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(ItemID).build(),
                    values,null,null);
        }
    }






    /* Clock & Calender Dialogs listeners*****************************/
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        if (isTodayReminder) {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
        } else if (isTomorrowReminder) {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        } else if (isCustomReminder) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
        }


        reminderTime = calendar.getTimeInMillis();
        Calendar currentCalendar = Calendar.getInstance();
        if (calendar.after(currentCalendar)) {
            isReminderActive = true;
            reminderToggleBtn.setChecked(true);
            reminderToggleBtn.setVisibility(View.VISIBLE);

            // Format the date and show it in reminder status
            reminderStatus.setText(formatDate(reminderTime));
        } else {
            isReminderActive = false;
            reminderToggleBtn.setChecked(false);
            reminderToggleBtn.setVisibility(View.GONE);
            Toast.makeText(this, "You Can't Set Previous Date", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        ClockDialog clockDialog = new ClockDialog();
        clockDialog.show(getSupportFragmentManager(), "custom_reminder_clock_dialog");
    }






    /* Helper Functions****************/
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

    private void setAlarm(int alarmID) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, Tasks_AlarmService.class);
        alarmIntent.putExtra(Tasks_AlarmService.KEY_ALARM_ID,alarmID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
        Log.i("123456", "Alarm is active " + formatDate(reminderTime)+", ID: "+alarmID);
    }

    public void cancelAlarm(int alarmID) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, Tasks_AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);

        Log.i("123456", "Alarm is canceled, Id "+alarmID);
    }

    private void insertData(String data)
    {
        ContentValues values = new ContentValues();
        values.put(DbContract.Tasks.COLUMN_TASKS, data);
        values.put(DbContract.Tasks.COLUMN_LIST_ID, itemListID);
        Log.i("123456","itemListID: "+itemListID);
        // set the alarm if user set the alarm
        if (isReminderActive && (formatDate(reminderTime) != null))
        {
            SharedPreferences sharedPreferences = getSharedPreferences(ALARM_SP, MODE_PRIVATE);
            int alarmID = sharedPreferences.getInt(KEY_ALARM_ID, 0);
            sharedPreferences.edit().putInt(KEY_ALARM_ID, ++alarmID).apply();

            values.put(DbContract.Tasks.COLUMN_REMINDER_TIME, reminderTime);
            values.put(DbContract.Tasks.COLUMN_ALARM_ID, alarmID);
            setAlarm(alarmID); // no need to send reminder time, because it global and automatically captured
        }

        Uri uri = getContentResolver().insert(DbContract.Tasks.CONTENT_URI, values);
        Log.i("123456","insertedItem uri: "+uri.toString());

        getContentResolver().notifyChange(DbContract.Tasks.CONTENT_URI,null);
        Toast.makeText(this, "Data Inserted Successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateData(String data)
    {
        ContentValues values = new ContentValues();
        values.put(DbContract.Tasks.COLUMN_TASKS, data);
        values.put(DbContract.Tasks.COLUMN_LIST_ID, itemListID); // optional
        // if user set the alarm then if previous alarm exists cancel it and start new
        if (isReminderActive && (formatDate(reminderTime) != null))
        {
            //** checking if alarm already exists or not, if exists then remove it
            Cursor cursor = getContentResolver().query(
                    DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(ItemID).build()
                    , null,
                    null,
                    null,
                    null);

            cursor.moveToFirst();
            int previousAlarmID = cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_ALARM_ID));
            if (previousAlarmID != 0) { // alarm already exists, So, del the previous alarm and set new
                //** canceling previous alarm
                ContentValues tempValues = new ContentValues();
                tempValues.put(DbContract.Tasks.COLUMN_ALARM_ID, 0);
                tempValues.put(DbContract.Tasks.COLUMN_REMINDER_TIME, 0);
                getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(ItemID).build(), tempValues, null, null);
                cancelAlarm(previousAlarmID);
            }



            //** Starting new alarm
            SharedPreferences sharedPreferences = getSharedPreferences(ALARM_SP, MODE_PRIVATE);
            int alarmID = sharedPreferences.getInt(KEY_ALARM_ID, 1000);
            sharedPreferences.edit().putInt(KEY_ALARM_ID, ++alarmID).apply();

            values.put(DbContract.Tasks.COLUMN_REMINDER_TIME, reminderTime);
            values.put(DbContract.Tasks.COLUMN_ALARM_ID, alarmID);
            setAlarm(alarmID); // no need to send reminder time, because it global and automatically captured


        }

        int result = getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(ItemID).build(), values, null, null);
        getContentResolver().notifyChange(DbContract.Tasks.CONTENT_URI,null);
        Toast.makeText(this, result+ " item Updated", Toast.LENGTH_SHORT).show();
    }

}








