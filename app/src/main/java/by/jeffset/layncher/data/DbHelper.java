package by.jeffset.layncher.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import by.jeffset.layncher.InstalledApp;

import static by.jeffset.layncher.MainActivity.TAG;

/**
 * Created by marco on 27.4.17.
 * Main LaYncher database
 */

public class DbHelper extends SQLiteOpenHelper {

   private static final int DATABASE_VERSION = 50;
   private static final String DATABASE_NAME = "laYncher.db";
   private final Context context;

   private final static String[] queryAllAppsProjection = new String[]{
       Contract.AppEntry.COL_NAME_PACKAGE_NAME,
       Contract.AppEntry.COL_NAME_ACTIVITY_NAME,
       Contract.AppEntry.COL_NAME_LABEL,
       Contract.AppEntry.COL_NAME_MODIFICATION_TIME,
       Contract.AppEntry.COL_NAME_USAGE_TIME,
       Contract.AppEntry.COL_NAME_IS_FAVOURITE,
       //Contract.AppEntry.COL_NAME_SOURCE_DIR,
   };

   public static List<AppEntry> queryAllApps(@NonNull SQLiteDatabase db) {
      List<AppEntry> allApps = new ArrayList<>();
      Cursor cursor = db.query(Contract.AppEntry.TABLE_NAME, queryAllAppsProjection,
          null, null, null, null, null);
      try {
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
            AppEntry entry = new AppEntry();
            entry.packageName = cursor.getString(cursor.getColumnIndex(Contract.AppEntry.COL_NAME_PACKAGE_NAME));
            entry.activityName = cursor.getString(cursor.getColumnIndex(Contract.AppEntry.COL_NAME_ACTIVITY_NAME));
            entry.label = cursor.getString(cursor.getColumnIndex(Contract.AppEntry.COL_NAME_LABEL));
            entry.modificationTime = cursor.getLong(cursor.getColumnIndex(Contract.AppEntry.COL_NAME_MODIFICATION_TIME));
            entry.usageTime = cursor.getLong(cursor.getColumnIndex(Contract.AppEntry.COL_NAME_USAGE_TIME));
            entry.isFavourite = cursor.getInt(cursor.getColumnIndex(Contract.AppEntry.COL_NAME_IS_FAVOURITE)) != 0;
            allApps.add(entry);
            cursor.moveToNext();
         }
         return allApps;
      } finally {
         cursor.close();
      }
   }

   public static void updateUsageTime(@NonNull SQLiteDatabase database, @NonNull AppEntry entry) {
      ContentValues values = new ContentValues();
      values.put(Contract.AppEntry.COL_NAME_USAGE_TIME, System.currentTimeMillis());
      String where = Contract.AppEntry.COL_NAME_PACKAGE_NAME + " == ? AND " +
          Contract.AppEntry.COL_NAME_ACTIVITY_NAME + " == ?";
      String[] whereArgs = new String[]{entry.packageName, entry.activityName};
      database.update(Contract.AppEntry.TABLE_NAME, values, where, whereArgs);
      Log.i(TAG, "updateUsageTime: updating");
   }

   public static void updateSetFavourite(@NonNull SQLiteDatabase database, @NonNull AppEntry entry) {
      ContentValues values = new ContentValues();
      values.put(Contract.AppEntry.COL_NAME_IS_FAVOURITE, true);
      String where = Contract.AppEntry.COL_NAME_PACKAGE_NAME + " == ? AND " +
          Contract.AppEntry.COL_NAME_ACTIVITY_NAME + " == ?";
      String[] whereArgs = new String[]{entry.packageName, entry.activityName};
      database.update(Contract.AppEntry.TABLE_NAME, values, where, whereArgs);
      Log.i(TAG, "updateSetFavourite: updating");
   }

   public static void updateResetAllFavourite(@NonNull SQLiteDatabase database) {
      ContentValues values = new ContentValues();
      values.put(Contract.AppEntry.COL_NAME_IS_FAVOURITE, false);
      database.update(Contract.AppEntry.TABLE_NAME, values, null, null);
      Log.i(TAG, "updateResetAllFavourite: updating");
   }

   public static Cursor queryUriHistory(@NonNull SQLiteDatabase database) {
      return database.query(Contract.UriEntry.TABLE_NAME,
          new String[]{
              Contract.UriEntry._ID,
              Contract.UriEntry.COL_NAME_URI},
          null, null, null, null, null);
   }

   public static void insertUriIntoHistory(@NonNull SQLiteDatabase database, String uri) {
      ContentValues values = new ContentValues();
      values.put(Contract.UriEntry.COL_NAME_URI, uri);
      database.insert(Contract.UriEntry.TABLE_NAME, null, values);
   }

   public static void resetUriHistory(@NonNull SQLiteDatabase database) {
      database.execSQL(Contract.DROP_URIS_SCRIPT);
      database.execSQL(Contract.CREATE_URIS_SCRIPT);
   }

   public DbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      this.context = context;
   }

   @Override public void onCreate(SQLiteDatabase db) {
      Log.i(TAG, "onCreate: database create...");
      db.execSQL(Contract.CREATE_APPS_SCRIPT);
      db.execSQL(Contract.CREATE_URIS_SCRIPT);
      //
      List<AppEntry> entries = fetchAndProcessApplications();
      for (AppEntry entry : entries) {
         ContentValues values = new ContentValues();
         values.put(Contract.AppEntry.COL_NAME_PACKAGE_NAME, entry.packageName);
         values.put(Contract.AppEntry.COL_NAME_ACTIVITY_NAME, entry.activityName);
         values.put(Contract.AppEntry.COL_NAME_LABEL, entry.label);
         values.put(Contract.AppEntry.COL_NAME_MODIFICATION_TIME, entry.modificationTime);
         values.put(Contract.AppEntry.COL_NAME_USAGE_TIME, entry.usageTime);
         values.put(Contract.AppEntry.COL_NAME_IS_FAVOURITE, entry.isFavourite);
         values.put(Contract.AppEntry.COL_NAME_SOURCE_DIR, entry.sourceDir);
         db.insert(Contract.AppEntry.TABLE_NAME, null, values);
      }
   }

   @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL(Contract.DROP_APPS_SCRIPT);
      db.execSQL(Contract.DROP_URIS_SCRIPT);
      onCreate(db);
   }


   // really long operation
   private List<AppEntry> fetchAndProcessApplications() {
      Log.i(TAG, "fetchAndProcessApplications: fetch");
      Intent launchIntent = new Intent(Intent.ACTION_MAIN);
      launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      PackageManager pm = context.getPackageManager();
      List<ResolveInfo> resolveInfoList =
          pm.queryIntentActivities(launchIntent, PackageManager.GET_META_DATA);
      List<AppEntry> entries = new ArrayList<>(resolveInfoList.size());
      for (ResolveInfo info : resolveInfoList) {
         ActivityInfo activityInfo = info.activityInfo;
         ApplicationInfo applicationInfo = activityInfo.applicationInfo;
         AppEntry entry = new AppEntry();
         entry.packageName = activityInfo.packageName;
         entry.activityName = activityInfo.name;
         entry.label = activityInfo.loadLabel(pm).toString();
         entry.modificationTime = new File(applicationInfo.sourceDir).lastModified();
         entry.sourceDir = applicationInfo.sourceDir;
         entry.usageTime = -1;
         entry.isFavourite = false;
         Drawable iconOriginal = null;
         try {
            ComponentName activityName = new ComponentName(activityInfo.packageName, activityInfo.name);
            final Resources res = pm.getResourcesForActivity(activityName);
            iconOriginal = ResourcesCompat.getDrawable(res, applicationInfo.icon, null);
         } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            e.printStackTrace();
         }
         if (iconOriginal == null)
            iconOriginal = pm.getApplicationIcon(applicationInfo);
         Bitmap icon = InstalledApp.decorateIcon(iconOriginal, context);
         File savedIcon = new File(context.getFilesDir(), entry.toString());
         try {
            icon.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(savedIcon));
         } catch (IOException e) {
            e.printStackTrace();
         }
         entries.add(entry);
      }
      return entries;
   }

   /**
    * Created by marco on 27.4.17.
    * Contract for database
    */

   public static final class Contract {
      public static abstract class AppEntry implements BaseColumns {

         public static final String TABLE_NAME = "apps";

         public static final String COL_NAME_PACKAGE_NAME = "packageName";
         public static final String COL_NAME_ACTIVITY_NAME = "activityName";
         public static final String COL_NAME_LABEL = "label";
         public static final String COL_NAME_MODIFICATION_TIME = "modTime";
         public static final String COL_NAME_USAGE_TIME = "usageTime";
         public static final String COL_NAME_IS_FAVOURITE = "favouriteFlag";
         public static final String COL_NAME_SOURCE_DIR = "sourceDir";
      }

      public static abstract class UriEntry implements BaseColumns {
         public static final String TABLE_NAME = "search";
         public static final String COL_NAME_URI = "value";
      }

      static final String CREATE_APPS_SCRIPT =
          "CREATE TABLE IF NOT EXISTS " + AppEntry.TABLE_NAME + " (" +
              AppEntry._ID + " INTEGER PRIMARY KEY, " +
              AppEntry.COL_NAME_PACKAGE_NAME + " TEXT, " +
              AppEntry.COL_NAME_ACTIVITY_NAME + " TEXT, " +
              AppEntry.COL_NAME_LABEL + " TEXT, " +
              AppEntry.COL_NAME_MODIFICATION_TIME + " INTEGER, " +
              AppEntry.COL_NAME_USAGE_TIME + " INTEGER, " +
              AppEntry.COL_NAME_IS_FAVOURITE + " INTEGER, " +
              AppEntry.COL_NAME_SOURCE_DIR + " TEXT)";
      static final String DROP_APPS_SCRIPT = "DROP TABLE IF EXISTS " + AppEntry.TABLE_NAME;
      static final String CREATE_URIS_SCRIPT =
          "CREATE TABLE IF NOT EXISTS " + UriEntry.TABLE_NAME + " (" +

              UriEntry._ID + " INTEGER PRIMARY KEY, " +
              UriEntry.COL_NAME_URI + " TEXT)";

      static final String DROP_URIS_SCRIPT = "DROP TABLE IF EXISTS " + UriEntry.TABLE_NAME;
   }
}
