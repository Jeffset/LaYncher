package by.jeffset.layncher.data;

import android.net.Uri;
import android.provider.BaseColumns;


public interface SearchContract {
   String AUTHORITY = "by.jeffset.layncher";
   String LAST_ONE_PATH = "search/lastDay/last";
   String LAST_DAY_PATH = "search/lastDay";
   String ALL_PATH = "search/";

   Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
   Uri LAST_ONE_URI = Uri.withAppendedPath(AUTHORITY_URI, LAST_ONE_PATH);
   Uri LAST_DAY_URI = Uri.withAppendedPath(AUTHORITY_URI, LAST_DAY_PATH);
   Uri ALL_URI = Uri.withAppendedPath(AUTHORITY_URI, ALL_PATH);

   String TABLE_NAME = "search";

   interface Search extends BaseColumns {
      String URI = "value";
      String TIME = "time";
   }

   String CREATE_URIS_SCRIPT =
       "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +

           Search._ID + " INTEGER PRIMARY KEY, " +
           Search.TIME + " INT, " +
           Search.URI + " TEXT)";

   String DROP_URIS_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
