package by.jeffset.layncher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by marco on 31.3.17.
 * Adapter for RecyclerVIew
 */

public class AppLauncherAdapter
    extends RecyclerView.Adapter<AppLauncherAdapter.AppViewHolder> {
   public static class DataFragment extends Fragment {
      private static final String TAG = "by.jeffset.layncher.adapter.dataFragment";

      public DataFragment() {}

   }

   static final int APP_TAG_KEY = R.id.recyclerView;

   private List<Launchable> launchables = new ArrayList<>();

   public void add(@NonNull Launchable item) {
      launchables.add(item);
      notifyItemInserted(launchables.size() - 1);
   }

   public void add(@NonNull Launchable item, int position) {
      launchables.add(position, item);
      notifyItemInserted(position);
   }

   public void remove(@NonNull Launchable item) {
      int index = launchables.indexOf(item);
      if (index != -1) {
         launchables.remove(item);
         notifyItemRemoved(index);
      }
   }

   public void add(@NonNull Collection<? extends Launchable> items) {
      launchables.addAll(items);
      notifyDataSetChanged();
   }

   public void clear() {
      launchables.clear();
   }

   private Activity activity;

   AppLauncherAdapter(AppCompatActivity activity) {
      this.activity = activity;
      FragmentManager fm = activity.getSupportFragmentManager();
      DataFragment data = (DataFragment) fm.findFragmentByTag(DataFragment.TAG);
      if (data == null) {
         data = new DataFragment();
         fm.beginTransaction().add(data, DataFragment.TAG).commit();
      }
      //setHasStableIds(true);
   }

   @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new AppViewHolder(parent.getContext(), parent);
   }

   @Override public void onBindViewHolder(AppViewHolder holder, int position) {
      holder.bind(launchables.get(position));
   }

   @Override public int getItemCount() {
      return launchables.size();
   }

   static class AppViewHolder extends RecyclerView.ViewHolder {
      Launchable launchable;
      private TextView textView;
      private ImageView imageView;

      AppViewHolder(@NonNull Context context, @Nullable ViewGroup parent) {
         super(LayoutInflater.from(context).inflate(R.layout.app_icon, parent, false));
         //activity.registerForContextMenu(itemView);
         textView = (TextView) itemView.findViewById(android.R.id.text1);
         imageView = (ImageView) itemView.findViewById(android.R.id.icon);
         itemView.setOnLongClickListener(v -> launchable.showContextMenu());
         itemView.setOnClickListener(v -> launchable.launch());
      }

      void bind(Launchable launchable) {
         this.launchable = launchable;
         imageView.setImageDrawable(launchable.getIcon());
         textView.setText(launchable.getLabel());
         itemView.setTag(APP_TAG_KEY, launchable);
      }

   }
}
