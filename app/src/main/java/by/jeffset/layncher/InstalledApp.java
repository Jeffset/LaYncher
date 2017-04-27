package by.jeffset.layncher;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

import java.io.File;

import javax.annotation.Nullable;

import by.jeffset.layncher.data.AppEntry;

/**
 * Created by marco on 25.4.17.
 * Class representing
 */

public class InstalledApp implements Launchable {
   private static int iconIds[] = {
       R.drawable.icon1, R.drawable.icon2,
       R.drawable.icon3, R.drawable.icon4,
       R.drawable.icon5, R.drawable.icon6,
       R.drawable.icon7, R.drawable.icon8,
       R.drawable.icon9, R.drawable.icon10
   };

   private static Bitmap mask = null;

   void setListener(AppListener listener) {
      this.listener = listener;
   }

   private AppListener listener = null;

   @NonNull
   AppEntry data;
   @NonNull
   private final Context context;

   private Drawable icon;
   private CharSequence label;

   private @Nullable Drawable getIconCached(@NonNull Context context) {
      File cached = data.getIconFile(context);
      if (cached.exists()) {
         return new BitmapDrawable(BitmapFactory.decodeFile(cached.getPath()));
      }
      return null;
   }

   @NonNull public static Bitmap decorateIcon(@NonNull Drawable original,
                                              @NonNull Context context) {
      Resources res = context.getResources();

      int size = res.getDimensionPixelSize(R.dimen.iconSize);
      Path clippingPath = new Path();
      RectF clip = new RectF(0.f, 0.f, size, size);
      clippingPath.addRoundRect(clip, size / 4.f, size / 4.f, Path.Direction.CW);

      if (mask == null) {
         mask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
         Canvas maskCanvas = new Canvas(mask);
         Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         pathPaint.setStyle(Paint.Style.FILL);
         pathPaint.setColor(Color.WHITE);
         maskCanvas.drawPath(clippingPath, pathPaint);
      }

      Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
      Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(output);
      int backgroundFill = Color.BLACK;
      if (original instanceof BitmapDrawable) {
         Palette palette = Palette.from(((BitmapDrawable) original).getBitmap()).generate();
         backgroundFill = palette.getDominantColor(Color.GREEN);
      }
      canvas.drawColor(backgroundFill);
      original.setBounds(0, 0, size, size);
      original.setAlpha(255);
      original.draw(canvas);
      p.setStyle(Paint.Style.STROKE);
      p.setColor(Color.argb(128, 0, 0, 0));
      p.setStrokeWidth(size * 0.1f);
      canvas.drawPath(clippingPath, p);
      p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
      p.setColor(Color.WHITE);
      canvas.drawBitmap(mask, 0, 0, p);

      return output;
   }

   public InstalledApp(@NonNull AppEntry data,
                       @NonNull Context context, AppListener listener) {
      this.data = data;
      this.context = context;
      icon = getIconCached(context);
      label = data.label;
      this.listener = listener;
   }

   @Override public boolean equals(Object obj) {
      return obj instanceof InstalledApp && ((InstalledApp) obj).data.equals(data);
   }

   @Override public Drawable getIcon() {
      return icon;
   }

   @Override public CharSequence getLabel() {
      return label;
   }

   @Override public boolean launch() {
      if (listener != null)
         listener.onLaunch(this);
      Intent intent = context.getPackageManager().getLaunchIntentForPackage(data.packageName);
      context.startActivity(intent);
      return true;
   }

   public boolean showContextMenu() {
      if (listener != null) {
         listener.showMenu(this);
         return true;
      }
      return false;
   }
}
