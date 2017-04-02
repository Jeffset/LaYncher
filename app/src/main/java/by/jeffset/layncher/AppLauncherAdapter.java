package by.jeffset.layncher;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

class App {
   String label;
   Bitmap icon;
   int appID;

   @Override public boolean equals(Object obj) {
      return obj instanceof App && appID == ((App) obj).appID;
   }
}

/**
 * Created by marco on 31.3.17.
 * Adapter for RecyclerVIew
 */

public class AppLauncherAdapter extends RecyclerView.Adapter<AppLauncherAdapter.AppViewHolder> {
   private static final String DELETED_KEY = "by.jeffset.layncher.adapter.deleted";
   static final int APP_TAG_KEY = R.id.recyclerView;

   interface AppActionListener {
      void onAppClick(App app);

      void onAppLongClick(View itemView);
   }

   private AppActionListener listener;

   private Activity activity;

   private TreeSet<Integer> deleted = new TreeSet<>();
   private Random random = new Random();

   private static int iconIds[] = {
       R.drawable.icon1, R.drawable.icon2,
       R.drawable.icon3, R.drawable.icon4,
       R.drawable.icon5, R.drawable.icon6,
       R.drawable.icon7, R.drawable.icon8,
       R.drawable.icon9, R.drawable.icon10
   };
   private static Bitmap[] icons = new Bitmap[10];

   AppLauncherAdapter(Activity activity) {
      this.activity = activity;
      setHasStableIds(true);
   }

   @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new AppViewHolder(parent);
   }

   @Override public void onBindViewHolder(AppViewHolder holder, int position) {
      holder.bind(position);
   }

   @Override public int getItemCount() {
      return Integer.MAX_VALUE;
   }

   @Override public long getItemId(int position) {
      return getAppID(position);
   }

   public static void loadIcons(Context context, @ColorRes int frameColor) {
      // Можно (и нужно) в AsyncTask, но пусть поработает - splash подольше повисит:)
      Resources res = context.getResources();
      int color = ResourcesCompat.getColor(res, frameColor, null);
      int radius = res.getDimensionPixelSize(R.dimen.iconCornerRadius);
      int W = res.getDimensionPixelSize(R.dimen.iconFrameThickness);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inDensity = 640;
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(W);
      paint.setColor(color);
      for (int i = 0; i < iconIds.length; i++) {
         if (icons[i] == null) {
            Bitmap source = BitmapFactory.decodeResource(res, iconIds[i], options);
            int width = source.getWidth();
            int height = source.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, source.getConfig());
            Canvas canvas = new Canvas(bmp);
            RectF rectF = new RectF(W, W, width - W, height - W);
            canvas.drawRoundRect(rectF, radius, radius, paint);
            canvas.drawBitmap(source, 0, 0, null);
            source.recycle();
            icons[i] = bmp;
         }
      }
   }


   void getApp(App app, int position) {
      app.appID = getAppID(position);
      app.label = String.format(Locale.US, "0x%X", app.appID + 1);
      random.setSeed(app.appID * 5);
      int n = random.nextInt(10);
      //int n = (int) (Math.round(Math.abs(Math.sin(1000 * position) * 10.f)) % 10);
      app.icon = icons[n];
   }

   void onSaveInstanceState(@Nullable Bundle outState) {
      if (outState != null) {
         outState.putSerializable(DELETED_KEY, deleted);
      }
   }

   void onRestoreInstanceState(@Nullable Bundle savedState) {
      if (savedState != null) {
         deleted = (TreeSet<Integer>) savedState.getSerializable(DELETED_KEY);
      }
   }

   private int getAppID(int position) {
      int i = 1;
      int mappedPosition = position;
      for (Integer del : deleted) {
         if (del <= mappedPosition) {
            mappedPosition = position + i;
         } else break;
         ++i;
      }
      return mappedPosition;
   }

   class AppViewHolder extends RecyclerView.ViewHolder {
      App app = new App();
      private TextView textView;
      private ImageView imageView;

      AppViewHolder(ViewGroup parent) {
         super(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_icon, parent, false));
         activity.registerForContextMenu(itemView);
         textView = (TextView) itemView.findViewById(android.R.id.text1);
         imageView = (ImageView) itemView.findViewById(android.R.id.icon);
         itemView.setOnLongClickListener(v -> {
            if (listener != null)
               listener.onAppLongClick(itemView);
            return true;
         });
         /*itemView.setOnClickListener(v -> {
            if (listener != null)
               listener.onAppClick(app);
         });*/
      }

      void bind(final int position) {
         getApp(app, position);
         imageView.setImageBitmap(app.icon);
         textView.setText(app.label);
         itemView.setTag(APP_TAG_KEY, app);
      }

   }


   void removeItem(App app) {
      deleted.add(app.appID);
      notifyDataSetChanged();
   }

   void setListener(AppActionListener listener) {
      this.listener = listener;
   }
}
