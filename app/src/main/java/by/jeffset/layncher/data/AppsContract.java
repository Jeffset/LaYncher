package by.jeffset.layncher.data;


import android.net.Uri;
import android.provider.BaseColumns;

public interface AppsContract {
   String AUTHORITY = "by.jeffset.layncher.private";
   String APPS_PATH = "apps";

   Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
   Uri APPS_URI = Uri.withAppendedPath(AUTHORITY_URI, APPS_PATH);

   String TABLE_NAME = "apps";

   interface App extends BaseColumns {

      String PACKAGE_NAME = "packageName";
      String ACTIVITY_NAME = "activityName";
      String LABEL = "label";
      String MODIFICATION_TIME = "modTime";
      String USAGE_TIME = "usageTime";
      String IS_FAVOURITE = "favouriteFlag";
      String SOURCE_DIR = "sourceDir";

      String[] ALL = new String[]{
          _ID,
          PACKAGE_NAME,
          ACTIVITY_NAME,
          LABEL,
          //MODIFICATION_TIME,
          //USAGE_TIME,
          IS_FAVOURITE,
          //SOURCE_DIR,
      };
   }

   String CREATE_SCRIPT =
       "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
           App._ID + " INTEGER PRIMARY KEY, " +
           App.PACKAGE_NAME + " TEXT, " +
           App.ACTIVITY_NAME + " TEXT, " +
           App.LABEL + " TEXT, " +
           App.MODIFICATION_TIME + " INTEGER, " +
           App.USAGE_TIME + " INTEGER, " +
           App.IS_FAVOURITE + " INTEGER, " +
           App.SOURCE_DIR + " TEXT)";
   String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
