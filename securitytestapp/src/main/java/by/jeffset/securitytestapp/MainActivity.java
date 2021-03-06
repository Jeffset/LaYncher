package by.jeffset.securitytestapp;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import by.jeffset.data.SearchContract;

public class MainActivity extends AppCompatActivity {

   private EditText editText;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      editText = (EditText) findViewById(R.id.editText);
   }

   private void displayError(SecurityException e) {
      e.printStackTrace();
      new AlertDialog.Builder(this)
          .setTitle("Security exception")
          .setMessage(e.getLocalizedMessage())
          .show();
   }

   public void queryLastUri(View view) {
      try {
         Cursor cursor = getContentResolver().query(SearchContract.LAST_ONE_URI, null, null,
             null, SearchContract.Search.TIME + " DESC");
         if (!cursor.moveToFirst())
            return;
         new AlertDialog.Builder(this)
             .setMessage(cursor.getString(cursor.getColumnIndex(SearchContract.Search.URI)))
             .setPositiveButton(android.R.string.ok, null)
             .show();
         cursor.close();
      } catch (SecurityException e) {
         displayError(e);
      }
   }

   public void queryLastDayUris(View view) {
      try {
         Cursor cursor = getContentResolver().query(SearchContract.LAST_DAY_URI, null, null,
             null, SearchContract.Search.TIME + " DESC");
         if (!cursor.moveToFirst())
            return;
         StringBuilder stringBuilder = new StringBuilder();
         int index = cursor.getColumnIndex(SearchContract.Search.URI);
         while (!cursor.isAfterLast()) {
            stringBuilder.append(cursor.getString(index)).append('\n');
            cursor.moveToNext();
         }
         new AlertDialog.Builder(this)
             .setMessage(stringBuilder.toString())
             .setPositiveButton(android.R.string.ok, null)
             .show();
         cursor.close();
      } catch (SecurityException e) {
         displayError(e);
      }
   }

   public void queryAllUris(View view) {
      try {
         Cursor cursor = getContentResolver().query(SearchContract.ALL_URI, null, null,
             null, SearchContract.Search.TIME + " DESC");
         if (!cursor.moveToFirst())
            return;
         StringBuilder stringBuilder = new StringBuilder();
         int index = cursor.getColumnIndex(SearchContract.Search.URI);
         while (!cursor.isAfterLast()) {
            stringBuilder.append(cursor.getString(index)).append('\n');
            cursor.moveToNext();
         }
         new AlertDialog.Builder(this)
             .setMessage(stringBuilder.toString())
             .setPositiveButton(android.R.string.ok, null)
             .show();
         cursor.close();
      } catch (SecurityException e) {
         displayError(e);
      }
   }

   public void insertNewUri(View view) {
      try {
         ContentValues values = new ContentValues();
         values.put(SearchContract.Search.URI, String.valueOf(editText.getText()));
         values.put(SearchContract.Search.TIME, System.currentTimeMillis());
         if (getContentResolver().insert(SearchContract.ALL_URI, values) != null)
            Toast.makeText(this, "inserted", Toast.LENGTH_SHORT).show();
      } catch (SecurityException e) {
         displayError(e);
      }
   }

   public void testUandexDsik(View view) {
      Cursor query = null;
      try {
         ProviderInfo info = getPackageManager().getProviderInfo(
             new ComponentName("ru.yandex.disk", "com.yandex.disk.sync.PhotoSyncProvider"),
             PackageManager.GET_META_DATA);
         Uri uri = Uri.parse("content://com.yandex.disk.sync.photo/" + editText.getText());
         String type = getContentResolver().getType(uri);
         System.out.println("type = " + type);
         query = getContentResolver().query(uri,
             null, null, null, null);
         System.out.println("query.getCount() = " + query.getCount());
      } catch (UnsupportedOperationException e) {
         Toast.makeText(this, "unsupported", Toast.LENGTH_SHORT).show();
         e.printStackTrace();
      } catch (PackageManager.NameNotFoundException e) {
         Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
         e.printStackTrace();
      }
   }
}
