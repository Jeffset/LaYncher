package by.jeffset.layncher.settings;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import by.jeffset.data.SearchContract;
import by.jeffset.layncher.R;
import by.jeffset.layncher.data.AppsContract;
import by.jeffset.layncher.data.PhonesContract;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

   public static final int NEED_RELAUNCH = -100;

   @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
      SettingsWrapper wrapper = new SettingsWrapper(this);
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      preferences.registerOnSharedPreferenceChangeListener(this);
      setTheme(wrapper.getThemeId());
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_settings);
      //PhotoLoadingService.setBackgroundImageAsync(this);
   }


   public void onClearSearchHistory(View view) {
      int count = getContentResolver().delete(SearchContract.ALL_URI, null, null);
      //DbHelper.resetUriHistory(database);
      Toast.makeText(this, "Cleared " + count + " item(s)", Toast.LENGTH_SHORT).show();
   }

   public void onClearFavouriteApps(View view) {
      ContentValues values = new ContentValues();
      values.put(AppsContract.App.IS_FAVOURITE, false);
      int apps = getContentResolver().update(AppsContract.APPS_URI, values,
          AppsContract.App.IS_FAVOURITE + " != 0", null);
      int phones = getContentResolver().delete(PhonesContract.PHONES_URI, null, null);
      Toast.makeText(this,
          String.format("Cleared %d apps and %d phones", apps, phones), Toast.LENGTH_SHORT).show();
   }

   @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals(getString(R.string.pref_theme_key)) ||
          key.equals(getString(R.string.pref_layout_key)) ||
          key.equals(getString(R.string.pref_show_faves_key))) {
         setResult(NEED_RELAUNCH);
         if (key.equals(getString(R.string.pref_theme_key)))
            finish();
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
