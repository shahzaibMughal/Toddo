package com.shahzaib.toddoo.DataUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ToddoContentProvider extends ContentProvider {

    private static final int TASKS_LIST_TITLES = 100;
    private static final int TASKS_LIST_TITLES_WITH_ID = 101;
    private static final int TASKS = 200;
    private static final int TASKS_WITH_ID = 201;


    DbHelper dbHelper;
    UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        uriMatcher = buildUriMatcher();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);

        switch(match)
        {
            case TASKS_LIST_TITLES:
                 cursor = db.query(
                        DbContract.TasksListTitles.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,null,null,null);
                cursor.setNotificationUri(getContext().getContentResolver(),uri); // to notify CursorLoader for any data changes
                return cursor;


            case TASKS_LIST_TITLES_WITH_ID:
                String taskListId = uri.getPathSegments().get(1); // this will give the last path
                String taskListSelection = "_id=?";
                String[] taskListSelectionArgs = new String[]{taskListId};

                cursor =  db.query(DbContract.TasksListTitles.TABLE_NAME,
                        null,
                        taskListSelection,
                        taskListSelectionArgs,
                        null,
                        null,
                        null,
                        null);
                cursor.setNotificationUri(getContext().getContentResolver(),uri);
                return cursor;


            case TASKS:
                cursor = db.query(
                        DbContract.Tasks.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,null,null,null);
                cursor.setNotificationUri(getContext().getContentResolver(),uri); // to notify CursorLoader for any data changes
                return cursor;


            case TASKS_WITH_ID:
                String taskId = uri.getPathSegments().get(1); // this will give the last path
                String taskSelection = "_id=?";
                String[] taskSelectionArgs = new String[]{taskId};

                cursor =  db.query(DbContract.Tasks.TABLE_NAME,
                        null,
                        taskSelection,
                        taskSelectionArgs,
                        null,
                        null,
                        null,
                        null);
                cursor.setNotificationUri(getContext().getContentResolver(),uri);
                return cursor;


                default:
                    throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long insertedItemID;
        Uri insertedItemUri;
        int match = uriMatcher.match(uri);

        switch(match) {
            case TASKS_LIST_TITLES:
                insertedItemID = db.insert(DbContract.TasksListTitles.TABLE_NAME, null, values);
                insertedItemUri = uri.buildUpon().appendPath("" + insertedItemID).build();
                getContext().getContentResolver().notifyChange(uri, null);// to notify CursorLoader for any data changes
                return insertedItemUri;


            case TASKS:
                insertedItemID = db.insert(DbContract.Tasks.TABLE_NAME, null, values);
                insertedItemUri = uri.buildUpon().appendPath("" + insertedItemID).build();
                getContext().getContentResolver().notifyChange(uri, null);// to notify CursorLoader for any data changes
                return insertedItemUri;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedItemsCount;
        int match = uriMatcher.match(uri);

        switch(match)
        {
            case TASKS_LIST_TITLES:
                deletedItemsCount = db.delete(DbContract.TasksListTitles.TABLE_NAME,selection,selectionArgs);
                getContext().getContentResolver().notifyChange(uri,null); // to notify CursorLoader for any data changes
                return deletedItemsCount;


            case TASKS_LIST_TITLES_WITH_ID:
                String taskListId = uri.getPathSegments().get(1); // this will give the last path
                String taskListSelection = "_id=?";
                String[] taskListSelectionArgs = new String[]{taskListId};

                deletedItemsCount = db.delete(DbContract.TasksListTitles.TABLE_NAME,taskListSelection,taskListSelectionArgs);
                getContext().getContentResolver().notifyChange(uri,null); // to notify CursorLoader for any data changes
                return deletedItemsCount;


            case TASKS:
                deletedItemsCount = db.delete(DbContract.Tasks.TABLE_NAME,selection,selectionArgs);
                getContext().getContentResolver().notifyChange(uri,null); // to notify CursorLoader for any data changes
                return deletedItemsCount;


            case TASKS_WITH_ID:
                String taskId = uri.getPathSegments().get(1); // this will give the last path
                String taskSelection = "_id=?";
                String[] taskSelectionArgs = new String[]{taskId};

                deletedItemsCount = db.delete(DbContract.Tasks.TABLE_NAME,taskSelection,taskSelectionArgs);
                getContext().getContentResolver().notifyChange(uri,null); // to notify CursorLoader for any data changes
                return deletedItemsCount;


            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedItemsCount;
        int match = uriMatcher.match(uri);

        switch(match)
        {
            case TASKS_LIST_TITLES:
                updatedItemsCount = db.update(DbContract.TasksListTitles.TABLE_NAME,values,selection,selectionArgs);
                getContext().getContentResolver().notifyChange(uri,null); // to notify CursorLoader for any data changes
                return updatedItemsCount;


            case TASKS_LIST_TITLES_WITH_ID:
                String taskListId = uri.getPathSegments().get(1); // this will give the last path
                String taskListSelection = "_id=?";
                String[] taskListSelectionArgs = new String[]{taskListId};

                updatedItemsCount = db.update(DbContract.TasksListTitles.TABLE_NAME,values,taskListSelection,taskListSelectionArgs);
                return updatedItemsCount;


            case TASKS:
                updatedItemsCount = db.update(DbContract.Tasks.TABLE_NAME,values,selection,selectionArgs);
                getContext().getContentResolver().notifyChange(uri,null); // to notify CursorLoader for any data changes
                return updatedItemsCount;


            case TASKS_WITH_ID:
                String taskId = uri.getPathSegments().get(1); // this will give the last path
                String taskSelection = "_id=?";
                String[] taskSelectionArgs = new String[]{taskId};

                updatedItemsCount = db.update(DbContract.Tasks.TABLE_NAME,values,taskSelection,taskSelectionArgs);
                return updatedItemsCount;


            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    private UriMatcher buildUriMatcher()
    {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DbContract.AUTHORITY,DbContract.PATH_TASKS_LIST_TITLES_TABLE,TASKS_LIST_TITLES);
        uriMatcher.addURI(DbContract.AUTHORITY,DbContract.PATH_TASKS_LIST_TITLES_TABLE+"/#",TASKS_LIST_TITLES_WITH_ID);
        uriMatcher.addURI(DbContract.AUTHORITY,DbContract.PATH_TASKS_TABLE,TASKS);
        uriMatcher.addURI(DbContract.AUTHORITY,DbContract.PATH_TASKS_TABLE+"/#",TASKS_WITH_ID);
        return uriMatcher;
    }


}
