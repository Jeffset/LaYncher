package by.jeffset.layncher;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class ContactsListAdapter extends RecyclerViewCursorAdapter<ContactsListAdapter.ContactViewHolder> {
   public interface ContactListener {

      void onCall(String packageName, String activityName, int appId);

      boolean onInfo(String packageName, int appId, Drawable icon, boolean isFavourite);

   }

   class ContactViewHolder extends RecyclerView.ViewHolder {

      public ContactViewHolder(View itemView) {
         super(itemView);
      }
   }

   ContactListener listener = null;

   public ContactsListAdapter(@NonNull Cursor cursor) {
      super(cursor);
   }

   @Override public void onBindViewHolder(ContactViewHolder holder, Cursor cursor) {

   }

   @Override public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return null;
   }
}
