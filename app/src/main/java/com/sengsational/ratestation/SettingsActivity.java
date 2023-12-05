package com.sengsational.ratestation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.SummaryProvider {
    private static String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        Log.v(TAG, "onSharedPreferencesChanged. [" + key + "]");
        String darkModeString = getString(R.string.dark_mode);
        Log.v(TAG, "onSharedPreferencesChanged. darkModeString [" + darkModeString + "]");
        String currentSetting = sharedPreferences.getString(getString(R.string.dark_mode), "");
        Log.v(TAG, "onSharedPreferencesChanged. currentSetting [" + currentSetting + "]");

        String[] darkModeValues = getResources().getStringArray(R.array.dark_mode_values);
        String[] darkModeEntries = getResources().getStringArray(R.array.dark_mode_entries);
    }

    @Override
    public CharSequence provideSummary(Preference preference) {
        Log.v(TAG, "provideSummary");

        if (preference != null && preference.getKey().equals(getString(R.string.dark_mode))) {
            if (preference instanceof ListPreference) {
                Log.v(TAG, "provideSummary working");
                return ((ListPreference)preference).getEntry();
            } else {
                return "Unknown Preference";
            }
        } else {
            return "Unknown Preference";
        }
    }


}