package com.shahzaib.toddoo.Adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shahzaib.toddoo.DataUtils.DbContract;
import com.shahzaib.toddoo.AlarmUtils.Tasks_AlarmService;
import com.shahzaib.toddoo.R;

import java.util.Collections;

import static android.content.Context.ALARM_SERVICE;

public class TasksAdapter  extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder>{

    Cursor cursor;
    Context context;
    SparseBooleanArray selectedItemsTrack, completedItemsTrack;
    private int selectedItemsCount;
    OnTaskListItemListener listener;


    public TasksAdapter(Context context, OnTaskListItemListener listener)
    {
        this.context = context;
        this.listener = listener;
        selectedItemsTrack = new SparseBooleanArray();
        completedItemsTrack = new SparseBooleanArray();
        selectedItemsCount = 0;

        // also clear, all the selected states
        ContentValues values = new ContentValues();
        values.put(DbContract.Tasks.COLUMN_SELECTED_STATE,0);
        String where = DbContract.Tasks.COLUMN_SELECTED_STATE+"=1";
        context.getContentResolver().update(
                DbContract.Tasks.CONTENT_URI,
                values,
                where,
                null);


        //put selected stats of all the selected items
        String where2 = DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE+"=1";
        Cursor tempCursor = context.getContentResolver().query(DbContract.Tasks.CONTENT_URI,null,
                where2,null,null);
        if(tempCursor.moveToFirst())
        {
            do{
                int ID = tempCursor.getInt(tempCursor.getColumnIndex(DbContract.Tasks._ID));
                completedItemsTrack.put(ID,true);
            }while (tempCursor.moveToNext());
        }

    }



