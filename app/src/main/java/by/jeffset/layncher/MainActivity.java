package by.jeffset.layncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import by.jeffset.layncher.settings.SettingsWrapper;

public class MainActivity extends AppCompatActivity implements Launchable.AppListener {

   public void onMenuDeleteClick(MenuItem item) {
      data.pendingRecentApps.remove(contextApp);
      data.recentApps.remove(contextApp);
      data.newApps.remove(contextApp);
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


   public static class DataFragment extends Fragment {
      static final String FRAGMENT_TAG = "by.jeffset.layncher.MainActivity$DataFragment";

      public DataFragment() {
         setRetainInstance(true);
      }

      private Deque<InstalledApp> newApps = new LinkedList<>();
      private Deque<InstalledApp> recentApps = new LinkedList<>();
      private Deque<InstalledApp> pendingRecentApps = new LinkedList<>();
   }

   @Override public void onCreateContextMenu(ContextMenu menu, View v,
                                             ContextMenu.ContextMenuInfo menuInfo) {
      /*App app = (App) v.getTag(AppLauncherAdapter.APP_TAG_KEY);
      getMenuInflater().inflate(R.menu.app_context_menu, menu);
      menu.setHeaderIcon(new BitmapDrawable(app.icon));
      menu.setHeaderTitle(app.label);*/
   }

   DataFragment data;
   InstalledApp contextApp;
   private AppLauncherAdapter launcherAdapter;// = new AppLauncherAdapter(this);

   private ViewGroup newAppsStrip;
   private ViewGroup popularAppsStrip;
   private RecyclerView recyclerView;

   private int columnCount;

   private boolean wasJustPaused = false;

   private void populateAdapterWithInstalledApps() {
      // TODO implement background precomputing.
      Intent launchIntent = new Intent(Intent.ACTION_MAIN);
      launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      PackageManager pm = getPackageManager();
      List<ResolveInfo> intentActivities = pm.queryIntentActivities(launchIntent, 0);
      List<Launchable> apps = new ArrayList<>();
      apps.add(new SettingsLauncher(this));
      for (ResolveInfo info : intentActivities) {
         ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
         InstalledApp app = new InstalledApp(applicationInfo, pm, this);
         app.setListener(this);
         apps.add(app);
      }
      launcherAdapter.add(apps);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      SettingsWrapper settingsWrapper = new SettingsWrapper(this);

      setTheme(settingsWrapper.getThemeId());
      int a = settingsWrapper.getHistoryLength();
      super.onCreate(savedInstanceState);
      wasJustPaused = false;

      //Drawable wallpaper = WallpaperManager.getInstance(this).getDrawable();
      //getWindow().setBackgroundDrawable(wallpaper);
      setContentView(R.layout.activity_main);

      FragmentManager fm = getSupportFragmentManager();
      data = (DataFragment) fm.findFragmentByTag(DataFragment.FRAGMENT_TAG);
      if (data == null) {
         data = new DataFragment();
         fm.beginTransaction().add(data, DataFragment.FRAGMENT_TAG).commit();
      }

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
      if (wasJustPaused) {
         for (InstalledApp app : data.pendingRecentApps) {
            if (data.recentApps.contains(app))
               data.recentApps.remove(app);
            data.recentApps.addFirst(app);
         }
         data.pendingRecentApps.clear();
      }
      publishRecentAppsList();
      publishNewAppsList();
   }

   @Override protected void onPause() {
      super.onPause();
      wasJustPaused = true;
   }

   private void publishNewAppsList() {
      newAppsStrip.removeAllViews();
      int i = 0;
      for (InstalledApp app : data.newApps) {
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
      for (InstalledApp app : data.recentApps) {
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
      if (launchable instanceof InstalledApp)
         addPendingRecentList((InstalledApp) launchable);
   }

   @Override public void showMenu(Launchable launchable) {
      // TODO implement context menu logic
   }

   private void addPendingRecentList(InstalledApp app) {
      data.pendingRecentApps.add(app);
      /*if (data.recentApps.contains(app))
         data.recentApps.remove(app);
      data.recentApps.addFirst(app);*/
   }
}
