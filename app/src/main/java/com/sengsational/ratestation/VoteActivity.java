package com.sengsational.ratestation;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.app.PendingIntent.getActivity;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import static com.sengsational.ratestation.PopTutorial.EXTRA_TEXT_RESOURCE;
import static com.sengsational.ratestation.PopTutorial.EXTRA_TITLE_RESOURCE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sengsational.ratestation.databinding.ActivityVoteBinding;

public class VoteActivity extends AppCompatActivity implements DataView {
    private static final String TAG = VoteActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1959;
    private static final int CALLBACK = 42;
    private ActivityVoteBinding binding;
    FestivalEvent mFestivalEvent = new FestivalEvent(); // Populated in onCreate(), used in determining valid voting location.
    private View mProgressView;
    private String mError;
    private String mMessage;
    private AlertDialog mEndDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        binding = ActivityVoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get FestivalEvent from extras
        final Object objReceived = ((ObjectWrapper)getIntent().getExtras().getBinder("EVENT_EXTRA")).getData();
        Log.d(TAG, "received object=" + objReceived);
        if (objReceived instanceof FestivalEvent) {
            mFestivalEvent = (FestivalEvent) objReceived;
        }

        boolean alreadyGranted = ContextCompat.checkSelfPermission(VoteActivity.this, ACCESS_COARSE_LOCATION)==PERMISSION_GRANTED?true:false;
        Toast.makeText(VoteActivity.this, "Button clicked. alreadyGranted " + alreadyGranted, Toast.LENGTH_SHORT).show();
        if (!alreadyGranted) {
            presentAlert(getString(R.string.location_permission_education), getString(R.string.location_permission_title), VoteActivity.this);
        } else {
            checkLocation(true);
        }

        mProgressView = findViewById(R.id.top_progress);
    }
    private void presentAlert(String dialogMessage, String dialogTitle, VoteActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.checkLocation(true);
            }
        });
        builder.setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.checkLocation(false);
            }
        });

        builder.setMessage(dialogMessage).setTitle(dialogTitle).create().show();
    }

    private void checkLocation(boolean allow) {
        if (allow) {
            Toast.makeText(this, "We " + (allow?"will":"will not") + " check location.", Toast.LENGTH_SHORT).show();
            requestPermissions( new String[] {ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(this, "We " + (allow?"will":"will not") + " check location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    SingleShotLocationProvider.requestSingleUpdate(this, new SingleShotLocationProvider.LocationCallback() {
                        @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                            Log.d(TAG, "device location is " + location.longitude + ", " + location.latitude);
                            if (mFestivalEvent.isWithinRadius(location.latitude, location.longitude)) {

                                Toast.makeText(VoteActivity.this, mFestivalEvent.getVoteRadiusMessage(location.latitude, location.longitude), Toast.LENGTH_LONG).show();

                                showProgress(true);
                                VoteSubmitInteractor voteSubmitInteractor = new VoteSubmitInteractor();
                                voteSubmitInteractor.pushDataToWeb(VoteActivity.this, new WebResultListener(VoteActivity.this), VoteActivity.this);

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(VoteActivity.this);
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                builder.setMessage(mFestivalEvent.getVoteRadiusMessage(location.latitude, location.longitude)).setTitle("So Far Away").create().show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "NO VOTES WILL BE SUBMITTED", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.v("sengsational", "VoteActivity.onActivityResult running with requestCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode == VoteActivity.CALLBACK) {
            Log.v(TAG, "onActivityResult fired <<<<<<<<<< resultCode:" + resultCode);
        } else {
            String intentName = "not available";
            if (intent != null) {
                intentName = intent.getClass().getName();
            }
            Log.v(TAG, "onActivityresult with " + requestCode + ", " + resultCode + ", " + intentName);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mEndDialog != null) {
            mEndDialog.dismiss();
            mEndDialog = null;
        }
    }

    /// BELOW: IMPLEMENTS DATA VIEW
    @Override
    public void getStoreList() {}
    @Override
    public Context getContext() {return this;}
    @Override
    public void showProgress(boolean show) {
        if (show) {
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            mProgressView.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy() {super.onDestroy();}
    @Override
    public void showMessage(String message) {
        mMessage = message;
    }
    @Override
    public void showMessage(String message, int toastLength) {}
    @Override
    public void setUsernameError(String message) {
        mError = message;
    }
    @Override
    public void setPasswordError(String message) {}
    @Override
    public void saveValidCredentials(String authenticationName, String password, String savePassword, String mou, String storeNumber, String userName, String tastedCount) {}
    @Override
    public void saveValidStore(String storeNumber) {}
    @Override
    public void navigateToHome() {
        AlertDialog.Builder endDialog = new AlertDialog.Builder(this);
        String dialogTitle = (mError != null)?"Problem Saving":"Input Saved";

        endDialog.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mEndDialog.dismiss();
                Intent beginAgain = new Intent(VoteActivity.this, MainActivity.class);
                startActivity(beginAgain);
            }
        });
        mEndDialog = endDialog.setMessage(mMessage).setTitle(dialogTitle).create();
        mEndDialog.show();
    }
    @Override
    public void setStoreView(boolean resetPresentation) {}
    @Override
    public void setUserView() {}
    @Override
    public void showDialog(String message, long daysSinceQuiz) {}
}

