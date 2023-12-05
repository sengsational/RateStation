package com.sengsational.ratestation;

import static com.sengsational.ratestation.BeerSlideActivity.EXTRA_TUTORIAL_TYPE;
import static com.sengsational.ratestation.BeerSlideActivity.PREF_RATE_BEER_TUTORIAL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopTutorial extends AppCompatActivity {
    public static String TAG = PopTutorial.class.getName();

    public static final String EXTRA_TITLE_RESOURCE = "extraTitleResource";
    public static final String EXTRA_TEXT_RESOURCE = "extraTextResource";
    private PopTutorial mPopTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_tutorial);
        mPopTutorial = this;

        // Define size of pop-up window
        //DisplayMetrics dm = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(dm);
        //int width = (int)(dm.widthPixels * 0.8);
        //int height = (int)(dm.heightPixels * 0.6);
        //getWindow().setLayout(width,height);

        // Get the title and the text to display to the user from strings.xml
        int titleResource = (int)getIntent().getExtras().get(EXTRA_TITLE_RESOURCE);
        TextView title = (TextView)findViewById(R.id.tutorial_title);
        title.setText(Html.fromHtml(getResources().getString(titleResource)));

        int textResource = (int)getIntent().getExtras().get(EXTRA_TEXT_RESOURCE);
        TextView instructions = (TextView)findViewById(R.id.tutorial_instructions);
        instructions.setText(Html.fromHtml(getResources().getString(textResource)));

        // Depending on tutorial type, show and hide different things
        final String tutorialType = (String)getIntent().getExtras().get(EXTRA_TUTORIAL_TYPE);
        Log.v(TAG, "Tutorial type [" + tutorialType + "]");
        RelativeLayout iconLayout = (RelativeLayout)findViewById(R.id.icon_layout);
        TextView cancelTextView = (TextView)findViewById(R.id.cancel_pop);
        switch (tutorialType) {
            case PREF_RATE_BEER_TUTORIAL:
                iconLayout.setVisibility(View.GONE);
                cancelTextView.setVisibility(View.VISIBLE);
                break;
        }
        // Listen for a change to the display preference and edit preferences
        CheckBox checkboxResource = (CheckBox)findViewById(R.id.dont_show_checkbox);
        checkboxResource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean showTutorial =  PreferenceManager.getDefaultSharedPreferences(mPopTutorial).getBoolean(tutorialType, true);
                Log.v(TAG, "looked up " + tutorialType + " and got back " + showTutorial);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mPopTutorial);
                SharedPreferences.Editor prefsEdit = prefs.edit();
                prefsEdit.putBoolean(tutorialType, !isChecked);
                prefsEdit.apply();
                Log.v(TAG, "applied " + !isChecked + " to preferences.");
            }
        });

        // Listen for a click on the 'GOT IT' Text
        TextView gotItTextView = (TextView)findViewById(R.id.got_it);
        gotItTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        // Listen for a click on the 'CANCEL' Text
        TextView cancelledTextView = (TextView)findViewById(R.id.cancel_pop);
        cancelledTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.v(TAG, "onBackPressed fired");
        finish();
    }

}
