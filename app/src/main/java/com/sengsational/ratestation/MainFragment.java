package com.sengsational.ratestation;

import static android.app.Activity.RESULT_OK;
import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.sengsational.ratestation.databinding.FragmentMainBinding;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getSimpleName();
    private FragmentMainBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState ) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Object festivalEvent = ((MainActivity)getActivity()).getFestivalEvent();

        binding.buttonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "buttonFind pressed: Starting ListActivity");

                String[] pullFields = StationItem.FIELDS_ALL;
                String selectionFields = null;
                String selectionArgs[] = null;
                String orderBy = null;

                StringBuilder selectionFieldsBuilder = new StringBuilder();
                ArrayList<String> selectionArgsArray = new ArrayList<>();

                selectionFields = selectionFieldsBuilder.toString();
                selectionArgs =  selectionArgsArray.toArray(new String[0]);

                Intent beerList = null;
                Log.v(TAG, "TRYING RECYCLER VIEW"); //<<<<<<<<<<<<<<<<<<<<<<<<<<<SWAP-OUT TECHNIQUE
                beerList = new Intent(MainFragment.this.getActivity(), RecyclerSqlbListActivity.class);
                beerList.putExtra("pullFields", pullFields);
                beerList.putExtra("selectionFields", selectionFields);
                beerList.putExtra("selectionArgs", selectionArgs);
                Log.v("sengsational", "going to RecyclerSqlbListActivity");
                startActivity(beerList);
            }
        });
        binding.buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainFragment.this.getActivity());
                integrator.setPrompt("Scan the beer's QR code");
                integrator.setTimeout(8000);
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
            }
        });
        binding.buttonRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] pullFields = StationItem.FIELDS_ALL;
                String selectionFields = null;
                String selectionArgs[] = null;
                String orderBy = null;

                StringBuilder selectionFieldsBuilder = new StringBuilder();
                ArrayList<String> selectionArgsArray = new ArrayList<>();

                selectionFields = selectionFieldsBuilder.toString();
                selectionArgs =  selectionArgsArray.toArray(new String[0]);

                Intent rateList = null;
                Log.v(TAG, "TRYING RECYCLER VIEW"); //<<<<<<<<<<<<<<<<<<<<<<<<<<<SWAP-OUT TECHNIQUE
                rateList = new Intent(MainFragment.this.getActivity(), RecyclerSqlcListActivity.class);
                rateList.putExtra("pullFields", pullFields);
                rateList.putExtra("selectionFields", selectionFields);
                rateList.putExtra("selectionArgs", selectionArgs);
                final Bundle bundle = new Bundle();
                bundle.putBinder("EVENT_EXTRA", new ObjectWrapper(festivalEvent));
                rateList.putExtras(bundle);
                Log.v("sengsational", "going to RecyclerSqlcListActivity");
                startActivity(rateList);
            }
        });
        binding.buttonDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainFragment.this.getActivity(), AndroidDatabaseManager.class));
            }
        });
        binding.buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainFragment.this.getActivity(), SettingsActivity.class));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}