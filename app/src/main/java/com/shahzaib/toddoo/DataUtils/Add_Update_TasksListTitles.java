package com.shahzaib.toddoo.DataUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.shahzaib.toddoo.R;

public class Add_Update_TasksListTitles extends AppCompatActivity {

    public static final String KEY_IS_TASK_LIST_TITLE_UPDATE = "isTaskUpdate";
    public static final String KEY_ITEM_ID = "ItemID"; // in case of item update


    EditText addListET;
    boolean isItemNew = false;
    String ItemID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__update__tasks_list_titles);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        addListET = findViewById(R.id.addListET);




        /* Data Binding  *****************/
        if (getIntent().getBooleanExtra(KEY_IS_TASK_LIST_TITLE_UPDATE, false)) {
            getSupportActionBar().setTitle("Update List");


            ItemID = getIntent().getStringExtra(KEY_ITEM_ID);
            Cursor cursor = getContentResolver().query(DbContract.TasksListTitles.CONTENT_URI.buildUpon().appendPath(ItemID).build(),
                    null,
                    null,
                    null,
                    null);
            cursor.moveToFirst();
            addListET.setText(cursor.getString(cursor.getColumnIndex(DbContract.TasksListTitles.COLUMN_TITLES)));
            cursor.close();
            isItemNew = false; // optional, just for some simplicity
        } else {
            getSupportActionBar().setTitle("Create New List");
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
                String data = addListET.getText().toString();
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

















    /* Helper Functions****************/

    private void insertData(String data)
    {
        ContentValues values = new ContentValues();
        values.put(DbContract.TasksListTitles.COLUMN_TITLES, data);

        Uri uri = getContentResolver().insert(DbContract.TasksListTitles.CONTENT_URI, values);
        getContentResolver().notifyChange(DbContract.TasksListTitles.CONTENT_URI,null);
        Log.i("123456","New Data Uri: "+uri.toString());

        Toast.makeText(this, "Data Inserted Successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateData(String data)
    {
        ContentValues values = new ContentValues();
        values.put(DbContract.TasksListTitles.COLUMN_TITLES, data);

        int result = getContentResolver().update(DbContract.TasksListTitles.CONTENT_URI.buildUpon().appendPath(ItemID).build(), values, null, null);
        getContentResolver().notifyChange(DbContract.TasksListTitles.CONTENT_URI,null);
        Toast.makeText(this, result+ " item Updated", Toast.LENGTH_SHORT).show();
    }

}
