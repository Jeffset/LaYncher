package by.jeffset.layncher.data;

import android.net.Uri;
import android.provider.BaseColumns;

public interface PhonesContract {
   String AUTHORITY = "by.jeffset.layncher.private";
   String PHONES_PATH = "phones";

   Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
   Uri PHONES_URI = Uri.withAppendedPath(AUTHORITY_URI, PHONES_PATH);

   String TABLE_NAME = "phones";

   interface Contact extends BaseColumns {
      String NAME = "name";
      String NUMBER = "number";
      String IMAGE = "image";
      String CONTACT_ID = "contactID";
   }

   String CREATE_SCRIPT =
       "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
           Contact._ID + " INTEGER PRIMARY KEY, " +
           Contact.CONTACT_ID + " INTEGER, " +
           Contact.NAME + " TEXT, " +
           Contact.NUMBER + " TEXT, " +
           Contact.IMAGE + " BLOB)";
   String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
