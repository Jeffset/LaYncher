package by.jeffset.layncher;

import android.app.Activity;
import android.app.FragmentManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import by.jeffset.layncher.data.AppProcessorService;
import by.jeffset.layncher.data.AppsContract;

public class AppListAdapter extends RecyclerViewCursorAdapter<AppListAdapter.AppViewHolder> {

   private int packageNameColInd;
   private int activityNameColInd;
   private int labelColInd;
   private int isFavouriteColInd;
   private IconDataFragment dataFragment;
   private Activity activity;
   AppViewListener listener = null;

   public interface AppViewListener {

      void onClick(String packageName, String activityName, int appId);

      boolean onLongClick(String packageName, int appId, Drawable icon, boolean isFavourite);
   }

   class AppViewHolder extends RecyclerView.ViewHolder {
      TextView labelView;
      ImageView iconView;
      ImageView startView;

      AppViewHolder(View itemView) {
         super(itemView);
         itemView.setOnClickListener(v -> {
            if (listener != null) {
               Cursor cursor = getCursor();
               if (!cursor.moveToPosition(getAdapterPosition()))
                  throw new IllegalStateException();
               listener.onClick(
                   cursor.getString(packageNameColInd),
                   cursor.getString(activityNameColInd),
                   cursor.getInt(getIdColumnIndex()));
            }
         });
         itemView.setOnLongClickListener(v -> {
            if (listener != null) {
               Cursor cursor = getCursor();
               if (!cursor.moveToPosition(getAdapterPosition()))
                  throw new IllegalStateException();
               return listener.onLongClick(cursor.getString(packageNameColInd),
                   cursor.getInt(getIdColumnIndex()),
                   iconView.getDrawable(),
                   cursor.getInt(isFavouriteColInd) != 0);
            }
            return false;
         });
         labelView = (TextView) itemView.findViewById(android.R.id.text1);
         iconView = (ImageView) itemView.findViewById(android.R.id.icon);
         startView = (ImageView) itemView.findViewById(android.R.id.icon1);
      }

      void bind(@NonNull Cursor cursor, @NonNull IconDataFragment dataFragment) {
         boolean isFavourite = cursor.getInt(isFavouriteColInd) != 0;
         String packageName = cursor.getString(packageNameColInd);
         String activityName = cursor.getString(activityNameColInd);
         String iconFileName = AppProcessorService.getIconFileName(packageName, activityName);
         String label = cursor.getString(labelColInd);
         startView.setVisibility(isFavourite ? View.VISIBLE : View.INVISIBLE);
         iconView.setImageDrawable(dataFragment.loadIcon(iconFileName));
         labelView.setText(label);
      }
   }


   public AppListAdapter(@NonNull Activity activity, @NonNull Cursor cursor) {
      super(cursor);
      packageNameColInd = cursor.getColumnIndex(AppsContract.App.PACKAGE_NAME);
      activityNameColInd = cursor.getColumnIndex(AppsContract.App.ACTIVITY_NAME);
      labelColInd = cursor.getColumnIndex(AppsContract.App.LABEL);
      isFavouriteColInd = cursor.getColumnIndex(AppsContract.App.IS_FAVOURITE);
      this.activity = activity;
      obtainDataFragment();
   }

   @Override public void onResume() {
      obtainDataFragment();
      super.onResume();
   }

   private void obtainDataFragment() {
      if (dataFragment == null) {
         FragmentManager fm = activity.getFragmentManager();
         dataFragment = (IconDataFragment) fm.findFragmentByTag(IconDataFragment.TAG);
         if (dataFragment == null) {
            dataFragment = new IconDataFragment();
            fm.beginTransaction()
                .add(dataFragment, IconDataFragment.TAG)
                .commit();
         }
      }
   }

   @Override public void onBindViewHolder(AppViewHolder holder, Cursor cursor) {
      obtainDataFragment();
      holder.bind(cursor, dataFragment);
   }

   @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_icon, null);
      return new AppViewHolder(v);
   }

}
