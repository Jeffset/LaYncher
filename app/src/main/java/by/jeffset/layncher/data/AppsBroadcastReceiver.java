package by.jeffset.layncher.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static by.jeffset.layncher.MainActivity.TAG;

public class AppsBroadcastReceiver extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      String packageName = intent.getData().getSchemeSpecificPart();
      switch (intent.getAction()) {
         case Intent.ACTION_PACKAGE_ADDED:
            Log.i(TAG, "onReceive: BROADCAST INSTALL " + packageName);
            onInstall(context, packageName);
            break;
         case Intent.ACTION_PACKAGE_REMOVED:
            onUninstall(context, packageName);
            Log.i(TAG, "onReceive: BROADCAST REMOVE " + packageName);
            break;
      }
   }

   private void onInstall(Context context, String packageName) {
      if (packageName.equals(context.getPackageName()))
         return;
      Intent intent = new Intent(context, AppProcessorService.class);
      intent.putExtra(AppProcessorService.JOB_TYPE_EXTRA, AppProcessorService.PROCESS_PACKAGE);
      intent.putExtra(AppProcessorService.PACKAGE_NAME_EXTRA, packageName);
      context.startService(intent);
      /*final PendingResult async = goAsync();
      new Thread(() -> {
         try {
            List<ContentValues> valuesList = AppProcessor.processApps(context, packageName);
            for (ContentValues values : valuesList) {
               context.getContentResolver().insert(AppsContract.APPS_URI, values);
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            async.finish();
         }
      }).start();*/
   }

   private void onUninstall(Context context, String packageName) {
      final PendingResult async = goAsync();
      new Thread(() -> {
         try {
            int count = context.getContentResolver().delete(
                AppsContract.APPS_URI,
                AppsContract.App.PACKAGE_NAME + "=?",
                new String[]{packageName});
            Log.i(TAG, "onUninstall: count = " + String.valueOf(count));
         } finally {
            async.finish();
         }
      }).start();

   }
}
