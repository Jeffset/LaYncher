package by.jeffset.layncher.data;


import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import by.jeffset.layncher.R;

public abstract class AppProcessor {
   public static List<ContentValues> processApps(@NonNull Context context, @Nullable String packageName) {
      List<ContentValues> result = new ArrayList<>();
      PackageManager pm = context.getPackageManager();
      Intent launchIntent = new Intent(Intent.ACTION_MAIN);
      launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      List<ResolveInfo> resolveInfoList =
          pm.queryIntentActivities(launchIntent, PackageManager.GET_META_DATA);
      for (ResolveInfo info : resolveInfoList) {
         if (packageName != null && !info.activityInfo.packageName.equals(packageName))
            continue;
         ActivityInfo activityInfo = info.activityInfo;
         ApplicationInfo applicationInfo = activityInfo.applicationInfo;
         ContentValues values = new ContentValues();
         values.put(AppsContract.App.PACKAGE_NAME, activityInfo.packageName);
         values.put(AppsContract.App.ACTIVITY_NAME, activityInfo.name);
         values.put(AppsContract.App.LABEL, activityInfo.loadLabel(pm).toString());
         values.put(AppsContract.App.MODIFICATION_TIME,
             new File(applicationInfo.sourceDir).lastModified());
         values.put(AppsContract.App.SOURCE_DIR, applicationInfo.sourceDir);
         values.put(AppsContract.App.USAGE_TIME, -1);
         values.put(AppsContract.App.IS_FAVOURITE, false);
         AppProcessor.renderIcon(context, activityInfo, getIconFileName(activityInfo.packageName, activityInfo.name));
         result.add(values);
      }
      return result;
   }

   public static String getIconFileName(@NonNull String packageName, @NonNull String activityName) {
      return String.format("%s.%s", packageName, activityName);
   }

   private static void renderIcon(@NonNull Context context, @NonNull ActivityInfo activityInfo,
                                  @NonNull String name) {
      PackageManager pm = context.getPackageManager();
      Drawable iconOriginal = null;
      ComponentName activityName = new ComponentName(activityInfo.packageName, activityInfo.name);
      try {
         final Resources res = pm.getResourcesForActivity(activityName);
         iconOriginal = ResourcesCompat.getDrawable(res, activityInfo.icon, null);
      } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
         e.printStackTrace();
      }
      try {
         if (iconOriginal == null) {
            final Resources res = pm.getResourcesForApplication(activityInfo.applicationInfo);
            iconOriginal = ResourcesCompat.getDrawable(res, activityInfo.applicationInfo.icon, null);
         }
      } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
         e.printStackTrace();
      }
      try {
         if (iconOriginal == null)
            iconOriginal = pm.getApplicationIcon(activityInfo.packageName);
      } catch (PackageManager.NameNotFoundException e) {
         e.printStackTrace();
         iconOriginal = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icon5, null);
      }
      Bitmap icon = decorateIcon(iconOriginal, context);
      File savedIcon = new File(context.getFilesDir(), name);
      try {
         icon.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(savedIcon));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static Bitmap mask = null;

   @NonNull private static Bitmap decorateIcon(@NonNull Drawable original,
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
}
