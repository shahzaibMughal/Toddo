package com.shahzaib.toddoo;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shahzaib.toddoo.Adapters.TasksListTitlesAdapter;
import com.shahzaib.toddoo.DataUtils.Add_Update_TasksListTitles;
import com.shahzaib.toddoo.DataUtils.DbContract;

public class TasksListTitles extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>, TasksListTitlesAdapter.OnListItemListener
,ActionMode.Callback{

    static final int TASKS_LIST_TITLES_CURSOR_LOADER = 1;


    RecyclerView taskListTitlesRecyclerView;
    TextView emptyListTV;

    TasksListTitlesAdapter adapter;
    int selectedItemsCount;
    private boolean isActionModeActivated = false;
    ActionMode actionMode;
    private String singleSelectedItemId = null;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_list_titles);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.app_name);
        taskListTitlesRecyclerView = findViewById(R.id.taskListTitlesRecyclerView);
        taskListTitlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyListTV = findViewById(R.id.emptyListTV);
        adapter = new  TasksListTitlesAdapter(this,this);


        getLoaderManager().initLoader(TASKS_LIST_TITLES_CURSOR_LOADER,null,this);


        /*delete item on Swipe ***************/
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                // delete the swiped item
                String id = (String) viewHolder.itemView.getTag();

                // before deleting the list, delete list sub tasks
                String where = DbContract.Tasks.COLUMN_LIST_ID+"="+id;
                int subTask = getContentResolver().delete(DbContract.Tasks.CONTENT_URI,where,null);
                Toast.makeText(TasksListTitles.this, subTask+" sub tasks Deleted", Toast.LENGTH_SHORT).show();

                int result = getContentResolver().
                        delete(DbContract.TasksListTitles.CONTENT_URI.buildUpon().appendPath(id).build(), null, null);
                Toast.makeText(TasksListTitles.this, result+" Item Deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(taskListTitlesRecyclerView);





    }


















    public void addTasksList(View view)
    {
        Intent intent = new Intent(this, Add_Update_TasksListTitles.class);
        intent.putExtra(Add_Update_TasksListTitles.KEY_IS_TASK_LIST_TITLE_UPDATE,false); // optional
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

    public void updateTasksList(String id)
    {
        Intent intent = new Intent(TasksListTitles.this,Add_Update_TasksListTitles.class);
        intent.putExtra(Add_Update_TasksListTitles.KEY_IS_TASK_LIST_TITLE_UPDATE,true);
        intent.putExtra(Add_Update_TasksListTitles.KEY_ITEM_ID,id);

        adapter.clearSelection();
        startActivity(intent);
    }















    /* Loader Callbacks***************************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id)
        {
            case TASKS_LIST_TITLES_CURSOR_LOADER:
                return new CursorLoader(
                        this,
                        DbContract.TasksListTitles.CONTENT_URI,
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
        if(data.getCount()==0)
        {
            emptyListTV.setVisibility(View.VISIBLE);
            taskListTitlesRecyclerView.setVisibility(View.GONE);

        }
        else
        {
            taskListTitlesRecyclerView.setVisibility(View.VISIBLE);
            emptyListTV.setVisibility(View.GONE);
        }

        adapter.setCursor(data);
        taskListTitlesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }




    /* On ListItem Listeners *********************/
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
        Intent TasksIntent = new Intent(this, com.shahzaib.toddoo.Tasks.class);
        TasksIntent.putExtra(com.shahzaib.toddoo.Tasks.KEY_ITEM_LIST_ID,id);
        startActivity(TasksIntent);
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
                updateTasksList(singleSelectedItemId);
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
