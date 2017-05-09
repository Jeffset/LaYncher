package by.jeffset.layncher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import by.jeffset.layncher.MainActivity;
import by.jeffset.layncher.R;
import by.jeffset.layncher.data.AppsContract;
import by.jeffset.layncher.data.DbHelper;
import by.jeffset.layncher.data.SearchContract;

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
      Activity parent = getParent();
      if (parent instanceof MainActivity) {
         parent.finish();
         startActivity(new Intent(this, MainActivity.class));
         overridePendingTransition(0, 0);
      }
   }

   public void onClearSearchHistory(View view) {
      SQLiteDatabase database = new DbHelper(this).getWritableDatabase();
      int count = getContentResolver().delete(SearchContract.ALL_URI, null, null);
      //DbHelper.resetUriHistory(database);
      Toast.makeText(this, "Cleared " + count + " item(s)", Toast.LENGTH_SHORT).show();
   }

   public void onClearFavouriteApps(View view) {
      ContentValues values = new ContentValues();
      values.put(AppsContract.App.IS_FAVOURITE, false);
      getContentResolver().update(AppsContract.APPS_URI, values, null, null);
      Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show();
   }

   @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals(getString(R.string.pref_theme_key))) {
         finish();
         overridePendingTransition(0, 0);
         startActivity(getIntent());
         overridePendingTransition(0, 0);
      }
   }

   public static class FavouriteAppsFragment
       extends PreferenceFragment {
      public static Fragment newInstance() {return new FavouriteAppsFragment();}

      @Override public void onCreate(@Nullable Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.settings_fave_apps);
      }
   }
}
