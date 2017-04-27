package by.jeffset.layncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import static by.jeffset.layncher.MainActivity.TAG;

/**
 * Created by marco on 27.4.17.
 * Main class for keeping serialized data
 */

public class AppDataHelper extends Observable {
   private static final String SAVED_DATA_FILENAME = "savedData";
   private static final String SAVED_DATA_KEY = "savedData.key";
   private final AppCompatActivity context;

   private static class Data implements Serializable {
      private List<InstalledApp.AppIdInfo> allApps = new LinkedList<>();
      private List<InstalledApp.AppIdInfo> newApps = new LinkedList<>();
      private List<InstalledApp.AppIdInfo> recentApps = new LinkedList<>();
   }

   public static class DataFragment extends Fragment {
      static final String FRAGMENT_TAG = "by.jeffset.layncher.AppDataHelper$DataFragment";

      Data data;

      public DataFragment() {
         setRetainInstance(true);
      }

      @Override public void onAttach(Context context) {
         super.onAttach(context);
         Log.i(TAG, "onAttach: get fragment data");
         data = (Data) getArguments().getSerializable(SAVED_DATA_KEY);
      }
   }

   private DataFragment dataFragment;

   public AppDataHelper(AppCompatActivity context) {
      this.context = context;
      FragmentManager fm = context.getSupportFragmentManager();
      dataFragment = (DataFragment) fm.findFragmentByTag(DataFragment.FRAGMENT_TAG);
      if (dataFragment == null) {
         Log.i(TAG, "AppDataHelper: fragment is null");
         dataFragment = new DataFragment();
         Data data = fetchData();
         Bundle args = new Bundle();
         args.putSerializable(SAVED_DATA_KEY, data);
         dataFragment.data = data;
         dataFragment.setArguments(args);
         fm.beginTransaction().add(dataFragment, DataFragment.FRAGMENT_TAG).commit();
         saveData();
      }
   }

   public List<InstalledApp> getAllApps(InstalledApp.AppListener listener) {
      List<InstalledApp> apps = new LinkedList<>();
      for (InstalledApp.AppIdInfo info : dataFragment.data.allApps)
         try {
            InstalledApp app = new InstalledApp(context, info);
            app.setListener(listener);
            apps.add(app);
         } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
         }
      return apps;
   }

   public void setRecentApps(List<InstalledApp> recentApps) {
      dataFragment.data.recentApps.clear();
      for (InstalledApp app : recentApps)
         dataFragment.data.recentApps.add(app.appIdInfo);
   }

   public List<InstalledApp> getNewApps(InstalledApp.AppListener listener) {
      List<InstalledApp> apps = new LinkedList<>();
      for (InstalledApp.AppIdInfo info : dataFragment.data.newApps)
         try {
            InstalledApp app = new InstalledApp(context, info);
            app.setListener(listener);
            apps.add(app);
         } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
         }
      return apps;
   }

   public List<InstalledApp> getRecentApps(InstalledApp.AppListener listener) {
      List<InstalledApp> apps = new LinkedList<>();
      for (InstalledApp.AppIdInfo info : dataFragment.data.recentApps)
         try {
            InstalledApp app = new InstalledApp(context, info);
            app.setListener(listener);
            apps.add(app);
         } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
         }
      return apps;
   }

   private @NonNull Data initData() {
      Log.i(TAG, "initData: create new data");
      Intent launchIntent = new Intent(Intent.ACTION_MAIN);
      launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      Data data = new Data();
      PackageManager pm = context.getPackageManager();
      List<ResolveInfo> intentActivities = pm.queryIntentActivities(launchIntent, 0);
      for (ResolveInfo info : intentActivities) {
         ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
         data.allApps.add(new InstalledApp.AppIdInfo(applicationInfo));
      }
      return data;
   }

   private @NonNull Data fetchData() {
      Data data;
      Log.i(TAG, "fetchData: fetching data");
      File file = new File(context.getFilesDir(), SAVED_DATA_FILENAME);
      try {
         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
         data = (Data) ois.readObject();
      } catch (IOException | ClassNotFoundException e) {
         e.printStackTrace();
         data = initData();
      }
     /* final Data data2 = data;
      AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
         @Override protected Void doInBackground(Void... voids) {
            PackageManager pm = context.getPackageManager();
            for (InstalledApp.AppIdInfo info : data2.allApps) {
               try {
                  new InstalledApp(pm.getApplicationInfo(info.packageName,
                      PackageManager.GET_META_DATA), context);
               } catch (PackageManager.NameNotFoundException e) {
                  e.printStackTrace();
               }
            }
            return null;
         }

         @Override protected void onPostExecute(Void aVoid) {
            notifyObservers();
         }
      }.execute();*/
      return data;
   }

   void saveData() {
      File file = new File(context.getFilesDir(), SAVED_DATA_FILENAME);
      try {
         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
         oos.writeObject(dataFragment.data);
         oos.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
