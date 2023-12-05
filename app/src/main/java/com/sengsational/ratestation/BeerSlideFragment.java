package com.sengsational.ratestation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;


/**
 * Created by Owner on 5/17/2016.
 */
public class BeerSlideFragment extends Fragment {
    private static final String TAG = BeerSlideFragment.class.getSimpleName();
    private static MybCursorRecyclerViewAdapter mCursorRecyclerViewAdapter;

    private static final String ARG_POSITION = "position";
    private static final String ARG_POSITION_FIRST_VISIBLE = "position_first_visible";
    private static final String ARG_LAYOUT_ID = "layout_id";
    private static boolean REFRESH_REQUIRED = false;
    private static int mListPosition = -1;
    private static int mLayoutId;
    public static final String PREF_DETAILS_TUTORIAL = "prefDetailsTutorial";
    private ViewGroup mRootView;
    private Cursor mCursor;
    private StationItem mModelItem;

    public static BeerSlideFragment create(int position, int firstVisible) {
        BeerSlideFragment fragment = new BeerSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_POSITION_FIRST_VISIBLE, firstVisible);
        Log.v("sengsational", "create first visible : " + firstVisible);
        fragment.setArguments(args);
        return fragment;
    }

    public static BeerSlideFragment newInstance(int position, int layoutId) {
        Log.v(TAG, "newInstance 1");
        mLayoutId = layoutId;
        //mCursor = cursor; // DRS 20161201 - Commented 1 - Cursors only in application class
        BeerSlideFragment fragment = new BeerSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_LAYOUT_ID, layoutId);
        fragment.setArguments(args);
        return fragment;
    }

    public static BeerSlideFragment newInstance(int position, int firstVisible , int unusedConstructor) {
        Log.v(TAG, "newInstance 2");
        BeerSlideFragment fragment = new BeerSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_POSITION_FIRST_VISIBLE, firstVisible);
        Log.v("sengsational", "new instance first visible : " + firstVisible);
        fragment.setArguments(args);
        REFRESH_REQUIRED = false;
        return fragment;
    }

    public BeerSlideFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Log.v("sengsational", "BeerSlideFragment.onCreate() ARG_POSITION position: " + getArguments().getInt(ARG_POSITION));
        if (savedInstanceState != null) {
            mLayoutId = savedInstanceState.getInt(ARG_LAYOUT_ID);
        }

        if (mCursorRecyclerViewAdapter == null) {
            mCursor = RatstatApplication.getCursor(getActivity());
            mCursorRecyclerViewAdapter = new MybCursorRecyclerViewAdapter(getActivity(), mCursor, false, false); //Pull cursor from the application class
            mCursorRecyclerViewAdapter.hasStableIds();
        }
        if (RatstatApplication.getTutorialLock()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean showTutorial =  prefs.getBoolean(PREF_DETAILS_TUTORIAL, true);
            //Log.v(TAG, "showTutorial was " + showTutorial);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG, "onActivityCreated() running.");
        Object activity = getActivity();
        if (activity != null ) {
            getActivity().setTitle("Rate Station");
        }
        if (savedInstanceState != null){
            int layoutIdFromSave = savedInstanceState.getInt(ARG_LAYOUT_ID, -1);
            if (layoutIdFromSave > 0) mLayoutId = layoutIdFromSave;
            else Log.v(TAG, "mLayoutId was " + mLayoutId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView() with mLayoutId " + mLayoutId);
        ViewGroup aViewGroup = null;
        try {
            aViewGroup = (ViewGroup) inflater.inflate(mLayoutId, container, false); // occasionally throws android.content.res.Resources$NotFoundException
            if (aViewGroup == null) {
                int layoutId = savedInstanceState.getInt(ARG_LAYOUT_ID);
                if (layoutId == 0) throw new Exception("Could not get the layoutId from the savedInstanceState");
                aViewGroup = (ViewGroup) inflater.inflate(layoutId, container, false);
            }
        } catch (Exception e) {
            Log.v("sengsational", "Could not define the rootView.  This is bad.  mLayoutId was " + mLayoutId + " Error: " + e.getMessage());
        }

        if (aViewGroup == null) aViewGroup = container; // Total BS...  I don't know what to do if this happens.

        final ViewGroup rootView = aViewGroup;
        mRootView = rootView;

        mListPosition = getArguments().getInt(ARG_POSITION);
        mCursor = RatstatApplication.getCursor(getContext());
        mCursor.moveToPosition(mListPosition);
        mModelItem = new StationItem(mCursor);
        final StationDbItem modelDbItem = mModelItem.getStationDbItem();

        ((TextView)rootView.findViewById(R.id.database_key)).setText(modelDbItem.getId());
        String beerName = modelDbItem.getBeerName();
        ((TextView)rootView.findViewById(R.id.beername)).setText(beerName);
        boolean includeBrewerName = false;
        ((TextView)rootView.findViewById(R.id.description)).setText(mModelItem.getNarrativeDescription(includeBrewerName));
        ((TextView)rootView.findViewById(R.id.abv)).setText(modelDbItem.getAbv());
        ((TextView)rootView.findViewById(R.id.city)).setText(modelDbItem.getTentSpace());
        ((TextView)rootView.findViewById(R.id.style)).setTypeface(null, Typeface.BOLD_ITALIC);
        ((TextView)rootView.findViewById(R.id.style)).setText(modelDbItem.getStyleCode());
        ((TextView)rootView.findViewById(R.id.created)).setText(modelDbItem.getVendorName() + " (" + modelDbItem.getVendorAbbr() +")");
        ((TextView)rootView.findViewById(R.id.new_arrival)).setText(modelDbItem.getBjcpCategory() + " (" + modelDbItem.getBjcpCode() + ")");
        ((TextView)rootView.findViewById(R.id.glass_price_details)).setText("#" + modelDbItem.getBeerCode());
        ((TextView)rootView.findViewById(R.id.local_db_key)).setText("" + modelDbItem.get_id());

        ImageView highlighted = (ImageView)rootView.findViewById(R.id.highlighted);

        final Context context = this.getContext();
        TextView description = (TextView)rootView.findViewById(R.id.description);
        description.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "BeerSlideFragment description long click.");
                boolean longClickConsumed = true;
                return longClickConsumed;
            }
        });

        TextView beerNameTextView = (TextView)rootView.findViewById(R.id.beername);
        beerNameTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "BeerSlideFragment beerName long click.");
                boolean longClickConsumed = true;
                return longClickConsumed;
            }
        });

        ////////////////////////////////////////////////////////////
        // RATINGS
        ///////////////////////////////////////////////////////////

        RatingBar mRatingBar = (RatingBar)rootView.findViewById(R.id.ratingBar);
        Button doneRatingButton = (Button)rootView.findViewById(R.id.done_rating_button);
        TextInputEditText ratingTextView = (TextInputEditText)rootView.findViewById(R.id.ratingEditText);
        Button clearRatingButton = (Button)rootView.findViewById(R.id.clear_rating_button);

        // Ratings Text
        String existingText = mModelItem.getUserReview();
        if (existingText != null && !"null".equals(existingText)) {
            Log.v(TAG, "onCreateView() modelItem supplied existingText for rating " + existingText);
            ratingTextView.setText(existingText);
        }

        // Ratings Bar for stars
        String starCountString = mModelItem.getUserStars();
        if (starCountString != null) {
            float starCount = -1f;
            Log.v(TAG, "onCreateView() modelItem supplied starCountString " + starCountString);
            try { starCount = Float.parseFloat(starCountString); } catch (Throwable t) {/*ignore*/ }
            if (starCount > 0) {
                mRatingBar.setRating(starCount);
            }
        }
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Log.v(TAG, "float " + v);
                mRatingBar.setRating(v);
                Log.v(TAG, "rating changed " + ratingBar.getRating() + " v:" + v + " b:"  + b);

            }
        });

        // Done Button
        doneRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update model item with data from the UI
                float ratingFloat = mRatingBar.getRating();
                String ratingText = Objects.requireNonNull(ratingTextView.getText()).toString();
                mModelItem.setUserReview(ratingText);
                mModelItem.setUserStars("" + ratingFloat);

                // Save data will be done in onPause()

                // Press "back"
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });

        // Clear Button
        clearRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean[] clearFields = {false};
                if (Objects.requireNonNull(ratingTextView.getText()).toString().length() > 10) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("Remove Rating and Comments?");
                    builder.setMessage("Do you want to erase your comments and rating?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            clearFields[0] = true;
                            ratingTextView.setText("");
                            mRatingBar.setRating(0);
                            // Update Model
                            mModelItem.setUserReview("");
                            mModelItem.setUserStars("0.0");
                            // Save now because on pause doesn't do empty stuff
                            RatstatDatabaseAdapter.update(mModelItem, mListPosition, getContext());
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.show();
                } else {
                    clearFields[0] = true;
                    ratingTextView.setText("");
                    mRatingBar.setRating(0);
                    // Update Model
                    mModelItem.setUserReview("");
                    mModelItem.setUserStars("0.0");
                    // Save now because on pause doesn't do empty stuff
                    RatstatDatabaseAdapter.update(mModelItem, mListPosition, getContext());
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause(){
        super.onPause();

        // Ascertain which database item we are talking about
        TextView itemFromView = (TextView)mRootView.findViewById(R.id.local_db_key);
        String localDatabaseKeyString = "-1";
        int databaseKey = -1;
        if (itemFromView != null) {
            localDatabaseKeyString = itemFromView.getText().toString();
            try {
                databaseKey = Integer.parseInt(localDatabaseKeyString);
            } catch (Throwable t) {
                Log.e(TAG, "Unable to parse database key.");
            }
        } else {
            Log.e(TAG, "itemFromView was not populated!!");
        }

        // backup the cursor up to 3 records
        for (int i = 0; i < 4 ; i++) {
            if (mCursor.isFirst()) break;
            mCursor.moveToPrevious();
        }

        // advance to find this key
        boolean cursorAligned = false;

        int columnIndexForKey =  mCursor.getColumnIndexOrThrow("_id");
        for (int i = 0; i < 10; i++) {
            int databaseKeyFound = mCursor.getInt(columnIndexForKey);
            if (databaseKey == databaseKeyFound) {
                //Log.v(TAG, "Cursor positioned properly.");
                cursorAligned = true;
                break;
            } else {
                //Log.v(TAG, databaseKeyFound + " not equal to " + databaseKey);
            }
            mCursor.moveToNext();
        }

        if (cursorAligned) {
            // Get ratings from UI
            float ratingFloat = ((RatingBar)mRootView.findViewById(R.id.ratingBar)).getRating();
            String ratingText = ((TextInputEditText)mRootView.findViewById(R.id.ratingEditText)).getText().toString();

            // Construct a model item from accurate cursor
            mModelItem = new StationItem(mCursor);

            // If data found, update model item
            boolean updateNeeded = false;
            if (ratingText != null && ratingText.length() >0) {
                mModelItem.setUserReview(ratingText);
                updateNeeded = true;
            }
            if (ratingFloat > 0) {
                mModelItem.setUserStars("" + ratingFloat);
                updateNeeded = true;
            }

            // Save model item to database
            if (updateNeeded) {
                RatstatDatabaseAdapter.update(mModelItem, mListPosition, getContext());
                //RatstatApplication.reQuery(getContext());

                Log.v(TAG, "onPause updated _id " + localDatabaseKeyString);
            } else {
                //Log.v(TAG, "onPause had no data so did not update _id " + localDatabaseKeyString);
            }
        } else {
            //Log.v(TAG, "onPause cursor not aligned. Doing nothing for _id " + localDatabaseKeyString);

        }
        //RatstatApplication.reQuery(getContext());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mLayoutId > 0) outState.putInt(ARG_LAYOUT_ID, mLayoutId);
        super.onSaveInstanceState(outState);
    }

    public void toggleFavorite(int databaseKey, View rootView, boolean refreshRequired) {
        REFRESH_REQUIRED = refreshRequired;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        //Create a cursor to read the one record by database _id, passed here by EXTRA_ID
        try {
            RatstatDatabaseAdapter ufoDatabaseAdapter = new RatstatDatabaseAdapter() ;
            db = ufoDatabaseAdapter.openDb(getActivity());                                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<OPENING DATABASE
            // DRS 20160809 - Added another highlight state "X" for 'unavailable'
            String highlightState = "F";
            cursor = db.query("UFO", new String[] {"HIGHLIGHTED"}, "_id = ?", new String[] {Integer.toString(databaseKey)}, null, null, null);
            if (cursor.moveToFirst()) {
                highlightState = cursor.getString(0);
            }
            cursor.close();
            Log.v("sengsational", "Highlighted state: " + highlightState);
            ImageView viewToManage = (ImageView)rootView.findViewById(R.id.highlighted);
            if (viewToManage == null) viewToManage = (ImageView)rootView.findViewById(R.id.highlighted_list_item);
            updateHighlightState(viewToManage, highlightState, db, databaseKey, this.getContext());

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(getContext(), "Database unavailable. "+ e.getMessage(), Toast.LENGTH_LONG)   ;
        } finally {
            try {cursor.close();} catch (Throwable t) {}
            try {db.close();} catch (Throwable t) {}
            }
    }

    private static void updateHighlightState(ImageView viewToManage, String highlightState, SQLiteDatabase db, int databaseKey, Context context) {
        if (highlightState == null) highlightState = "F";
        switch (highlightState) {
            case "F":
                highlightState = "T";
                break;
            case "T":
                highlightState = "X";
                break;
            case "X":
                highlightState = "F";
            break;
        }
        db.execSQL("update UFO set HIGHLIGHTED='" + highlightState + "' where _id = " + databaseKey);
    }



}
