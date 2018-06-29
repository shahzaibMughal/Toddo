package com.shahzaib.toddoo;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shahzaib.toddoo.Adapters.TasksAdapter;
import com.shahzaib.toddoo.DataUtils.Add_Update_Tasks;
import com.shahzaib.toddoo.DataUtils.DbContract;
import com.shahzaib.toddoo.AlarmUtils.Tasks_AlarmService;

public class Tasks extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>, TasksAdapter.OnTaskListItemListener, ActionMode.Callback{

    static final int TASKS_CURSOR_LOADER = 2;
    public static final String KEY_ITEM_LIST_ID = "ListID";

    RecyclerView tasksRecyclerView;
    TextView emptyTasksTV;
    String itemListID;
    TasksAdapter adapter;
    int selectedItemsCount;
    private boolean isActionModeActivated = false;
    ActionMode actionMode;
    private String singleSelectedItemId = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyTasksTV = findViewById(R.id.emptyTasksTV);
        itemListID = getIntent().getStringExtra(KEY_ITEM_LIST_ID);
        adapter = new TasksAdapter(this,this);


        //setting the toolbar title
        Cursor cursor = getContentResolver().query(DbContract.TasksListTitles.CONTENT_URI.buildUpon().appendPath(itemListID).build(),
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        getSupportActionBar().setTitle(cursor.getString(cursor.getColumnIndex(DbContract.TasksListTitles.COLUMN_TITLES)));


        getLoaderManager().initLoader(TASKS_CURSOR_LOADER,null,this);


        /*delete item on Swipe, re-order item on drag ***************/
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                // delete the swiped item
                String id = (String) viewHolder.itemView.getTag();
                // if reminder is set then cancel the reminder also
                Cursor cursor = getContentResolver().query(
                        DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(id).build(),
                        null,
                        null,
                        null,
                        null
                );
                int alarmID = 0;
                if(cursor.moveToFirst())
                {
                    alarmID = cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_ALARM_ID));
                    if(alarmID!=0)
                    {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                        Intent alarmIntent = new Intent(Tasks.this, Tasks_AlarmService.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(Tasks.this, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        alarmManager.cancel(pendingIntent);

                        Log.i("123456", "Alarm is canceled, Id "+alarmID);
                    }
                }


                int result = getContentResolver().
                        delete(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(id).build(), null, null);
                Toast.makeText(Tasks.this, result+" Item Deleted", Toast.LENGTH_SHORT).show();
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }






        }).attachToRecyclerView(tasksRecyclerView);

    }








    public void addTask(View view) {
        Intent intent = new Intent(this, Add_Update_Tasks.class);
        intent.putExtra(Add_Update_Tasks.KEY_IS_TASK_UPDATE,false); // optional
        intent.putExtra(KEY_ITEM_LIST_ID,itemListID);
        startActivity(intent);
    }

    private void startStopActionModeToolbar()
    {
        if(selectedItemsCount == 1)
        {
            if(!isActionModeActivated)
            {
                actionMode = startSupportActionMode(this);
                isActionModeActivated = true;
            }
            else
            {
                actionMode.getMenu().findItem(R.id.ic_edit).setVisible(true);
            }
        }
        else if(selectedItemsCount > 1)
        {
            actionMode.getMenu().findItem(R.id.ic_edit).setVisible(false);
        }
        else
        {
            actionMode.finish();
        }

    }
    public void updateTasks(String id)
    {
        Intent intent = new Intent(Tasks.this,Add_Update_Tasks.class);
        intent.putExtra(Add_Update_Tasks.KEY_IS_TASK_UPDATE,true);
        intent.putExtra(Add_Update_Tasks.KEY_ITEM_ID,id);
        intent.putExtra(KEY_ITEM_LIST_ID,itemListID);


        adapter.clearSelection();
        startActivity(intent);
    }












    /* Loader Callbacks************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id)
        {
            case TASKS_CURSOR_LOADER:
                return new CursorLoader(
                        this,
                        DbContract.Tasks.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
                default:
                    return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        String where = DbContract.Tasks.COLUMN_LIST_ID+"="+itemListID;
        Cursor cursor = getContentResolver().query(DbContract.Tasks.CONTENT_URI,null,where,null,null);
        if(cursor.getCount()==0)
        {
            emptyTasksTV.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
        }
        else
        {
            emptyTasksTV.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }


        adapter.setCursor(data,getIntent().getStringExtra(KEY_ITEM_LIST_ID));
        tasksRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }







    /* Task List Click Listeners***********************/
    @Override
    public void onListItemLongClick(int selectedItemsCount) {
        this.selectedItemsCount = selectedItemsCount;
        startStopActionModeToolbar();
        if(actionMode!=null)
        {
            actionMode.setTitle(""+selectedItemsCount);
        }
    }

    @Override
    public void onListItemLongClick_ID(String id) {
        singleSelectedItemId = id;
    }

    @Override
    public void onItemClicked(String id) {
       /* Intent TasksIntent = new Intent(this, com.shahzaib.toddoo.Tasks.class);
        TasksIntent.putExtra(com.shahzaib.toddoo.Tasks.KEY_ITEM_LIST_ID,id);
        startActivity(TasksIntent);*/
    }



    /* ActionMode Callbacks *********************/
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.action_mode_toolbar,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.ic_delete:
                // deleted button pressed
                Toast.makeText(this, "Delete button pressed", Toast.LENGTH_SHORT).show();
                adapter.deleteSelectedItems();
                break;

            case R.id.ic_edit:
                updateTasks(singleSelectedItemId);
                break;


        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        isActionModeActivated = false;
        adapter.clearSelection();
    }
}
