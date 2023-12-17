package com.sengsational.ratestation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;


public class MycCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<MycCursorRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = MycCursorRecyclerViewAdapter.class.getSimpleName();
    private Context mContext;

    public MycCursorRecyclerViewAdapter(Context context, Cursor c) {
        super(c, true);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View modelView = inflater.inflate(R.layout.v_item, parent, false);
        return new ViewHolder(modelView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor aCursor) {
        final StationItem model = new StationItem(aCursor);
        final StationDbItem modelItem = model.getStationDbItem();
        final int layoutPosition = viewHolder.getLayoutPosition();
        viewHolder.beerName.setText(modelItem.getBeerName());
        viewHolder.tvDatabaseKey.setText(modelItem.getId() + ""); //My database key
        viewHolder.tvDatabaseExternalKey.setText(modelItem.getBeerCode()); // External database key
        viewHolder.voteCheckbox.setChecked(model.hasVote());
        viewHolder.ratingBar.setRating(model.getUserStarsNumber());

        // DEFINE ACTIVITY THAT HAPPENS WHEN ITEM IS CLICKED
        viewHolder.voteCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkedCount = RatstatDatabaseAdapter.countVotedItems(mContext);
                Log.v(TAG, "countVotedItems " + checkedCount);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                String maxVotesString = prefs.getString(FestivalEvent.EVENT_MAX_VOTES_PREF, "5");
                int maxVotesNumber = 5;
                try { maxVotesNumber = Integer.parseInt(maxVotesString);} catch (Throwable t) {}
                if (viewHolder.voteCheckbox.isChecked() && checkedCount >= maxVotesNumber) {
                    viewHolder.voteCheckbox.setChecked(false);
                    String text = "You may vote for a maximum of " + maxVotesString  + " items.";
                    SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
                    biggerText.setSpan(new RelativeSizeSpan(2.0f), 0, text.length(), 0);
                    Toast.makeText(mContext, biggerText, Toast.LENGTH_LONG).show();
                } else {
                    String databaseKey = (String) viewHolder.tvDatabaseKey.getText();
                    Log.v(TAG, "checkbox item clicked. Is checked:" + viewHolder.voteCheckbox.isChecked() + " model item key is " + modelItem.get_id() + " and this clicked item is " + databaseKey);
                    Log.e(TAG, "database item " + databaseKey);
                    model.setVote(viewHolder.voteCheckbox.isChecked());
                    RatstatDatabaseAdapter.update(model, mContext);
                }
            }
        });


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "setOnClickListener fired with view " + view); // view is RelativeLayout from list_item.xml
                Intent intent = new Intent(mContext, BeerSlideActivity.class);
                intent.putExtra(BeerSlideActivity.EXTRA_POSITION, layoutPosition);
                ((Activity)mContext).startActivityForResult(intent, RecyclerSqlcListActivity.DETAIL_REQUEST);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView beerName;
        CheckBox voteCheckbox;
        TextView tvDatabaseKey;
        TextView tvDatabaseExternalKey;

        ViewHolder(View view) {
            super(view);
            ratingBar = (RatingBar)view.findViewById(R.id.v_ratingBar);
            beerName = (TextView)view.findViewById(R.id.v_name);
            voteCheckbox = (CheckBox)view.findViewById(R.id.v_checkbox);
            tvDatabaseKey = (TextView)view.findViewById(R.id.v_db_item);
            tvDatabaseExternalKey = (TextView)view.findViewById(R.id.v_db_ext_item);
        }
    }
}



