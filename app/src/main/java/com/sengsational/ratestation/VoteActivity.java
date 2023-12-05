package com.sengsational.ratestation;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.app.PendingIntent.getActivity;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sengsational.ratestation.databinding.ActivityVoteBinding;

public class VoteActivity extends AppCompatActivity {
    private static final String TAG = VoteActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1959;
    private ActivityVoteBinding binding;
    FestivalEvent mFestivalEvent = new FestivalEvent(); // Populated in onCreate(), used in determining valid voting location.

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

        final Button voteButton = binding.buttonVote;
        voteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean alreadyGranted = ContextCompat.checkSelfPermission(VoteActivity.this, ACCESS_COARSE_LOCATION)==PERMISSION_GRANTED?true:false;
                Toast.makeText(VoteActivity.this, "Button clicked. alreadyGranted " + alreadyGranted, Toast.LENGTH_SHORT).show();
                if (!alreadyGranted) {
                    presentAlert(getString(R.string.location_permission_education), getString(R.string.location_permission_title), VoteActivity.this);
                } else {
                    checkLocation(true);
                }
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
        });
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
                                Toast.makeText(VoteActivity.this, "Votes will be counted!!", Toast.LENGTH_SHORT).show();

                                // Need to start a listener

                                // Need to start a background process here to submit the votes to the server.

                                // Need to present a spinner

                                // Listener needs to wait for success or timeout and present results to the user.

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(VoteActivity.this);
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                builder.setMessage("It looks like you're not at the venue right now (" + String.format("%d",(long)mFestivalEvent.getMilesFromCenter(location.latitude, location.longitude)) +" miles away).  Voting is for people that are on-site.").setTitle("So Far Away").create().show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "NO VOTES WILL BE SUBMITTED", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}

