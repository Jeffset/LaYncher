package by.jeffset.layncher;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AppLauncherAdapter.AppActionListener {

   public static final String EXTRA_LAUNCHER_MODE = "by.jeffset.layncher.launcher_mode";
   public static final int MODE_STANDARD = 1;
   public static final int MODE_LARGE = 2;
   public static final String EXTRA_THEME = "by.jeffset.layncher.theme";

   public void onMenuDeleteClick(MenuItem item) {
      data.pendingRecentApps.remove(contextApp);
      data.recentApps.remove(contextApp);
      data.newApps.remove(contextApp);
      launcherAdapter.removeItem(contextApp);
      publishRecentAppsList();
      publishNewAppsList();
   }

   public void onMenuInfoClick(MenuItem item) {
      new AlertDialog.Builder(this)
          .setTitle(R.string.app_info_header)
          .setMessage(contextApp.label)
          .setPositiveButton(android.R.string.ok, null)
          .show();
   }

   public static class DataFragment extends Fragment {
      static final String FRAGMENT_TAG = "by.jeffset.layncher.MainActivity$DataFragment";

      public DataFragment() {
         setRetainInstance(true);
      }

      private Deque<App> newApps = new LinkedList<>();
      private Deque<App> recentApps = new LinkedList<>();
      private Deque<App> pendingRecentApps = new LinkedList<>();
   }

   @Override public void onCreateContextMenu(ContextMenu menu, View v,
                                             ContextMenu.ContextMenuInfo menuInfo) {
      App app = (App) v.getTag(AppLauncherAdapter.APP_TAG_KEY);
      getMenuInflater().inflate(R.menu.app_context_menu, menu);
      menu.setHeaderIcon(new BitmapDrawable(app.icon));
      menu.setHeaderTitle(app.label);
   }

   DataFragment data;
   App contextApp;
   private AppLauncherAdapter launcherAdapter;// = new AppLauncherAdapter(this);

   private ViewGroup newAppsStrip;
   private ViewGroup popularAppsStrip;
   private RecyclerView recyclerView;

   private int columnCount;

   private boolean wasJustPaused = false;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      setTheme(getIntent().getIntExtra(EXTRA_THEME, R.style.AppTheme_Light));
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      wasJustPaused = false;

      FragmentManager fm = getSupportFragmentManager();
      data = (DataFragment) fm.findFragmentByTag(DataFragment.FRAGMENT_TAG);
      if (data == null) {
         data = new DataFragment();
         fm.beginTransaction().add(data, DataFragment.FRAGMENT_TAG).commit();
      }

      int mode = getIntent().getIntExtra(EXTRA_LAUNCHER_MODE, MODE_STANDARD);
      switch (mode) {
         case MODE_STANDARD:
            columnCount = getResources().getInteger(R.integer.columnStandard);
            break;
         case MODE_LARGE:
            columnCount = getResources().getInteger(R.integer.columnLarge);
            break;
      }

      recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
      launcherAdapter = new AppLauncherAdapter(this);
      launcherAdapter.setListener(this);
      recyclerView.setAdapter(launcherAdapter);
      recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

      newAppsStrip = (ViewGroup) findViewById(R.id.newAppsBarStrip);
      popularAppsStrip = (ViewGroup) findViewById(R.id.popularAppsStrip);

      // get apps from 2nd row
      if (data.recentApps.isEmpty())
         for (int i = 0; i < columnCount; ++i) {
            App app = new App();
            launcherAdapter.getApp(app, columnCount + i);
            data.recentApps.add(app);
         }
      Random random = new Random(getTaskId());
      if (data.newApps.isEmpty())
         for (int i = 0; i < columnCount; i++) {
            App app = new App();
            launcherAdapter.getApp(app, random.nextInt(128));
            data.newApps.add(app);
         }
   }

   @Override protected void onResume() {
      super.onResume();
      if (wasJustPaused) {
         for (App app : data.pendingRecentApps) {
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
      for (App app : data.newApps) {
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
      for (App app : data.recentApps) {
         if (i++ == columnCount) // mru strip limit
            break;
         popularAppsStrip.addView(createAppView(app));
      }
   }

   @NonNull private View createAppView(App app) {
      View view = View.inflate(this, R.layout.app_icon, null);
      TextView text = (TextView) view.findViewById(android.R.id.text1);
      ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
          (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.weight = 1.0f;
      view.setLayoutParams(params);
      text.setText(app.label);
      icon.setImageBitmap(app.icon);
      view.setTag(AppLauncherAdapter.APP_TAG_KEY, app);
      return view;
   }

   @Override
   public void onAppClick(App app) {
      addPendingRecentList(app);
      Snackbar.make(recyclerView, getString(R.string.launched_msg, app.label), Snackbar.LENGTH_SHORT).show();
   }

   private void addPendingRecentList(App app) {
      data.pendingRecentApps.add(app);
      /*if (data.recentApps.contains(app))
         data.recentApps.remove(app);
      data.recentApps.addFirst(app);*/
   }

   @Override
   public void onAppLongClick(View view) {
      contextApp = (App) view.getTag(AppLauncherAdapter.APP_TAG_KEY);
      view.showContextMenu();
   }

   public void onAppClick(View view) {
      onAppClick((App) view.getTag(AppLauncherAdapter.APP_TAG_KEY));
   }
}
