package by.jeffset.layncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import by.jeffset.layncher.settings.SettingsWrapper;

public class MainActivity extends AppCompatActivity
    implements Launchable.AppListener, Observer {
   public static final String TAG = "LaYncher.AppDataHelper";

   public static final String PERSISTENT_DATA_KEY = "PERSISTENT_DATA";

   AppDataHelper helper;
   List<InstalledApp> recentApps = new LinkedList<>();
   List<InstalledApp> newApps = new LinkedList<>();

   public void onMenuDeleteClick(MenuItem item) {
      recentApps.remove(contextApp);
      newApps.remove(contextApp);
      launcherAdapter.remove(contextApp);
      publishRecentAppsList();
      publishNewAppsList();
   }

   public void onMenuInfoClick(MenuItem item) {
      new AlertDialog.Builder(this)
          .setTitle(R.string.app_info_header)
          .setMessage(contextApp.getLabel())
          .setPositiveButton(android.R.string.ok, null)
          .show();
   }

   @Override public void onCreateContextMenu(ContextMenu menu, View v,
                                             ContextMenu.ContextMenuInfo menuInfo) {
      /*App app = (App) v.getTag(AppLauncherAdapter.APP_TAG_KEY);
      getMenuInflater().inflate(R.menu.app_context_menu, menu);
      menu.setHeaderIcon(new BitmapDrawable(app.icon));
      menu.setHeaderTitle(app.label);*/
   }

   InstalledApp contextApp;
   private AppLauncherAdapter launcherAdapter;// = new AppLauncherAdapter(this);

   private ViewGroup newAppsStrip;
   private ViewGroup popularAppsStrip;
   private RecyclerView recyclerView;

   private int columnCount;


   private void populateAdapterWithInstalledApps() {
      launcherAdapter.clear();
      launcherAdapter.add(new SettingsLauncher(this));
      launcherAdapter.add(helper.getAllApps(this));
      recentApps = helper.getRecentApps(this);
      newApps = helper.getNewApps(this);
   }

   @Override public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      Log.i(TAG, "onSaveInstanceState: here");
      helper.saveData();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      SettingsWrapper settingsWrapper = new SettingsWrapper(this);

      setTheme(settingsWrapper.getThemeId());
      int a = settingsWrapper.getHistoryLength();


      super.onCreate(savedInstanceState);

      helper = new AppDataHelper(this);
      helper.addObserver(this);

      //Drawable wallpaper = WallpaperManager.getInstance(this).getDrawable();
      //getWindow().setBackgroundDrawable(wallpaper);
      setContentView(R.layout.activity_main);

      Resources res = getResources();
      if (settingsWrapper.getLayoutMode().equals(settingsWrapper.STANDARD_MODE))
         columnCount = res.getInteger(R.integer.columnStandard);
      else
         columnCount = res.getInteger(R.integer.columnLarge);

      recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
      launcherAdapter = new AppLauncherAdapter(this);
      recyclerView.setAdapter(launcherAdapter);
      recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

      newAppsStrip = (ViewGroup) findViewById(R.id.newAppsBarStrip);
      popularAppsStrip = (ViewGroup) findViewById(R.id.popularAppsStrip);

      populateAdapterWithInstalledApps();
      // get apps from 2nd row
      /*if (data.recentApps.isEmpty())
         for (int i = 0; i < columnCount; ++i) {
            App app = new App();
            launcherAdapter.getApp(app, columnCount + i);
            data.recentApps.add(app);
         }
      if (data.newApps.isEmpty())
         for (int i = 0; i < columnCount; i++) {
            App app = new App();
            launcherAdapter.getApp(app, random.nextInt(128));
            data.newApps.add(app);
         }*/
   }

   @Override protected void onResume() {
      super.onResume();
      /*if (wasJustPaused) {
         for (InstalledApp app : data.pendingRecentApps) {
            if (data.recentApps.contains(app))
               data.recentApps.remove(app);
            data.recentApps.addFirst(app);
         }
         data.pendingRecentApps.clear();
      }*/
      publishRecentAppsList();
      publishNewAppsList();
   }

   @Override protected void onPause() {
      super.onPause();
   }

   private void publishNewAppsList() {
      newAppsStrip.removeAllViews();
      int i = 0;
      for (InstalledApp app : newApps) {
         if (i++ == columnCount) // new strip limit
            break;
         View view = createAppView(app);
         TextView text = (TextView) view.findViewById(android.R.id.text1);
         text.setText(String.format(Locale.US, "%d (%s)", i, text.getText()));
         newAppsStrip.addView(view);
      }
   }

   private void publishRecentAppsList() {
      popularAppsStrip.removeAllViews();
      int i = 0;
      for (InstalledApp app : recentApps) {
         if (i++ == columnCount) // mru strip limit
            break;
         popularAppsStrip.addView(createAppView(app));
      }
   }

   @NonNull private View createAppView(InstalledApp app) {

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
          (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.weight = 1.0f;

      AppLauncherAdapter.AppViewHolder holder =
          new AppLauncherAdapter.AppViewHolder(this, null);
      holder.bind(app);
      View view = holder.itemView;
      view.setLayoutParams(params);
      view.setTag(AppLauncherAdapter.APP_TAG_KEY, app);
      return view;
   }

   @Override public void onLaunch(Launchable launchable) {
      if (launchable instanceof InstalledApp) {
         if (!recentApps.contains(launchable)) {
            recentApps.add((InstalledApp) launchable);
            helper.setRecentApps(recentApps);
         }
      }
   }

   @Override public void showMenu(Launchable launchable) {
      // TODO implement context menu logic
   }

   @Override public void update(Observable o, Object arg) {
      populateAdapterWithInstalledApps();
   }
}