    @NonNull
    @Override
    public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_tasks,parent,false);
        return new TasksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TasksViewHolder holder, int position) {
        if(cursor == null) return;
        final int mPosition = position; // for inner class access


        cursor.moveToPosition(position);
        holder.itemView.setTag(cursor.getString(cursor.getColumnIndex(DbContract.Tasks._ID)));
        holder.itemView.setActivated(selectedItemsTrack.get(position,false));


        int itemID = cursor.getInt(cursor.getColumnIndex(DbContract.Tasks._ID));
        if(completedItemsTrack.get(itemID,false))
        {
            holder.tasksTV.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.ic_delete.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.tasksTV.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
            holder.ic_delete.setVisibility(View.GONE);
        }





        /* Bind Data ********************/
        String data = cursor.getString(cursor.getColumnIndex(DbContract.Tasks.COLUMN_TASKS));
        holder.tasksTV.setText(data);







        /* Click Listeners *********************/
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                select_unSelectItems(holder, mPosition);
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedItemsCount>0)
                {
                    select_unSelectItems(holder,mPosition);
                }
                else
                {
                    listener.onItemClicked((String) holder.itemView.getTag());
                    Log.i("123456","Item Clicked");
                    // perform text strike here
                    // if item is completed then mark as un-complete it, otherwise mark as complete
                    String itemID =""+ holder.itemView.getTag();
                    strike_unStrike_item(itemID,mPosition);
                }
            }
        });

        holder.ic_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // deleted the item,
                int result = context.getContentResolver().delete(
                        DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(""+holder.itemView.getTag()).build(),
                        null,
                        null);
                if(result>0) Log.i("123456",holder.itemView.getTag()+" Deleted");
                else Log.i("123456",holder.itemView.getTag()+" Failed to Delete");
            }
        });



        /* Drag item to reOrder *******************/

        /*holder.ic_drag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(holder);
                }
                return false;
            }
        });*/


    }

    @Override
    public int getItemCount() {
        if(cursor !=null)
        {
            return cursor.getCount();
        }
        return 0;
    }









    private void strike_unStrike_item(String itemID,int position)
    {// when task is completed strike the item
        boolean isStrike;
        Cursor cursor = context.getContentResolver().query(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(itemID).build(),
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        if(cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE))==1)
        {
            isStrike = true;
        }
        else
        {
            isStrike = false;
        }


        int ID = cursor.getInt(cursor.getColumnIndex(DbContract.Tasks._ID));
        if(isStrike)
        {// then unStrike the item
            completedItemsTrack.put(ID,false);
            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE,0);
            context.getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);
            Log.i("123456",itemID+" unStriked");
        }
        else
        {// then strike the item
            completedItemsTrack.put(ID,true);
            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE,1);
            context.getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);
            Log.i("123456",itemID+" Striked");
        }
        notifyItemChanged(position);





        /*if(isStrike)
        {// then unStrike
            completedItemsTrack.put(position,false);
            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE,0);
            context.getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);
            Log.i("123456",itemID+" unStriked");
        }
        else
        {// strike the item
            completedItemsTrack.put(position,true);
            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_COMPLETED_ITEM_STATE,1);
            context.getContentResolver().update(DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);
            Log.i("123456",itemID+" Striked");
        }*/
    }


    private void select_unSelectItems(TasksAdapter.TasksViewHolder holder, int position)
    {
        String id = (String) holder.itemView.getTag();

        if(selectedItemsTrack.get(position,false)) // if item is already selected
        {
            selectedItemsTrack.put(position,false);
            selectedItemsCount--;

            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_SELECTED_STATE,0);
            context.getContentResolver().update(
                    DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(id).build(),
                    values,
                    null,
                    null);
        }
        else //if item is not selected
        {
            selectedItemsTrack.put(position,true);
            selectedItemsCount++;

            ContentValues values = new ContentValues();
            values.put(DbContract.Tasks.COLUMN_SELECTED_STATE,1);
            int temp = context.getContentResolver().update(
                    DbContract.Tasks.CONTENT_URI.buildUpon().appendPath(id).build(),
                    values,
                    null,
                    null);
        }
        notifyItemChanged(position);
        listener.onListItemLongClick(selectedItemsCount);

        // following will use if user want to update the item
        if(selectedItemsCount == 1)
        { // get the selected item ID and return it
            String where = DbContract.Tasks.COLUMN_SELECTED_STATE+"=1";
            Cursor cursor = context.getContentResolver().query(
                    DbContract.Tasks.CONTENT_URI,
                    null,
                    where,
                    null,
                    null
            );
            cursor.moveToFirst();
                String ID =""+cursor.getLong(cursor.getColumnIndex(DbContract.Tasks._ID));
                listener.onListItemLongClick_ID(ID);
        }
        else
        {
            listener.onListItemLongClick_ID(null);
        }
    }

    public void setCursor(Cursor cursor,String itemListID)
    {
        this.cursor = cursor;
        String where = DbContract.Tasks.COLUMN_LIST_ID+"="+itemListID;
        this.cursor = context.getContentResolver().query(DbContract.Tasks.CONTENT_URI,null,where,null,null);
    }

    public void deleteSelectedItems()
    {
        String where = DbContract.Tasks.COLUMN_SELECTED_STATE+"=1";
        /* Delete reminder if exists***************/
        Cursor cursor = context.getContentResolver().query(DbContract.Tasks.CONTENT_URI,
                null,
                where,
                null,
                null);
        cursor.moveToFirst();
        do {
            //****** deleting reminder
            int alarmID = cursor.getInt(cursor.getColumnIndex(DbContract.Tasks.COLUMN_ALARM_ID));
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, Tasks_AlarmService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);

            Log.i("123456", "Alarm is canceled, Id "+alarmID);
        }while (cursor.moveToNext());

        int result = context.getContentResolver()
                .delete( DbContract.Tasks.CONTENT_URI, where,null);
        if(result>0)
        {
            Toast.makeText(context, result+" Items Deleted", Toast.LENGTH_SHORT).show();
            clearSelection();
        }
        else
        {
            Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show();
        }

    }

    public void clearSelection()
    {
        selectedItemsTrack.clear();
        selectedItemsCount = 0;
        listener.onListItemLongClick(selectedItemsCount);

        ContentValues values = new ContentValues();
        values.put(DbContract.Tasks.COLUMN_SELECTED_STATE,0);
        String where = DbContract.Tasks.COLUMN_SELECTED_STATE+"=1";

        context.getContentResolver().update(
                DbContract.Tasks.CONTENT_URI,
                values,
                where,
                null);
        notifyDataSetChanged();
    }












    class TasksViewHolder extends RecyclerView.ViewHolder
    {
        ImageButton ic_drag, ic_delete;
        TextView tasksTV;

        public TasksViewHolder(View itemView) {
            super(itemView);
            ic_drag = itemView.findViewById(R.id.ic_drag);
            ic_delete = itemView.findViewById(R.id.ic_delete);
            tasksTV = itemView.findViewById(R.id.taskTV);
        }
    }

    public interface OnTaskListItemListener
    {
        void onListItemLongClick(int selectedItemsCount);
        void onListItemLongClick_ID(String id); // jb single item selected ho to us ki id pass krni hy (for edit)
        void onItemClicked(String id);
    }
}
