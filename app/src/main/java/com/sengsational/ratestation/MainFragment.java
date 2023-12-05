package com.sengsational.ratestation;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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
                // HERE IS WHERE WE COULD SET SELECTION CRITERIA, IF NEEDED

                // Do not show beer number 42 (just to put something in the selection fields).
                selectionFieldsBuilder.append(StationDbItem.EVENT_BEER_ID + " <> ? ");
                selectionArgsArray.add("42");

                selectionFields = selectionFieldsBuilder.toString();
                selectionArgs =  selectionArgsArray.toArray(new String[0]);

                Intent beerList = null;
                Log.v("sengsational", "TRYING RECYCLER VIEW"); //<<<<<<<<<<<<<<<<<<<<<<<<<<<SWAP-OUT TECHNIQUE
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
                NavHostFragment.findNavController(MainFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        binding.buttonRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Not implemented", Toast.LENGTH_SHORT).show();
            }
        });
        binding.buttonVotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Bundle bundle = new Bundle();
                bundle.putBinder("EVENT_EXTRA", new ObjectWrapper(festivalEvent));
                startActivity(new Intent(MainFragment.this.getActivity(), VoteActivity.class).putExtras(bundle));
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