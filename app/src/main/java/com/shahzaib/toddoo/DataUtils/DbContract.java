package com.shahzaib.toddoo.DataUtils;

import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract{

    public static final String AUTHORITY = "com.shahzaib.toddoo";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);
    public static final String PATH_TASKS_LIST_TITLES_TABLE = TasksListTitles.TABLE_NAME;
    public static final String PATH_TASKS_TABLE = Tasks.TABLE_NAME;

    private DbContract(){}

    public static class Tasks implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS_TABLE).build();
        public static final String TABLE_NAME = "Tasks";
        public static final String COLUMN_TASKS = "Tasks";
        public static final String COLUMN_REMINDER_TIME = "ReminderTime";
        public static final String COLUMN_ALARM_ID = "AlarmID";
        public static final String COLUMN_SELECTED_STATE = "SelectedState";
        public static final String COLUMN_LIST_ID = "ListID";
        public static final String COLUMN_COMPLETED_ITEM_STATE = "CompletedItemState";
        public static final String COLUMN_NOTIFICATION_ID = "NotificationID"; // it will help to cancel notification on action click
        public static final String COLUMN_DELETED_STATE = "DeletedState";
    }

    public static class TasksListTitles implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS_LIST_TITLES_TABLE).build();
        public static final String TABLE_NAME = "TasksListTitles";
        public static final String COLUMN_TITLES = "Titles";
        public static final String COLUMN_SELECTED_STATE = "SelectedState";
        public static final String COLUMN_DELETED_STATE = "DeletedState";
    }
}
