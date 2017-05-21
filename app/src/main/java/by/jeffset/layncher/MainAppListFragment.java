package by.jeffset.layncher;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import by.jeffset.layncher.data.AppsContract;
import by.jeffset.layncher.settings.SettingsWrapper;


public class MainAppListFragment extends AppListFragment  {

   AppListAdapter launcherAdapter;
   RecyclerView recyclerView;

   private RecyclerView newAppsStrip;
   private RecyclerView popularAppsStrip;

   private AppBarLayout appBarLayout;

   private AppListAdapter popularAppsAdapter;
   private AppListAdapter newAppsAdapter;

   //===================================================

   @NonNull public static MainAppListFragment newInstance() {
      return new MainAppListFragment();
   }

   public MainAppListFragment() {}

   public static int obtainColumnCount(@NonNull Context context) {
      SettingsWrapper settingsWrapper = new SettingsWrapper(context);
      Resources res = context.getResources();
      if (settingsWrapper.getLayoutMode().equals(settingsWrapper.STANDARD_MODE))
         return res.getInteger(R.integer.columnStandard);
      else
         return res.getInteger(R.integer.columnLarge);
   }

   @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      Activity activity = getActivity();

      int columnCount = obtainColumnCount(activity);

      ContentResolver contentResolver = activity.getContentResolver();

      popularAppsStrip.setLayoutManager(new GridLayoutManager(activity, columnCount));
      Cursor popularQuery = contentResolver.query(AppsContract.APPS_URI, AppsContract.App.ALL,
          AppsContract.App.USAGE_TIME + "!=?", new String[]{"-1"},
          AppsContract.App.USAGE_TIME + " DESC LIMIT " + String.valueOf(columnCount));
      popularAppsAdapter = new AppListAdapter(activity, popularQuery);
      popularAppsStrip.setAdapter(popularAppsAdapter);


      newAppsStrip.setLayoutManager(new GridLayoutManager(activity, columnCount));
      Cursor newQuery = contentResolver.query(AppsContract.APPS_URI, AppsContract.App.ALL,
          null, null,
          AppsContract.App.MODIFICATION_TIME + " DESC LIMIT " + String.valueOf(columnCount));
      newAppsAdapter = new AppListAdapter(activity, newQuery);
      newAppsStrip.setAdapter(newAppsAdapter);


      recyclerView.setLayoutManager(new GridLayoutManager(activity, columnCount));

      Cursor query = contentResolver.query(AppsContract.APPS_URI, AppsContract.App.ALL,
          null, null, null);
      launcherAdapter = new AppListAdapter(activity, query);
      recyclerView.setAdapter(launcherAdapter);

      launcherAdapter.listener = this;
      newAppsAdapter.listener = this;
      popularAppsAdapter.listener = this;
   }

   @Override
   public View onCreateView
       (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_main_app_list, container, false);
      recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
      newAppsStrip = (RecyclerView) view.findViewById(R.id.newAppsBarStrip);
      popularAppsStrip = (RecyclerView) view.findViewById(R.id.popularAppsStrip);
      appBarLayout = (AppBarLayout) view.findViewById(R.id.appsLayoutContainer);

      return view;
   }


   @Override public void onResume() {
      super.onResume();
      new Handler().postDelayed(() -> {
         launcherAdapter.onResume();
         newAppsAdapter.onResume();
         popularAppsAdapter.onResume();
      }, 200);
   }

   @Override public void onPause() {
      super.onPause();
      launcherAdapter.onPause();
      newAppsAdapter.onPause();
      popularAppsAdapter.onPause();
   }

   @Override public void onClick(String packageName, String activityName, int appId) {
      popularAppsAdapter.onPause();
      super.onClick(packageName, activityName, appId);
   }


}
