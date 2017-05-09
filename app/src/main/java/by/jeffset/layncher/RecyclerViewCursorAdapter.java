package by.jeffset.layncher;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Created by marco on 9.5.17.
 * Simple cursor adapter for recyclerView (because there's none provided from google)
 */

public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {
   private final Cursor cursor;
   private final int idColumnIndex;
   private boolean observerRegistered;

   protected int getIdColumnIndex() {
      return idColumnIndex;
   }

   private ContentObserver observer = new ContentObserver(new Handler()) {
      @Override public boolean deliverSelfNotifications() {
         return true;
      }

      @Override public void onChange(boolean selfChange) {
         if (!cursor.requery())
            throw new IllegalStateException();
         notifyDataSetChanged();
      }
   };

   public RecyclerViewCursorAdapter(@NonNull Cursor cursor) {
      this.cursor = cursor;
      idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
      super.setHasStableIds(true);
   }

   public void onPause() {
      if (observerRegistered) {
         cursor.unregisterContentObserver(observer);
         observerRegistered = false;
      }
   }

   public void onResume() {
      cursor.requery();
      notifyDataSetChanged();
      if (!observerRegistered) {
         cursor.registerContentObserver(observer);
         observerRegistered = true;
      }
   }

   public abstract void onBindViewHolder(VH holder, Cursor cursor);

   final public Cursor getCursor() {return cursor;}

   @Override final public void onBindViewHolder(VH holder, int position) {
      if (!cursor.moveToPosition(position))
         throw new IllegalStateException();
      onBindViewHolder(holder, cursor);
   }

   @Override final public void setHasStableIds(boolean hasStableIds) {}

   @Override final public long getItemId(int position) {
      if (!cursor.moveToPosition(position))
         throw new IllegalStateException();
      return cursor.getInt(idColumnIndex);
   }

   @Override final public int getItemCount() {
      return cursor.getCount();
   }

}
