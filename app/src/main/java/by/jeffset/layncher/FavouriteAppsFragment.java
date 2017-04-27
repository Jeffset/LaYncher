package by.jeffset.layncher;


import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import by.jeffset.layncher.data.AppEntry;
import by.jeffset.layncher.data.DbHelper;
import by.jeffset.layncher.settings.SettingsWrapper;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteAppsFragment extends AppList {
   private AutoCompleteTextView uriTextView;

   interface FragmentNotifier {
      void notifyFragment();
   }

   private SQLiteDatabase database;
   private SimpleCursorAdapter cursorAdapter;

   public FavouriteAppsFragment() {}

   @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      database = new DbHelper(getActivity()).getWritableDatabase();
      cursorAdapter = new SimpleCursorAdapter(getActivity(),
          android.R.layout.simple_list_item_1,
          DbHelper.queryUriHistory(database),
          new String[]{
              DbHelper.Contract.UriEntry.COL_NAME_URI},
          new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
      cursorAdapter.setCursorToStringConverter(cursor ->
          cursor.getString(cursor.getColumnIndex(DbHelper.Contract.UriEntry.COL_NAME_URI)));
      uriTextView.setAdapter(cursorAdapter);


      recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
      reloadFavourites();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_favourite_apps, container, false);
      uriTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);

      uriTextView.setOnEditorActionListener((v, actionId, event) -> {
         if ((actionId == EditorInfo.IME_ACTION_DONE)) {
            String text = String.valueOf(uriTextView.getText());
            Toast.makeText(FavouriteAppsFragment.this.getActivity(), text, Toast.LENGTH_SHORT).show();
            try {
               startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(text)));
               updateHistory();
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

   public void reloadFavourites() {
      try {
         launcherAdapter.clear();
         List<AppEntry> entries = DbHelper.queryAllApps(database);
         List<InstalledApp> favourites = new ArrayList<>();
         for (AppEntry entry : entries) {
            if (entry.isFavourite) {
               favourites.add(new InstalledApp(entry, getActivity(), this));
            }
         }
         launcherAdapter.add(new SettingsLauncher(getActivity()));
         launcherAdapter.add(favourites);
      } catch (NullPointerException ignored) {}
   }

   private void updateHistory() {
      SettingsWrapper wrapper = new SettingsWrapper(getActivity());
      String text = String.valueOf(uriTextView.getText());
      Cursor cursor = cursorAdapter.getCursor();

      if (cursor.getCount() >= wrapper.getHistoryLength())
         return;
      cursor.moveToFirst();
      boolean exists = false;
      while (!cursor.isAfterLast()) {
         if (cursor.getString(cursor.getColumnIndex(DbHelper.Contract.UriEntry.COL_NAME_URI))
             .equals(text)) {
            exists = true;
            break;
         }
         cursor.moveToNext();
      }
      if (!exists) DbHelper.insertUriIntoHistory(database, text);
      cursorAdapter.changeCursor(DbHelper.queryUriHistory(database));
   }

   @Override public void onLaunch(Launchable launchable) {

   }

   @Override public void showMenu(Launchable launchable) {

   }
}
