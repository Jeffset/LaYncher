package by.jeffset.layncher;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import by.jeffset.data.SearchContract;
import by.jeffset.layncher.data.AppsContract;
import by.jeffset.layncher.data.PhonesContract;
import by.jeffset.layncher.settings.SettingsWrapper;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteAppsFragment extends AppListFragment implements PhonesListAdapter.PhoneViewListener {
   private AutoCompleteTextView uriTextView;

   private RecyclerView recyclerView;
   private RecyclerView recyclerViewContacts;

   private SimpleCursorAdapter cursorAdapter;
   private AppListAdapter appListAdapter;
   private PhonesListAdapter phonesAdapter;

   public FavouriteAppsFragment() {}

   @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      Activity activity = getActivity();
      ContentResolver contentResolver = activity.getContentResolver();
      SettingsWrapper wrapper = new SettingsWrapper(activity);

      Cursor querySearch = contentResolver.query(SearchContract.ALL_URI,
          null, null, null,
          SearchContract.Search.TIME + " DESC LIMIT " + wrapper.getHistoryLength());

      cursorAdapter = new SimpleCursorAdapter(activity,
          android.R.layout.simple_list_item_1,
          querySearch,
          new String[]{SearchContract.Search.URI},
          new int[]{android.R.id.text1},
          CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER | CursorAdapter.FLAG_AUTO_REQUERY);
      cursorAdapter.setCursorToStringConverter(cursor ->
          cursor.getString(cursor.getColumnIndex(SearchContract.Search.URI)));
      uriTextView.setAdapter(cursorAdapter);
      boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

      Cursor query = contentResolver.query(AppsContract.APPS_URI, AppsContract.App.ALL,
          AppsContract.App.IS_FAVOURITE + "!=?", new String[]{"0"}, null);
      appListAdapter = new AppListAdapter(activity, query);
      int columnCount = 4;
      recyclerView.setLayoutManager(new GridLayoutManager(activity, columnCount));
      recyclerView.setAdapter(appListAdapter);
      appListAdapter.listener = this;

      Cursor phonesQuery = contentResolver.query(PhonesContract.PHONES_URI, null, null,
          null, null);
      phonesAdapter = new PhonesListAdapter(activity, phonesQuery);
      recyclerViewContacts.setLayoutManager(new GridLayoutManager(activity, isLandscape ? 3 : 4));
      recyclerViewContacts.setAdapter(phonesAdapter);
      phonesAdapter.listener = this;
   }

   @Override public void onResume() {
      super.onResume();
      new Handler().postDelayed(() -> {
         appListAdapter.onResume();
         phonesAdapter.onResume();
      }, 200);
   }

   @Override public void onPause() {
      super.onPause();
      appListAdapter.onPause();
      phonesAdapter.onPause();
   }

   @Override public void onDestroy() {
      super.onDestroy();
      cursorAdapter.getCursor().close();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_favourite_apps, container, false);
      uriTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
      recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
      recyclerViewContacts = (RecyclerView) view.findViewById(R.id.recyclerViewContacts);

      uriTextView.setOnEditorActionListener((v, actionId, event) -> {
         if ((actionId == EditorInfo.IME_ACTION_DONE)) {
            String text = String.valueOf(uriTextView.getText());
            Toast.makeText(FavouriteAppsFragment.this.getActivity(), text, Toast.LENGTH_SHORT).show();
            try {
               startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(text)));
               ContentValues values = new ContentValues();
               values.put(SearchContract.Search.URI, text);
               values.put(SearchContract.Search.TIME, System.currentTimeMillis());
               ContentResolver contentResolver = getActivity().getContentResolver();
               Cursor cursor = null;
               try {
                  cursor = contentResolver.query(SearchContract.ALL_URI, null,
                      SearchContract.Search.URI + "=?", new String[]{text}, null);
                  if (cursor.getCount() == 0) {
                     contentResolver.insert(SearchContract.ALL_URI, values);
                  } else {
                     contentResolver.update(SearchContract.ALL_URI, values,
                         SearchContract.Search.URI + "=?", new String[]{text});
                  }
               } finally {
                  if (cursor != null) cursor.close();
               }
            } catch (Exception e) {
               Toast.makeText(getActivity(), "Invalid URI", Toast.LENGTH_SHORT).show();
            } finally {
               uriTextView.setText("");
            }
            return true;
         }
         return false;
      });

      return view;
   }

   @Override public void onPhoneViewClick(String name, String number) {
      Intent callIntent = new Intent(Intent.ACTION_CALL);
      callIntent.setData(Uri.parse("tel:" + number));
      startActivity(callIntent);
   }

   @Override public boolean onPhoneViewLongClick(String name, String number, Drawable icon, long contactID) {
      Activity activity = getActivity();
      String[] items = {"Info", "Remove from list"};
      new AlertDialog.Builder(activity)
          .setItems(items, (dialog, which) -> {
             switch (which) {
                case 0: { // SHOW INFO
                   Intent intent = new Intent();
                   intent.setAction(Intent.ACTION_VIEW);
                   Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                       String.valueOf(contactID));
                   intent.setData(uri);
                   startActivity(intent);
                   break;
                }
                case 1: { // ADD/REMOVE TO/FROM FAVOURITES
                   AsyncTask.execute(() -> getActivity().getContentResolver().delete(PhonesContract.PHONES_URI,
                       PhonesContract.Contact.NUMBER + " = ?", new String[]{number}));
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
