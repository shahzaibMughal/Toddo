package com.shahzaib.toddoo.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shahzaib.toddoo.DataUtils.DbContract;
import com.shahzaib.toddoo.R;

import java.util.Random;

public class TasksListTitlesAdapter extends RecyclerView.Adapter<TasksListTitlesAdapter.TasksListTitlesViewHolder>{

    Cursor cursor;
    public  SparseBooleanArray selectedItemsTrack;
    private int selectedItemsCount;
    OnListItemListener listener;
    Context context;




    public TasksListTitlesAdapter(Context context,OnListItemListener listener)
    {
        selectedItemsTrack = new SparseBooleanArray();
        selectedItemsCount = 0;
        this.listener = listener;
        this.context = context;

        // also clear, all the selected states
        ContentValues values = new ContentValues();
        values.put(DbContract.TasksListTitles.COLUMN_SELECTED_STATE,0);
        String where = DbContract.TasksListTitles.COLUMN_SELECTED_STATE+"=1";
        context.getContentResolver().update(
                DbContract.TasksListTitles.CONTENT_URI,
                values,
                where,
                null);
    }


    @NonNull
    @Override
    public TasksListTitlesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_tasks_list_titles,parent,false);
        return new TasksListTitlesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TasksListTitlesViewHolder holder, int position) {
        if(cursor == null) return;
        final int mPosition = position;

        cursor.moveToPosition(position);
        holder.itemView.setTag(cursor.getString(cursor.getColumnIndex(DbContract.TasksListTitles._ID)));
        holder.itemView.setActivated(selectedItemsTrack.get(position,false));



        /* Bind Data ******************/
        String data = cursor.getString(cursor.getColumnIndex(DbContract.TasksListTitles.COLUMN_TITLES));
        holder.listTitleTV.setText(data);



        holder.listTitleRectangle.setBackgroundColor(ContextCompat.getColor(context,R.color.blue));


        /* Click Listeners ******************/
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
                }
            }
        });
        }

    @Override
    public int getItemCount() {
        if(cursor!=null)
        {
            return cursor.getCount();
        }
        return 0;
    }




    public void setCursor(Cursor cursor)
    {
        this.cursor = cursor;
    }

    private void select_unSelectItems(TasksListTitlesViewHolder holder, int position)
    {
        String id = (String) holder.itemView.getTag();

        if(selectedItemsTrack.get(position,false)) // if item is already selected
        {
            selectedItemsTrack.put(position,false);
            selectedItemsCount--;

            ContentValues values = new ContentValues();
            values.put(DbContract.TasksListTitles.COLUMN_SELECTED_STATE,0);
            context.getContentResolver().update(
                    DbContract.TasksListTitles.CONTENT_URI.buildUpon().appendPath(id).build(),
                    values,
                    null,
                    null);
        }
        else // if item is not selected
        {
            selectedItemsTrack.put(position,true);
            selectedItemsCount++;

            ContentValues values = new ContentValues();
            values.put(DbContract.TasksListTitles.COLUMN_SELECTED_STATE,1);
            context.getContentResolver().update(
                    DbContract.TasksListTitles.CONTENT_URI.buildUpon().appendPath(id).build(),
                    values,
                    null,
                    null);
        }
        notifyItemChanged(position);
        listener.onListItemLongClick(selectedItemsCount);

        // following will use if user want to update the item
        if(selectedItemsCount == 1)
        { // get the selected item ID and return it
            String where = DbContract.TasksListTitles.COLUMN_SELECTED_STATE+"=1";
            Cursor cursor = context.getContentResolver().query(
                    DbContract.TasksListTitles.CONTENT_URI,
                    null,
                    where,
                    null,
                    null
            );
            cursor.moveToFirst();
            String ID =""+cursor.getLong(cursor.getColumnIndex(DbContract.TasksListTitles._ID));
            listener.onListItemLongClick_ID(ID);
        }
        else
        {
            listener.onListItemLongClick_ID(null);
        }
    }

    public void deleteSelectedItems()
    {
        String where = DbContract.TasksListTitles.COLUMN_SELECTED_STATE+"=1";
        /* Delete reminder & sub tasks if exists***************/
        Cursor cursor = context.getContentResolver().query(DbContract.TasksListTitles.CONTENT_URI,
                null,
                where,
                null,
                null);
        cursor.moveToFirst();
        do {
            //***** deleting sub tasks
            // before deleting the list, delete list sub tasks
            String tempListID =""+ cursor.getLong(cursor.getColumnIndex(DbContract.TasksListTitles._ID));
            String tempWhere =DbContract.Tasks.COLUMN_LIST_ID+"="+tempListID;
            int result = context.getContentResolver().delete(DbContract.Tasks.CONTENT_URI,tempWhere,null);
            Log.i("123456",result+" sub task deleted");

        }while (cursor.moveToNext());

        int result = context.getContentResolver()
                .delete( DbContract.TasksListTitles.CONTENT_URI, where,null);
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
        values.put(DbContract.TasksListTitles.COLUMN_SELECTED_STATE,0);
        String where = DbContract.TasksListTitles.COLUMN_SELECTED_STATE+"=1";

        context.getContentResolver().update(
                DbContract.TasksListTitles.CONTENT_URI,
                values,
                where,
                null);

        notifyDataSetChanged();
    }








    /*View Holder Class ************************************/
    class TasksListTitlesViewHolder extends RecyclerView.ViewHolder
    {
        TextView listTitleTV;
        ImageView listTitleRectangle;

        public TasksListTitlesViewHolder(View itemView) {
            super(itemView);
            listTitleTV = itemView.findViewById(R.id.listTitleTV);
            listTitleRectangle = itemView.findViewById(R.id.listTitleRectangle);
        }
    }


    public interface OnListItemListener
    {
        void onListItemLongClick(int selectedItemsCount);
        void onListItemLongClick_ID(String id); // jb single item selected ho to us ki id pass krni hy (for edit)
        void onItemClicked(String id);
    }
}
