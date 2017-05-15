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

public class LaYContentProvider extends ContentProvider {
   private static final UriMatcher uriMatcher = new UriMatcher(0);
   private static final int APPS_CODE = 1;
   private static final int APPS_ID_CODE = 2;
   private static final int PHONES_CODE = 228;
   private static final String selectID = BaseColumns._ID + "=?";

   private SQLiteOpenHelper helper;

   public LaYContentProvider() {
      uriMatcher.addURI(AppsContract.AUTHORITY, AppsContract.APPS_PATH, APPS_CODE);
      uriMatcher.addURI(AppsContract.AUTHORITY, AppsContract.APPS_PATH + "/#", APPS_ID_CODE);
      uriMatcher.addURI(PhonesContract.AUTHORITY, PhonesContract.PHONES_PATH, PHONES_CODE);
   }

   @Override
   public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
            return deleteImpl(AppsContract.TABLE_NAME, uri, selection, selectionArgs);
         case PHONES_CODE:
            return deleteImpl(PhonesContract.TABLE_NAME, uri, selection, selectionArgs);
         case APPS_ID_CODE:
            // user selection args are ignored
            return deleteImpl(AppsContract.TABLE_NAME, AppsContract.AUTHORITY_URI,
                selectID, new String[]{uri.getLastPathSegment()});
         default:
            throw new UnsupportedOperationException();
      }
   }

   private int deleteImpl(String tableName, @NonNull Uri uri, String selection, String[] selectionArgs) {
      SQLiteDatabase db = helper.getWritableDatabase();
      int count = db.delete(tableName, selection, selectionArgs);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   @Override
   public String getType(@NonNull Uri uri) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
         case APPS_ID_CODE:
            return String.format("vnd.%s/vnd.%s", AppsContract.AUTHORITY, "app");
         case PHONES_CODE:
            return String.format("vnd.%s/vnd.%s", PhonesContract.AUTHORITY, "phoneEntry");
         default:
            return null;
      }
   }

   @Override
   public Uri insert(@NonNull Uri uri, ContentValues values) {
      switch (uriMatcher.match(uri)) {
         case APPS_CODE:
            return insertImpl(AppsContract.TABLE_NAME, uri, values);
         case PHONES_CODE:
            return insertImpl(PhonesContract.TABLE_NAME, uri, values);
         default:
            throw new UnsupportedOperationException();
      }
   }

   private Uri insertImpl(String tableName, @NonNull Uri uri, ContentValues values) {
      SQLiteDatabase db = helper.getWritableDatabase();
      long id = db.insert(tableName, null, values);
      Uri insertedUri = ContentUris.withAppendedId(uri, id);
      getContext().getContentResolver().notifyChange(AppsContract.APPS_URI, null);
      return insertedUri;
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
            return queryImpl(AppsContract.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
         case APPS_ID_CODE:
            return queryImpl(AppsContract.TABLE_NAME, uri, projection, selectID,
                new String[]{uri.getLastPathSegment()}, sortOrder);
         case PHONES_CODE:
            return queryImpl(PhonesContract.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
         default:
            throw new UnsupportedOperationException();
      }
   }

   @NonNull private Cursor queryImpl(String tableName, @NonNull Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
      SQLiteDatabase db = helper.getReadableDatabase();
      Cursor cursor = db.query(tableName,
          projection, selection, selectionArgs, null, null, sortOrder);
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
      return cursor;
   }

   @Override
   public int update(@NonNull Uri uri, ContentValues values, String selection,
                     String[] selectionArgs) {
      switch (uriMatcher.match(uri)) {
         case APPS_ID_CODE:
            return updateImpl(AppsContract.TABLE_NAME, uri, values, selectID, new String[]{uri.getLastPathSegment()});
         case APPS_CODE:
            return updateImpl(AppsContract.TABLE_NAME, uri, values, selection, selectionArgs);
         case PHONES_CODE:
            return updateImpl(PhonesContract.TABLE_NAME, uri, values, selection, selectionArgs);
         default:
            throw new UnsupportedOperationException();
      }
   }

   private int updateImpl(String tableName, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
      SQLiteDatabase db = helper.getWritableDatabase();
      int count = db.update(tableName, values, selection, selectionArgs);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }
}
