package com.sengsational.ratestation;

import static com.sengsational.ratestation.FestivalEvent.EVENT_HASH_PREF;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sengsational.ratestation.databinding.ActivityMainBinding;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DataView {
    private static final String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private StoreListPresenter storeListPresenter;
    private FestivalEvent mFestivalEvent;
    private ActivityResultLauncher<Intent> scanQrResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We can use this any time
        Log.v(TAG, "We can get ANDROID_ID when needed: " +  android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        dumpPrefs(prefs);

        // Get the intent that started this activity
        Intent intent = getIntent();
        // Get the URI from the intent
        Uri uri= intent.getData();
        // Get the original QR code data
        if (uri != null) {
            String data = uri.toString();
            // Extract the exercise id and use it
            String dataArgs = data.replace("ratstat://", "");
            Log.v(TAG, "dataArgs [" + dataArgs + "]");
        }

        // FIRST RUN, EVENT HASH WILL BE EMPTY --> GO OUT AND GET THE EVENT DATA FROM THE WEB
        String eventHash = prefs.getString(EVENT_HASH_PREF, "");
        Log.v(TAG, "Starting MainActivity with eventHash [" + eventHash + "]");
        if (eventHash.isEmpty()) {
            // Need to start web interaction to pull the data from the Event page.
            // Run Async Task (does not block).  Calls back on WebResultListener.onEventListSuccess().
            Log.v(TAG, ">>>>>>>>>>>> GOING OUT TO WEB FOR FESTIVAL EVENT <<<<<<<<<<<<<<<<<<<<<<");
            new FestivalEventInteractor().getEventDataFromWeb(this, new WebResultListener(this));
        } else {
            // We have a hash, so build the FestivalEvent object from preferences.
            mFestivalEvent = new FestivalEvent(prefs);
        }

        storeListPresenter = new StoreListPresenter(this); // Creates WebResultListener

        boolean wipeDatabaseForTesting = false;
        if (wipeDatabaseForTesting){
            Log.e(TAG, "ERASING EVERYTHING IN " + RatstatDatabaseAdapter.MAIN_TABLE +" FOR DEBUG");
            RatstatDatabaseAdapter databaseAdapter = new RatstatDatabaseAdapter() ;
            SQLiteDatabase db = databaseAdapter.openDb(this);                                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<OPENING DATABASE
            db.execSQL("delete from " + RatstatDatabaseAdapter.MAIN_TABLE);
            try {db.close();} catch (Throwable t){};                                //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<CLOSING DATABASE
        } else {
            RatstatDatabaseAdapter databaseAdapter = new RatstatDatabaseAdapter() ;
            SQLiteDatabase db = databaseAdapter.openDb(this);                                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<OPENING DATABASE
            Log.v(TAG, "Found database version " + db.getVersion());
            try {db.close();} catch (Throwable t){};                                //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<CLOSING DATABASE
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == IntentIntegrator.REQUEST_CODE)  {
            // This is after the user scanned the Touchless menu and we now need to USE the URL that was scanned
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if(result != null) {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Took a while, so we gave up", Toast.LENGTH_LONG).show();
                } else {
                    String barcodeContents = result.getContents();
                    String eventHash = mFestivalEvent.getEventHash() + "-";
                    if (barcodeContents.contains(eventHash)) {
                        String beerCode =  barcodeContents.split("-")[1];
                        try {
                            int beerNumber = Integer.parseInt(beerCode);
                            Log.v(TAG, "Got a parsable beer number: " + beerNumber);
                            // We now need to run "FIND" with search text of the beerNumber

                            String[] pullFields = StationItem.FIELDS_ALL;
                            String selectionFields = null;
                            String selectionArgs[] = null;
                            String orderBy = null;

                            StringBuilder selectionFieldsBuilder = new StringBuilder();
                            ArrayList<String> selectionArgsArray = new ArrayList<>();
                            // HERE IS WHERE WE COULD SET SELECTION CRITERIA, IF NEEDED

                            selectionFieldsBuilder.append(StationDbItem.BEER_CODE + " = ? ");
                            selectionArgsArray.add("" + beerNumber);

                            selectionFields = selectionFieldsBuilder.toString();
                            selectionArgs =  selectionArgsArray.toArray(new String[0]);

                            Intent beerList = null;
                            beerList = new Intent(this, RecyclerSqlbListActivity.class);
                            beerList.putExtra("pullFields", pullFields);
                            beerList.putExtra("selectionFields", selectionFields);
                            beerList.putExtra("selectionArgs", selectionArgs);
                            Log.v(TAG, "Going from MainActivity.onActivityResult() to RecyclerSqlbListActivity");
                            startActivity(beerList);

                        } catch (Throwable t){
                            Toast.makeText(this, "QR code doesn't appear to have a beer number: " + result.getContents(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "QR code doesn't appear to be for a beer: " + result.getContents(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, intent);
            }
        } else {
            Log.v(TAG, "Activity Result code " + resultCode + " not handled.");
        }
    }


    public void saveValidEvent(FestivalEvent discoveredFestivalEvent) {
        mFestivalEvent = discoveredFestivalEvent;
        mFestivalEvent.saveObjectToPreferences(this);
        Log.v(TAG, "FestivalEvent has hash " + mFestivalEvent.getEventHash());
        // Because we just got an event from the web, we need to refresh the data
        RatstatDatabaseAdapter databaseAdapter = new RatstatDatabaseAdapter() ;
        SQLiteDatabase db = databaseAdapter.openDb(this);                                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<OPENING DATABASE
        db.execSQL("delete from " + RatstatDatabaseAdapter.MAIN_TABLE);
        try {db.close();} catch (Throwable t){};                                //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<CLOSING DATABASE
        getStoreList();
    }

    public FestivalEvent getFestivalEvent() {
        return mFestivalEvent;
    }
    private void dumpPrefs(SharedPreferences prefs) {
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d(TAG, entry.getKey() + ": " + entry.getValue());
        }
    } // Private Helper Method

    public void clearEventHash(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(EVENT_HASH_PREF);
        editor.commit();
        // Restart the app
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        mainIntent.setPackage(getPackageName());
        startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    ///////////////////////////////////////////
    // IMPLEMENTS DataView
    ///////////////////////////////////////////


    @Override
    public void getStoreList() {
        Log.v(TAG, "getStoreList() running.");
        storeListPresenter = new StoreListPresenter(this); // Creates WebResultListener
        storeListPresenter.getStoreList();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void showProgress(boolean show) {

    }

    @Override
    public void showMessage(String message) {

    }

    @Override
    public void showMessage(String message, int toastLength) {

    }

    @Override
    public void setUsernameError(String message) {

    }

    @Override
    public void setPasswordError(String message) {

    }

    @Override
    public void saveValidCredentials(String authenticationName, String password, String savePassword, String mou, String storeNumber, String userName, String tastedCount) {

    }

    @Override
    public void saveValidStore(String storeNumber) {

    }

    @Override
    public void navigateToHome() {

    }

    @Override
    public void setStoreView(boolean resetPresentation) {

    }

    @Override
    public void setUserView() {

    }

    @Override
    public void showDialog(String message, long daysSinceQuiz) {

    }

}