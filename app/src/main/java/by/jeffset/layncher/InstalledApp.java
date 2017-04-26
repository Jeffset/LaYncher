package by.jeffset.layncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.util.TypedValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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

   static {

   }

   void setListener(AppListener listener) {
      this.listener = listener;
   }

   private AppListener listener = null;

   @NonNull
   private final ApplicationInfo applicationInfo;
   @NonNull
   private final Context context;

   private Drawable icon;
   private CharSequence label;

   @NonNull private static Drawable decorateIcon(@NonNull Drawable original,
                                                 @NonNull Context context,
                                                 @NonNull String id) {
      /*Resources res = context.getResources();
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
      }*/
      File cached = new File(context.getCacheDir(), id + "icon");
      if (cached.exists()) {
         return new BitmapDrawable(BitmapFactory.decodeFile(cached.getPath()));
      }

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

      try {
         output.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cached));
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }

      return new BitmapDrawable(output);
   }

   InstalledApp(@NonNull ApplicationInfo applicationInfo,
                @NonNull PackageManager pm,
                @NonNull Context context) {
      this.applicationInfo = applicationInfo;
      this.context = context;
      Drawable iconOriginal = null;
      try {
         final Resources res = pm.getResourcesForApplication(applicationInfo);
         iconOriginal = ResourcesCompat.getDrawable(res, applicationInfo.icon, null);
      } catch (PackageManager.NameNotFoundException e) {
         e.printStackTrace();
      } catch (Resources.NotFoundException e) {
         e.printStackTrace();
      }
      if (iconOriginal == null) iconOriginal = pm.getApplicationIcon(applicationInfo);
      icon = decorateIcon(iconOriginal, context, applicationInfo.packageName);
      label = pm.getApplicationLabel(applicationInfo);
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
      Intent intent = context.getPackageManager().getLaunchIntentForPackage(applicationInfo.packageName);
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
