package by.jeffset.layncher.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

public class InternalContentProvider extends ContentProvider {
   private static final UriMatcher uriMatcher = new UriMatcher(0);
   private static final int APPS_CODE = 1;
   private static final int APPS_ID_CODE = 2;
   private static final String selectID = BaseColumns._ID + "=?";

   private SQLiteOpenHelper helper;

   public InternalContentProvider() {
      uriMatcher.addURI(AppsContract.AUTHORITY, AppsContract.RESULTS_PATH, APPS_CODE);
      uriMatcher.addURI(AppsContract.AUTHORITY, AppsContract.RESULTS_PATH + "/#", APPS_ID_CODE);
   }

   @Override
   public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
            return deleteImpl(uri, selection, selectionArgs);
         case APPS_ID_CODE:
            // user selection args are ignored
            return deleteImpl(uri, selectID, new String[]{uri.getLastPathSegment()});
         default:
            return -1;
      }
   }

   private int deleteImpl(@NonNull Uri uri, String selection, String[] selectionArgs) {
      SQLiteDatabase db = helper.getWritableDatabase();
      int count = db.delete(AppsContract.TABLE_NAME, selection, selectionArgs);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   @Override
   public String getType(@NonNull Uri uri) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
         case APPS_ID_CODE:
            return String.format("vnd.%s/vnd.%s", AppsContract.AUTHORITY, "app");
         default:
            return null;
      }
   }

   @Override
   public Uri insert(@NonNull Uri uri, ContentValues values) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
            SQLiteDatabase db = helper.getWritableDatabase();
            long id = db.insert(AppsContract.TABLE_NAME, null, values);
            Uri insertedUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(AppsContract.APPS_URI, null);
            return insertedUri;
         default:
            return null;
      }
   }

   @Override
   public boolean onCreate() {
      helper = new DbHelper(getContext());
      return true;
   }

   @Override
   public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
            return queryImpl(uri, projection, selection, selectionArgs, sortOrder);
         case APPS_ID_CODE:
            return queryImpl(uri, projection, selectID,
                new String[]{uri.getLastPathSegment()}, sortOrder);
         default:
            return null;
      }
   }

   @NonNull private Cursor queryImpl(@NonNull Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
      SQLiteDatabase db = helper.getReadableDatabase();
      Cursor cursor = db.query(AppsContract.TABLE_NAME,
          projection, selection, selectionArgs, null, null, sortOrder);
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
      return cursor;
   }

   @Override
   public int update(@NonNull Uri uri, ContentValues values, String selection,
                     String[] selectionArgs) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
            return updateImpl(uri, values, selection, selectionArgs);
         case APPS_ID_CODE:
            return updateImpl(uri, values, selectID, new String[]{uri.getLastPathSegment()});
         default:
            return -1;
      }
   }

   private int updateImpl(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
      SQLiteDatabase db = helper.getWritableDatabase();
      int count = db.update(AppsContract.TABLE_NAME, values, selection, selectionArgs);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }
}
