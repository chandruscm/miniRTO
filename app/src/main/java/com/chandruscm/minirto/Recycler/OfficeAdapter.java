package com.chandruscm.minirto.Recycler;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chandruscm.minirto.R;

public class OfficeAdapter extends CursorRecyclerViewAdapter
{

    public OfficeAdapter(Cursor cursor){
        super(cursor);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, Cursor cursor)
    {
        ((TextView)holder.itemView.findViewById(R.id.office_state)).setText(cursor.getString(2));
        ((TextView)holder.itemView.findViewById(R.id.office_state_code)).setText(cursor.getString(0).substring(0,2));
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor)
    {
        ((TextView)holder.itemView.findViewById(R.id.office_district)).setText(cursor.getString(1));
        ((TextView)holder.itemView.findViewById(R.id.office_district_code)).setText(cursor.getString(0));
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.office_list_header_layout, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.office_district);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.office_list_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }
}