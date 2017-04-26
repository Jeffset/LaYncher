package by.jeffset.layncher.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import by.jeffset.layncher.MainActivity;
import by.jeffset.layncher.R;

/**
 * Created by marco on 26.4.17.
 * dsdsdsdsd
 */

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
   @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
      SettingsWrapper wrapper = new SettingsWrapper(this);
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      preferences.registerOnSharedPreferenceChangeListener(this);
      setTheme(wrapper.getThemeId());
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_settings);
   }

   @Override public void onBackPressed() {
      super.onBackPressed();
      finish();
      startActivity(new Intent(this, MainActivity.class));

   }

   public void onClearSearchHistory(View view) {}

   public void onClearFavouriteApps(View view) {}

   @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals(getString(R.string.pref_theme_key))) {
         finish();
         startActivity(new Intent(this, getClass()));
      }

   }

   public static class FavouriteAppsFragment extends PreferenceFragment {
      @Override public void onCreate(@Nullable Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.settings_fave_apps);
      }
   }
}
