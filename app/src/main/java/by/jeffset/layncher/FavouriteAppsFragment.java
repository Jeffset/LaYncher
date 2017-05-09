package by.jeffset.layncher;


import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import by.jeffset.layncher.settings.SettingsWrapper;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteAppsFragment extends AppListFragment {
   private AutoCompleteTextView uriTextView;

   private RecyclerView recyclerView;

   private SimpleCursorAdapter cursorAdapter;
   private AppListAdapter appListAdapter;

   public FavouriteAppsFragment() {}

   @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      SettingsWrapper wrapper = new SettingsWrapper(getActivity());

      Cursor querySearch = getActivity().getContentResolver().query(SearchContract.ALL_URI,
          null, null, null,
          SearchContract.Search.TIME + " DESC LIMIT " + wrapper.getHistoryLength());

      cursorAdapter = new SimpleCursorAdapter(getActivity(),
          android.R.layout.simple_list_item_1,
          querySearch,
          new String[]{SearchContract.Search.URI},
          new int[]{android.R.id.text1},
          CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER | CursorAdapter.FLAG_AUTO_REQUERY);
      cursorAdapter.setCursorToStringConverter(cursor ->
          cursor.getString(cursor.getColumnIndex(SearchContract.Search.URI)));
      uriTextView.setAdapter(cursorAdapter);

      Cursor query = getActivity().getContentResolver().query(AppsContract.APPS_URI, AppsContract.App.ALL,
          AppsContract.App.IS_FAVOURITE + "!=?", new String[]{"0"}, null);
      appListAdapter = new AppListAdapter(getActivity(), query);
      int columnCount = 4;
      recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
      recyclerView.setAdapter(appListAdapter);
      appListAdapter.listener = this;
   }

   @Override public void onResume() {
      super.onResume();
      appListAdapter.onResume();
   }

   @Override public void onPause() {
      super.onPause();
      appListAdapter.onPause();
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
}
