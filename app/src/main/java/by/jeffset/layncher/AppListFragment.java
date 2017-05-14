package by.jeffset.layncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;

import by.jeffset.layncher.data.AppsContract;


public abstract class AppListFragment extends Fragment implements AppListAdapter.AppViewListener {

   @Override public void onClick(String packageName, String activityName, int appId) {
      AsyncTask.execute(() -> {
         ContentValues values = new ContentValues();
         values.put(AppsContract.App.USAGE_TIME, System.currentTimeMillis());
         getActivity().getContentResolver().update(Uri.withAppendedPath(AppsContract.APPS_URI,
             String.valueOf(appId)), values, null, null);
      });
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.setComponent(new ComponentName(packageName, activityName));
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
      startActivity(intent);
   }

   @Override public void onResume() {
      super.onResume();
      getActivity().overridePendingTransition(0, 0);
   }

   @Override public boolean onLongClick(String packageName, int appId, Drawable icon, boolean isFavourite) {
      Activity activity = getActivity();
      String[] items = {"Info", "Uninstall", isFavourite ? "Remove from favourites" : "Add to favourites"};
      new AlertDialog.Builder(activity)
          .setItems(items, (dialog, which) -> {
             switch (which) {
                case 0: { // SHOW INFO
                   Intent intent = new Intent();
                   intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                   Uri uri = Uri.fromParts("package",
                       packageName, null);
                   intent.setData(uri);
                   startActivity(intent);
                   break;
                }
                case 1: { // UNINSTALL
                   Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package",
                       packageName, null));
                   startActivity(intent);
                   break;
                }
                case 2: { // ADD/REMOVE TO/FROM FAVOURITES
                   AsyncTask.execute(() -> {
                      ContentValues values = new ContentValues();
                      values.put(AppsContract.App.IS_FAVOURITE, !isFavourite);
                      getActivity().getContentResolver().update(Uri.withAppendedPath(AppsContract.APPS_URI,
                          String.valueOf(appId)), values, null, null);
                   });
                   break;
                }
             }
          })
          .setIcon(icon)
          .create()
          .show();
      return true;
   }
}
