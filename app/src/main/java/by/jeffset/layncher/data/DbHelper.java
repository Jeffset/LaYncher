package by.jeffset.layncher.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static by.jeffset.layncher.MainActivity.TAG;

/**
 * Created by marco on 27.4.17.
 * Main LaYncher database
 */

public class DbHelper extends SQLiteOpenHelper {

   private static final int DATABASE_VERSION = 51;
   private static final String DATABASE_NAME = "laYncher.db";

   public DbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   @Override public void onCreate(SQLiteDatabase db) {
      Log.i(TAG, "onCreate: database create...");
      db.execSQL(AppsContract.CREATE_APPS_SCRIPT);
      db.execSQL(SearchContract.CREATE_URIS_SCRIPT);
   }

   @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL(AppsContract.DROP_APPS_SCRIPT);
      db.execSQL(SearchContract.DROP_URIS_SCRIPT);
      onCreate(db);
   }
}
