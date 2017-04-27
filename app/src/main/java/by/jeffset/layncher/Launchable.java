package by.jeffset.layncher;

import android.graphics.drawable.Drawable;

/**
 * Created by marco on 25.4.17.
 * Primary interface for presentation in laYncher
 */

public interface Launchable {

   interface AppListener {
      void onLaunch(Launchable launchable);

      void showMenu(Launchable launchable);
   }

   Drawable getIcon();

   CharSequence getLabel();

   boolean launch();

   boolean showContextMenu();
}
