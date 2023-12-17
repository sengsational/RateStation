package com.sengsational.ratestation;

import static com.sengsational.ratestation.PopTutorial.EXTRA_TEXT_RESOURCE;
import static com.sengsational.ratestation.PopTutorial.EXTRA_TITLE_RESOURCE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Owner on 5/17/2016.
 */
public class BeerSlideActivity extends AppCompatActivity {
    private static final String TAG = BeerSlideActivity.class.getSimpleName();
    //private static Cursor mCursor; // DRS 20161130

    public static final int PINT_GLASS = 0;
    public static final int SNIFTER_GLASS = 1;
    public static final String EXTRA_GLASS_TYPE = "extra_glass_type";
    public static final String EXTRA_POSITION = "extra_position";
    public static final String EXTRA_FILENAME = "fileName";

    private static final int POST_PICTURE_INTENT = 1959;
    private static final int POST_HELP_TAKE_PICTURE_INTENT = 1957;
    private static final int POST_POSITION_INTENT = 1992;
    private static final int POST_HELP_RATE_BEER_INTENT = 1664;

    public static final String EXTRA_DB_KEY = "extra_db_key";
    public static final String PREF_TAKE_PICTURE_TUTORIAL = "takePictureTutorial";
    public static final String PREF_RATE_BEER_TUTORIAL = "rateBeerTutorial";
    public static final String EXTRA_TUTORIAL_TYPE = "tutorialType";
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1942;
    public static final String CURRENT_FILE_NAME_KEY = "mCurrentFileName";

    private String mCurrentFileName;
    private RelativeLayout mBeerLayout;
    private int mPosition = 0;
    private int mGlassType = PINT_GLASS;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentFileName = savedInstanceState.getString(CURRENT_FILE_NAME_KEY);
        }

        Log.v("sengsational", "BeerSlideActivity.onCreate().  position: " + (Integer) getIntent().getExtras().get(EXTRA_POSITION));

        // DRS 20161130 - Added 3
        try { mPosition = (Integer) getIntent().getExtras().get(EXTRA_POSITION); } catch (Throwable t) {Log.e(TAG, "unable to get " + EXTRA_POSITION);}
        //mCursor = UfoDatabaseAdapter.getCursor();
        if (RatstatApplication.getCursor("queryPackage", getApplicationContext()) != null) {
            Cursor cursor = RatstatApplication.getCursor("queryPackage", getApplicationContext());
            cursor.moveToPosition(mPosition);
        }

        setContentView(R.layout.activity_screen_slide);
        ViewPager mPager = (ViewPager)findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new BeerSlidePageAdapter(getSupportFragmentManager(), getApplicationContext());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mPosition);
        Log.v(TAG, "mPager current item: " + mPager.getCurrentItem());

        // DRS 20161130 firstVisiblePosition = (int) getIntent().getExtras().get(EXTRA_FIRST_VISIBLE_POSITION);

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CURRENT_FILE_NAME_KEY, mCurrentFileName);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.v("sengsational", "Item selected: " + item.getTitle());

        // Must set the current position in order for the right beer description page to be used
        ViewPager mPager = (ViewPager)findViewById(R.id.pager);
        mPosition = mPager.getCurrentItem();

        if (R.id.rate_beer == item.getItemId()) {
            boolean rateBeerTutorial =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_RATE_BEER_TUTORIAL, true);
            Log.v(TAG, "looked up " + PREF_RATE_BEER_TUTORIAL + " and got back " + rateBeerTutorial);
            if (rateBeerTutorial) {
                Intent popTutorialIntent = new Intent(BeerSlideActivity.this, PopTutorial.class);
                popTutorialIntent.putExtra(EXTRA_TEXT_RESOURCE, R.string.rate_beer_instructions);
                popTutorialIntent.putExtra(EXTRA_TITLE_RESOURCE, R.string.rate_beer_title);
                popTutorialIntent.putExtra(EXTRA_TUTORIAL_TYPE, PREF_RATE_BEER_TUTORIAL);
                startActivityForResult(popTutorialIntent, POST_HELP_RATE_BEER_INTENT);
            } else {
                onActivityResult(POST_HELP_RATE_BEER_INTENT, RESULT_OK, null);
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        switch (requestCode) {
            case POST_HELP_RATE_BEER_INTENT:
                //Intent rateBeerIntent = new Intent(BeerSlideActivity.this, RateBeerActivity.class);
                //r/teBeerIntent.putExtra(EXTRA_POSITION, mPosition);
                //startActivity(rateBeerIntent);
                break;
        } // switch
    }


    public static void showSnackBar(final Activity activity, int messageId, final String[] permissionStringArray) {
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, messageId, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(activity, permissionStringArray,MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        });
        View snackbarView = snackbar.getView();
        TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackTextView.setMaxLines(9);
        snackbar.show();
    }



    private static class BeerSlidePageAdapter extends FragmentStatePagerAdapter {

        int count;

        public BeerSlidePageAdapter(FragmentManager fm, Context applicationContext){
            super(fm);
            // DRS 20161130 - Commented 1, Added 1
            if (RatstatApplication.getCursor("queryPackage", applicationContext) != null) this.count = RatstatApplication.getCursor("queryPackage", applicationContext).getCount();
            else Log.v(TAG, "Cursor was null in BeerSlidePageAdapter.");
        }

        @Override
        public int getCount() {
            return this.count;
        }


        @Override
        public Fragment getItem(int position) {
            //Log.v("sengsational", "BeerSlideActivity.getItem with passed in position " + position);
            //return BeerSlideFragment.create(position);
            // DRS 20161130 - Commented 1, Added 1
            //return BeerSlideFragment.newInstance(position, firstVisiblePosition);
            return BeerSlideFragment.newInstance(position, R.layout.activity_beer);
        }

    }
}
