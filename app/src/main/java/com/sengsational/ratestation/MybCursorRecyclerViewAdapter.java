package com.sengsational.ratestation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class MybCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<MybCursorRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = MybCursorRecyclerViewAdapter.class.getSimpleName();
    private boolean mShowDateInList;
    private boolean mSkipGagState = false;
    private Context mContext;
    private String mQueryText = "";

    public MybCursorRecyclerViewAdapter(Context context, Cursor c, boolean showDateInList, boolean skipGagState) {
        super(c, true);
        mContext = context;
        mShowDateInList = showDateInList;
        mSkipGagState = skipGagState;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.v(TAG, ">>>>>>>>>>>>onCreateViewHolder<<<<<<<<<<<<<");
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View modelView = inflater.inflate(R.layout.b_item, parent, false);
        return new ViewHolder(modelView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor aCursor) {
        //Log.v(TAG, "onBindViewHolder with cursor " + aCursor);
        //Log.v(TAG, ">>>>>>>>>>>>onBindViewHolder<<<<<<<<<<<<<");
        final StationItem model = new StationItem(aCursor);
        final StationDbItem modelItem = model.getStationDbItem();
        final int layoutPosition = viewHolder.getLayoutPosition();
        viewHolder.tvFirst.setText(modelItem.getBeerName());
        if (mShowDateInList){
            viewHolder.tvSecond.setText(modelItem.getBeerName()) ;
        } else {
            String pctString = modelItem.getAbv() + "  -  ";
            pctString = pctString.length()==5?"":pctString;
            viewHolder.tvSecond.setText(pctString + modelItem.getBjcpCode() + "-" + modelItem.getStyleCode() + " - " + modelItem.getVendorAbbr() + " - " + modelItem.getTentSpace());
        }

        viewHolder.tvFirst.setTypeface(null, Typeface.NORMAL);

        viewHolder.tvDatabaseKey.setText(modelItem.getId() + "");


        // DEFINE ACTIVITY THAT HAPPENS WHEN ITEM IS CLICKED
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "setOnClickListener fired with view " + view); // view is RelativeLayout from list_item.xml
                Intent intent = new Intent(mContext, BeerSlideActivity.class);
                intent.putExtra(BeerSlideActivity.EXTRA_POSITION, layoutPosition);
                ((Activity)mContext).startActivityForResult(intent, RecyclerSqlbListActivity.DETAIL_REQUEST);

            }
        });

        // If the item is long-clicked, we want to change the icon in the model and in the database
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.v(TAG, "setOnLongClickListener fired with view " + view); // view is RelativeLayout from list_item.xml
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void setQueryText(String queryText) {
        this.mQueryText = queryText;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView viewToManage;
        TextView tvFirst;
        TextView tvSecond;
        TextView tvDatabaseKey;
        TextView descriptionTextView;

        ImageView glassSize; // DRS 20171128 - Menu scan
        TextView glassPrice; // DRS 20171128 - Menu scan

        ViewHolder(View view) {
            super(view);

            tvFirst = (TextView)view.findViewById(R.id.b_name);
            tvSecond = (TextView)view.findViewById(R.id.b_second_line);
            viewToManage = (ImageView)view.findViewById(R.id.b_icon);
            tvDatabaseKey = (TextView)view.findViewById(R.id.b_db_item);
            descriptionTextView = (TextView) itemView.findViewById(R.id.b_description);
            glassSize = (ImageView)view.findViewById(R.id.glass_icon); // DRS 20171128 - Menu scan
            glassPrice = (TextView)view.findViewById(R.id.b_price); // DRS 20171128 - Menu scan
        }



    }

}



