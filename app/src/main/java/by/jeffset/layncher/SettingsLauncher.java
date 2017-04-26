package by.jeffset.layncher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import by.jeffset.layncher.settings.SettingsActivity;

/**
 * Created by marco on 26.4.17.
 * Design
 */

public class SettingsLauncher implements Launchable {
   private Activity context;

   SettingsLauncher(Activity context) {this.context = context;}

   @Override public Drawable getIcon() {
      return ResourcesCompat.getDrawable
          (context.getResources(), R.drawable.icon, null);
   }

   @Override public CharSequence getLabel() {
      StyleSpan span = new StyleSpan(Typeface.BOLD);
      SpannableStringBuilder builder = new SpannableStringBuilder("Y Settings");
      builder.setSpan(span, 0, 10,
          Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      return builder;
   }

   @Override public boolean launch() {
      context.finish();
      context.startActivity(new Intent(context, SettingsActivity.class));
      return true;
   }

   @Override public boolean showContextMenu() {
      return false;
   }
}
