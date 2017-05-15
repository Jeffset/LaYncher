package by.jeffset.layncher.data;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import by.jeffset.layncher.R;


public class AppProcessorService extends Service {

   public static final String JOB_TYPE_EXTRA = "by.jeffset.service.jobType";
   public static final String PACKAGE_NAME_EXTRA = "by.jeffset.service.packageName";
   public static final int PROCESS_ALL = 0;
   public static final int PROCESS_PACKAGE = 1;
   private static final int NO_JOB = -1;
   private static final String TAG = "LaY-Service";
   public static final String[] PROJECTION = {
       AppsContract.App._ID,
       AppsContract.App.PACKAGE_NAME,
       AppsContract.App.MODIFICATION_TIME,
       AppsContract.App.IS_FAVOURITE,
       AppsContract.App.USAGE_TIME,
   };
   public static final String FINISHED = "LaY-apps-processing-finished";
   public static final String PROGRESS = "LaY-apps-processing-progress";
   public static final String PROGRESS_VALUE_EXTRA = "by.jeffset.service.progress";

   private ServiceHandler handler;

   private int currentOperation = NO_JOB;

   private static class ExistingEntry {
      int id;
      long modTime;
      long usageTime;
      boolean isFavourite;

      ExistingEntry(int id, long modTime, long usageTime, boolean isFavourite) {
         this.id = id;
         this.modTime = modTime;
         this.usageTime = usageTime;
         this.isFavourite = isFavourite;
      }
   }

   private class ServiceHandler extends Handler {
      ServiceHandler(Looper looper) {
         super(looper);
      }

      @Override
      public void handleMessage(Message msg) {
         Log.i(TAG, "handleMessage: start handling message");
         currentOperation = msg.getData().getInt(JOB_TYPE_EXTRA, PROCESS_ALL);
         String packageName = msg.getData().getString(PACKAGE_NAME_EXTRA);
         Log.i(TAG, "handleMessage: packageName = " + packageName);

         Cursor cursor;
         if (packageName == null) cursor = getContentResolver().query(AppsContract.APPS_URI, PROJECTION,
             null, null, null);
         else cursor = getContentResolver().query(AppsContract.APPS_URI, PROJECTION,
             AppsContract.App.PACKAGE_NAME + " = ?", new String[]{packageName}, null);

         Log.i(TAG, "handleMessage: cursor queried with count = " + cursor.getCount());

         Map<String, ExistingEntry> existingData = new HashMap<>(cursor.getCount());
         Set<String> existingPackages = new TreeSet<>();
         int pnI = cursor.getColumnIndex(AppsContract.App.PACKAGE_NAME);
         int mtI = cursor.getColumnIndex(AppsContract.App.MODIFICATION_TIME);
         int utI = cursor.getColumnIndex(AppsContract.App.USAGE_TIME);
         int ifI = cursor.getColumnIndex(AppsContract.App.IS_FAVOURITE);
         int idI = cursor.getColumnIndex(AppsContract.App._ID);
         if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
               String pName = cursor.getString(pnI);
               existingData.put(pName,
                   new ExistingEntry(
                       cursor.getInt(idI),
                       cursor.getLong(mtI),
                       cursor.getLong(utI),
                       cursor.getInt(ifI) != 0));
               existingPackages.add(pName);
               cursor.moveToNext();
            }
         }
         cursor.close();

         LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(AppProcessorService.this);
         PackageManager pm = getPackageManager();
         Intent launchIntent = new Intent(Intent.ACTION_MAIN);
         launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
         List<ResolveInfo> resolveInfoList =
             pm.queryIntentActivities(launchIntent, PackageManager.GET_META_DATA);
         int i = 1;
         for (ResolveInfo info : resolveInfoList) {
            existingPackages.remove(info.activityInfo.packageName);
            if (info.activityInfo.packageName.equals(getPackageName()))
               continue;
            if (packageName != null && !info.activityInfo.packageName.equals(packageName)) {
               Log.i(TAG, "handleMessage: entry skipped due to packageName");
               continue;
            }
            ActivityInfo activityInfo = info.activityInfo;
            ApplicationInfo applicationInfo = activityInfo.applicationInfo;
            long lastModified = new File(applicationInfo.sourceDir).lastModified();

            ExistingEntry existingEntry = existingData.get(activityInfo.packageName);
            if (existingEntry != null && existingEntry.modTime == lastModified) {
               Log.i(TAG, String.format("handleMessage: entry already up to date {%s.%s} ",
                   activityInfo.packageName, activityInfo.name));
               continue;
            }

            ContentValues values = new ContentValues();
            values.put(AppsContract.App.PACKAGE_NAME, activityInfo.packageName);
            values.put(AppsContract.App.ACTIVITY_NAME, activityInfo.name);
            values.put(AppsContract.App.LABEL, activityInfo.loadLabel(pm).toString());
            values.put(AppsContract.App.SOURCE_DIR, applicationInfo.sourceDir);
            values.put(AppsContract.App.MODIFICATION_TIME, lastModified);
            if (existingEntry == null) {
               values.put(AppsContract.App.USAGE_TIME, -1);
               values.put(AppsContract.App.IS_FAVOURITE, false);
            } else {
               values.put(AppsContract.App.USAGE_TIME, existingEntry.usageTime);
               values.put(AppsContract.App.IS_FAVOURITE, existingEntry.isFavourite);
            }
            renderIcon(activityInfo, getIconFileName(activityInfo.packageName, activityInfo.name));
            if (existingEntry == null) {
               Log.i(TAG, String.format("handleMessage: insert new entry {%s.%s}",
                   activityInfo.packageName, activityInfo.name));
               getContentResolver().insert(AppsContract.APPS_URI, values);
            } else {
               Log.i(TAG, String.format("handleMessage: update entry {%s.%s}",
                   activityInfo.packageName, activityInfo.name));
               getContentResolver().update(AppsContract.APPS_URI, values,
                   AppsContract.App._ID + "=" + existingEntry.id, null);
            }

            Intent intent = new Intent(PROGRESS);
            intent.putExtra(PROGRESS_VALUE_EXTRA, ++i * 100 / resolveInfoList.size());
            broadcastManager.sendBroadcast(intent);
         }

