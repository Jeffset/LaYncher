package by.jeffset.layncher.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Calendar;

import by.jeffset.data.SearchContract;

import static by.jeffset.layncher.MainActivity.TAG;

public class SearchUriContentProvider extends ContentProvider {
   private static final UriMatcher uriMatcher = new UriMatcher(0);
   public static final int LAST_ONE_CODE = 1;
   public static final int LAST_DAY_CODE = 2;
   public static final int ALL_CODE = 3;
   private SQLiteOpenHelper helper;

   public SearchUriContentProvider() {
      uriMatcher.addURI(SearchContract.AUTHORITY, SearchContract.LAST_ONE_PATH, LAST_ONE_CODE);
      uriMatcher.addURI(SearchContract.AUTHORITY, SearchContract.LAST_DAY_PATH, LAST_DAY_CODE);
      uriMatcher.addURI(SearchContract.AUTHORITY, SearchContract.ALL_PATH, ALL_CODE);
   }

   @Override
   public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
      switch (uriMatcher.match(uri)) {
         case ALL_CODE:
            SQLiteDatabase db = helper.getWritableDatabase();
            int count = db.delete(SearchContract.TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(SearchContract.ALL_URI, null);
            return count;
         default: // for all others deletion is not supported
            return -1;
      }
   }

   @Override
   public String getType(@NonNull Uri uri) {
      switch (uriMatcher.match(uri)) {
         case LAST_ONE_CODE:
         case LAST_DAY_CODE:
         case ALL_CODE:
            return String.format("vnd.%s/vnd.%s", SearchContract.AUTHORITY, "searchUri");
         default:
            return null;
      }
   }

   @Override
   public Uri insert(@NonNull Uri uri, ContentValues values) {
      switch (uriMatcher.match(uri)) {
         case ALL_CODE:
            SQLiteDatabase db = helper.getWritableDatabase();
            long id = db.insert(SearchContract.TABLE_NAME, null, values);
            Uri insertedUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(SearchContract.ALL_URI, null);
            return insertedUri;
         default:// for all others insertion is not supported
            return null;
      }
   }

   @Override
   public boolean onCreate() {
      helper = new DbHelper(getContext());
      return true;
   }


   private static long getDayStart() {
      Calendar c = Calendar.getInstance();
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTimeInMillis();
   }

   @Override
   public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
      switch (uriMatcher.match(uri)) {
         case LAST_ONE_CODE:
            Log.i(TAG, "query: one uri");
            return queryImpl(projection, selection, selectionArgs,
                SearchContract.Search.TIME + " DESC LIMIT 1");
         case LAST_DAY_CODE: //  TODO implement in proper way
            long dayStart = getDayStart();
            Log.i(TAG, "query: now = " + System.currentTimeMillis());
            Log.i(TAG, "query: startDay = " + dayStart);
            return queryImpl(projection, SearchContract.Search.TIME + ">?",
                new String[]{String.valueOf(dayStart)},
                SearchContract.Search.TIME + " DESC");
         case ALL_CODE:
            return queryImpl(projection, selection, selectionArgs, sortOrder);
         default:
            return null;
      }
   }

   @NonNull private Cursor queryImpl(String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
      SQLiteDatabase db = helper.getReadableDatabase();
      Cursor cursor = db.query(SearchContract.TABLE_NAME,
          projection, selection, selectionArgs, null, null, sortOrder);
      cursor.setNotificationUri(getContext().getContentResolver(), SearchContract.ALL_URI);
      return cursor;
   }

   @Override
   public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
      switch (uriMatcher.match(uri)) {
         case ALL_CODE:
            SQLiteDatabase db = helper.getWritableDatabase();
            int count = db.update(SearchContract.TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
         default:
            return -1;
      }
   }
}
