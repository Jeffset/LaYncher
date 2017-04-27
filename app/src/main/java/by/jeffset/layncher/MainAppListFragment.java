package by.jeffset.layncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import by.jeffset.layncher.data.AppEntry;
import by.jeffset.layncher.data.DbHelper;
import by.jeffset.layncher.settings.SettingsWrapper;


public class MainAppListFragment extends AppList {

   private SQLiteDatabase database;

   private List<InstalledApp> recentApps = new LinkedList<>();
   private List<InstalledApp> newApps = new LinkedList<>();

   private ViewGroup newAppsStrip;
   private ViewGroup popularAppsStrip;

   private int columnCount;

   InstalledApp contextApp;

   //===================================================

   private void populateAdapterWithInstalledApps() {
      launcherAdapter.clear();
      newApps.clear();
      recentApps.clear();
      Activity activity = getActivity();
      launcherAdapter.add(new SettingsLauncher(activity));
      List<AppEntry> appEntries = DbHelper.queryAllApps(database);
      List<InstalledApp> apps = new ArrayList<>(appEntries.size());
      for (AppEntry entry : appEntries)
         apps.add(new InstalledApp(entry, activity, this));
      //noinspection ComparatorCombinators, Java8ListSort
      Collections.sort(appEntries,
          (o1, o2) -> new Date(o2.modificationTime).compareTo(new Date(o1.modificationTime)));
      for (int i = 0; i < columnCount; i++)
         newApps.add(new InstalledApp(appEntries.get(i), activity, this));
      //noinspection ComparatorCombinators, Java8ListSort
      Collections.sort(appEntries,
          (o1, o2) -> new Date(o2.usageTime).compareTo(new Date(o1.usageTime)));
      for (int i = 0; i < columnCount; i++) {
         AppEntry entry = appEntries.get(i);
         if (entry.usageTime > 0)
            recentApps.add(new InstalledApp(entry, activity, this));
      }

      launcherAdapter.add(apps);
   }

   private void publishNewAppsList() {
      newAppsStrip.removeAllViews();
      int i = 0;
      for (InstalledApp app : newApps) {
         if (i++ == columnCount) // new strip limit
            break;
         View view = createAppView(app);
         TextView text = (TextView) view.findViewById(android.R.id.text1);
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
          new AppLauncherAdapter.AppViewHolder(getActivity(), null);
      holder.bind(app);
      View view = holder.itemView;
      view.setLayoutParams(params);
      view.setTag(AppLauncherAdapter.APP_TAG_KEY, app);
      return view;
   }
   //===================================================


   @NonNull public static MainAppListFragment newInstance() {
      return new MainAppListFragment();
   }

   public MainAppListFragment() {}

   @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      database = new DbHelper(getActivity()).getWritableDatabase();

      SettingsWrapper settingsWrapper = new SettingsWrapper(getActivity());

      Resources res = getResources();
      if (settingsWrapper.getLayoutMode().equals(settingsWrapper.STANDARD_MODE))
         columnCount = res.getInteger(R.integer.columnStandard);
      else
         columnCount = res.getInteger(R.integer.columnLarge);

      recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));

      populateAdapterWithInstalledApps();
   }

   @Override
   public View onCreateView
       (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_main_app_list, container, false);
      newAppsStrip = (ViewGroup) view.findViewById(R.id.newAppsBarStrip);
      popularAppsStrip = (ViewGroup) view.findViewById(R.id.popularAppsStrip);
      return view;
   }


   @Override public void onResume() {
      super.onResume();
      publishRecentAppsList();
      publishNewAppsList();
   }

   @Override public void onLaunch(Launchable launchable) {
      if (launchable instanceof InstalledApp) {
         DbHelper.updateUsageTime(database, ((InstalledApp) launchable).data);
         populateAdapterWithInstalledApps();
      }
      /*if (launchable instanceof InstalledApp) {
         if (!recentApps.contains(launchable)) {
            recentApps.add((InstalledApp) launchable);

            //helper.setRecentApps(recentApps);
         }
      }*/
   }

   @Override public void showMenu(Launchable launchable) {
      if (!(launchable instanceof InstalledApp)) return;
      contextApp = (InstalledApp) launchable;
      Activity activity = getActivity();
      new AlertDialog.Builder(activity)
          .setItems(new String[]{"Info", "Uninstall", "Add to favourites"}, (dialog, which) -> {
             switch (which) {
                case 0: { // SHOW INFO
                   Intent intent = new Intent();
                   intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                   Uri uri = Uri.fromParts("package",
                       contextApp.data.packageName, null);
                   intent.setData(uri);
                   startActivity(intent);
                   break;
                }
                case 1: { // UNINSTALL
                   Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package",
                       contextApp.data.packageName, null));
                   startActivity(intent);
                   break;
                }
                case 2: { // ADD TO FAVOURITES
                   DbHelper.updateSetFavourite(database, contextApp.data);
                   if (activity instanceof FavouriteAppsFragment.FragmentNotifier) {
                      ((FavouriteAppsFragment.FragmentNotifier) activity).notifyFragment();
                   }
                   break;
                }
             }
          })
          .setIcon(launchable.getIcon())
          .create()
          .show();
   }
}