         for (String removedPackageName : existingPackages) {
            getContentResolver().delete(AppsContract.APPS_URI, AppsContract.App.PACKAGE_NAME + " = ?",
                new String[]{removedPackageName});
         }

         Log.i(TAG, "handleMessage: finishing work");
         Intent intent = new Intent(FINISHED);
         broadcastManager.sendBroadcast(intent);
         stopSelf(msg.arg1);
         currentOperation = NO_JOB;
         Log.i(TAG, "handleMessage: service finished");
      }
   }

   @Override
   public void onCreate() {
      Log.i(TAG, "onCreate: create service");
      HandlerThread thread = new HandlerThread("ServiceStartArguments",
          Process.THREAD_PRIORITY_MORE_FAVORABLE);
      thread.start();
      handler = new ServiceHandler(thread.getLooper());
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.i(TAG, "onStartCommand: start command");
      if (currentOperation == NO_JOB || currentOperation == PROCESS_PACKAGE) {
         Log.i(TAG, "onStartCommand: adding command to queue");
         Message msg = handler.obtainMessage();
         msg.arg1 = startId;
         msg.setData(intent.getExtras());
         handler.sendMessage(msg);
      }
      return START_STICKY;
   }

   @Override @Nullable public IBinder onBind(Intent intent) {return null;}

   //=====================================================================

   public static IntentFilter getBroadcastIntentFilter() {
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(FINISHED);
      intentFilter.addAction(PROGRESS);
      return intentFilter;
   }

   private void renderIcon(@NonNull ActivityInfo activityInfo, @NonNull String name) {
      PackageManager pm = getPackageManager();
      Drawable iconOriginal = null;
      ComponentName activityName = new ComponentName(activityInfo.packageName, activityInfo.name);
      try {
         final Resources res = pm.getResourcesForActivity(activityName);
         iconOriginal = ResourcesCompat.getDrawable(res, activityInfo.icon, null);
      } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
         //e.printStackTrace();
      }
      try {
         if (iconOriginal == null) {
            final Resources res = pm.getResourcesForApplication(activityInfo.applicationInfo);
            iconOriginal = ResourcesCompat.getDrawable(res, activityInfo.applicationInfo.icon, null);
         }
      } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
         //e.printStackTrace();
      }
      try {
         if (iconOriginal == null)
            iconOriginal = pm.getApplicationIcon(activityInfo.packageName);
      } catch (PackageManager.NameNotFoundException e) {
         //e.printStackTrace();
         iconOriginal = ResourcesCompat.getDrawable(getResources(), R.drawable.icon5, null);
      }
      Bitmap icon = decorateIcon(iconOriginal, getResources(), true);
      File savedIcon = new File(getFilesDir(), name);
      try {
         icon.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(savedIcon));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static String getIconFileName(@NonNull String packageName, @NonNull String activityName) {
      return String.format("%s.%s", packageName, activityName);
   }

   private static Bitmap mask = null;

   @NonNull public static Bitmap decorateIcon(@NonNull Drawable original, Resources resources, boolean usePalette) {
      int size = resources.getDimensionPixelSize(R.dimen.iconSize);
      Path clippingPath = new Path();
      RectF clip = new RectF(0.f, 0.f, size, size);
      clippingPath.addRoundRect(clip, size / 4.f, size / 4.f, Path.Direction.CW);

      if (mask == null) {
         mask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
         Canvas maskCanvas = new Canvas(mask);
         Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         pathPaint.setStyle(Paint.Style.FILL);
         pathPaint.setColor(Color.WHITE);
         maskCanvas.drawPath(clippingPath, pathPaint);
      }

      Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
      Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(output);
      int backgroundFill = Color.BLACK;
      if (usePalette && original instanceof BitmapDrawable) {
         Palette palette = Palette.from(((BitmapDrawable) original).getBitmap()).generate();
         backgroundFill = palette.getDominantColor(Color.GREEN);
      }
      canvas.drawColor(backgroundFill);
      original.setBounds(0, 0, size, size);
      original.setAlpha(255);
      original.draw(canvas);
      p.setStyle(Paint.Style.STROKE);
      p.setColor(Color.argb(128, 0, 0, 0));
      p.setStrokeWidth(size * 0.1f);
      canvas.drawPath(clippingPath, p);
      p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
      p.setColor(Color.WHITE);
      canvas.drawBitmap(mask, 0, 0, p);

      return output;
   }
}