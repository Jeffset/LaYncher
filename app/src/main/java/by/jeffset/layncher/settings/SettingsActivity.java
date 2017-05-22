package by.jeffset.layncher.settings;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;

import by.jeffset.data.SearchContract;
import by.jeffset.layncher.R;
import by.jeffset.layncher.data.AppsContract;
import by.jeffset.layncher.data.PhonesContract;
import by.jeffset.layncher.net.PhotoLoadingService;
import by.jeffset.layncher.net.PhotoLoadingService.PhotoServiceBinder;

public class SettingsActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener,
    PhotoLoadingService.ImageReadyListener {

   public static final int NEED_RELAUNCH = -100;
   private PhotoServiceBinder photoService;
   private ServiceConnection serviceConnection;
   private ImageSwitcher background;

   @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
      SettingsWrapper wrapper = new SettingsWrapper(this);
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      preferences.registerOnSharedPreferenceChangeListener(this);
      setTheme(wrapper.getThemeId());
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_settings);
      background = (ImageSwitcher) findViewById(android.R.id.background);
      background.setFactory(() -> {
         ImageView imageView = new ImageView(SettingsActivity.this);
         imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

         ViewGroup.LayoutParams params = new ImageSwitcher.LayoutParams(
             ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

         imageView.setLayoutParams(params);
         return imageView;
      });
      //PhotoLoadingService.setBackgroundImageAsync(this);
   }


   @Override protected void onStart() {
      super.onStart();
      Intent intent = new Intent(this, PhotoLoadingService.class);
      serviceConnection = new ServiceConnection() {
         @Override public void onServiceConnected(ComponentName name, IBinder service) {
            photoService = (PhotoServiceBinder) service;
            photoService.addListener(SettingsActivity.this);
            photoService.loadCurrentImage(SettingsActivity.this);
         }

         @Override public void onServiceDisconnected(ComponentName name) {
            photoService = null;
         }
      };
      bindService(intent, serviceConnection, BIND_AUTO_CREATE);
   }

   @Override protected void onStop() {
      photoService.removeListener(this);
      unbindService(serviceConnection);
      super.onStop();
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

   @Override public void onImageReady(@Nullable Bitmap bitmap) {
      if (bitmap != null)
         runOnUiThread(() -> background.setImageDrawable(new BitmapDrawable(bitmap)));
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
