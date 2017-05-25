package by.jeffset.layncher.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import by.jeffset.layncher.R;
import by.jeffset.layncher.welcome.WelcomeActivity;

/**
 * Created by marco on 26.4.17.
 * Simple settings wrapper and helper
 */

public class SettingsWrapper {

   private final Resources res;

   public int getThemeId() {
      String s = prefs.getString(res.getString(R.string.pref_theme_key),
          "1");
      return s.equals("1") ? R.style.AppTheme_Light : R.style.AppTheme_Dark;
   }

   public String getLayoutMode() {
      return prefs.getString(res.getString(R.string.pref_layout_key),
          STANDARD_MODE);
   }

   public int getHistoryLength() {
      return Integer.valueOf(prefs.getString(res.getString(R.string.pref_history_len_key),
          "10"));
   }

   public boolean isShowFaves() {
      return prefs.getBoolean(res.getString(R.string.pref_show_faves_key), true);
   }

   public boolean wasWelcomeShowed() {
      return prefs.getBoolean(WelcomeActivity.PREFS_WELCOME_SHOWED, false);
   }

   public void setWelcomeWasShowed() {
      prefs.edit().putBoolean(WelcomeActivity.PREFS_WELCOME_SHOWED, true).apply();
   }

   public void setThemeId(@StyleRes int themeId) {
      int value = themeId == R.style.AppTheme_Light ? 1 : 2;
      prefs.edit().putString(res.getString(R.string.pref_theme_key), String.valueOf(value)).apply();
   }

   public void setLayoutMode(String mode) {
      prefs.edit().putString(res.getString(R.string.pref_layout_key), mode).apply();
   }

   public int getImageUpdatePeriod() {
      return Integer.valueOf(prefs.getString(res.getString(R.string.pref_update_photo_delay), String.valueOf(900)));
   }

   public void setHistoryLength(int length) {
      prefs.edit().putString(res.getString(R.string.pref_history_len_key), String.valueOf(length)).apply();
   }

   public void setShowFaves(boolean show) {
      prefs.edit().putString(res.getString(R.string.pref_show_faves_key), String.valueOf(show)).apply();
   }

   public final String STANDARD_MODE;
   public final String LARGE_MODE;

   private final SharedPreferences prefs;

   public SettingsWrapper(@NonNull Context context) {
      res = context.getResources();
      STANDARD_MODE = res.getString(R.string.standard_layout_value);
      LARGE_MODE = res.getString(R.string.large_layout_value);
      prefs = PreferenceManager.getDefaultSharedPreferences(context);
   }
}
