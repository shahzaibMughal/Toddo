package com.shahzaib.toddoo.DataUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Toddo.db";
    public static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TASKS_LIST_TITLES_TABLE =
                "CREATE TABLE "+DbContract.TasksListTitles.TABLE_NAME+
                        "(" +
                        DbContract.TasksListTitles._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                        DbContract.TasksListTitles.COLUMN_TITLES+" TEXT NOT NULL," +
                        DbContract.TasksListTitles.COLUMN_SELECTED_STATE+" INTEGER " +
                        ");";

        final String SQL_CREATE_TASKS_TABLE =
                "CREATE TABLE "+DbContract.Tasks.TABLE_NAME+
                        "(" +
                        DbContract.Tasks._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                        DbContract.Tasks.COLUMN_TASKS+" TEXT NOT NULL," +
                        DbContract.Tasks.COLUMN_REMINDER_TIME+" INT8," +
                        DbContract.Tasks.COLUMN_SELECTED_STATE+" INTEGER, " +
                        DbContract.Tasks.COLUMN_ALARM_ID+" INTEGER, " +
                        DbContract.Tasks.COLUMN_LIST_ID+" INTEGER NOT NULL," +
                        DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE+" INTEGER," +
                        DbContract.Tasks.COLUMN_NOTIFICATION_ID+" INTEGER" +
                        ");";

        db.execSQL(SQL_CREATE_TASKS_LIST_TITLES_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String  DROP_TASKS_LIST_TITLES_TABLE = "DROP TABLE IF EXISTS "+DbContract.TasksListTitles.TABLE_NAME;;
        final String  DROP_TASKS_TABLE = "DROP TABLE IF EXISTS "+DbContract.Tasks.TABLE_NAME;;

        db.execSQL(DROP_TASKS_LIST_TITLES_TABLE);
        db.execSQL(DROP_TASKS_TABLE);
        onCreate(db);
    }
}
