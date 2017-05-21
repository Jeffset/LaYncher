package by.jeffset.layncher.net;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageSwitcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import by.jeffset.layncher.settings.SettingsWrapper;

public class PhotoLoadingService extends Service {
   private static final String TAG = "LaY-PhotoService";

   public static final String IMAGE_READY_BROADCAST = "by.jeffset.layncher.net.broadcast.IMAGE_READY";

   static final String UPDATE_NOW_ACTION = "by.jeffset.layncher.net.action.UPDATE_NOW";
   static final String START_CYCLE_ACTION = "by.jeffset.layncher.net.action.START_CYCLE";
   static final String STOP_CYCLE_ACTION = "by.jeffset.layncher.net.action.STOP_CYCLE";
   private static final String BACKGROUND_PNG = "background.png";
   private static final String UPDATE_TIME_KEY = "by.layncher.lastImageUpdateTime";

   private Looper looper;
   private ServiceHandler handler;
   private PhotoFetcherFactory fetcherFactory;
   private boolean runningCycle = false;

   public static void startUpdateNow(@NonNull Context context) {
      Intent intent = new Intent(context, PhotoLoadingService.class);
      intent.setAction(UPDATE_NOW_ACTION);
      context.startService(intent);
   }

   public static void startCycledUpdate(@NonNull Context context) {
      Intent intent = new Intent(context, PhotoLoadingService.class);
      intent.setAction(START_CYCLE_ACTION);
      context.startService(intent);
   }

   public static void stopCycledUpdate(@NonNull Context context) {
      Intent intent = new Intent(context, PhotoLoadingService.class);
      intent.setAction(STOP_CYCLE_ACTION);
      context.startService(intent);
   }

   public static void setBackgroundImageAsync(@NonNull Activity activity, @NonNull ImageSwitcher imageView) {
      AsyncTask.execute(() -> {
         File imageFile = new File(activity.getFilesDir(), BACKGROUND_PNG);
         if (imageFile.exists()) {
            try {
               Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
               activity.runOnUiThread(() -> {
                  BitmapDrawable drawable = new BitmapDrawable(bitmap);
                  imageView.setImageDrawable(drawable);
                  //drawable.setAlpha(128);
                  //drawable.setGravity(Gravity.FILL);
                  //activity.getWindow().setBackgroundDrawable(drawable);
               });
            } catch (FileNotFoundException ignored) {}
         }
      });
   }

   public PhotoLoadingService() {
   }

   @Override public void onCreate() {
      super.onCreate();

      fetcherFactory = new PhotoFetcherFactory(this);

      HandlerThread thread = new HandlerThread("PhotoLoadingService");
      thread.start();

      looper = thread.getLooper();
      handler = new ServiceHandler(looper);
   }

   @Override public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
      switch (intent.getAction()) {
         case UPDATE_NOW_ACTION:
            Log.i(TAG, "onStartCommand: update_now_action");
            handler.post(() -> {
               try {
                  fetchImage();
               } finally {
                  Log.i(TAG, "job: stop");

                  //stopSelf(startId);
               }
            });
            break;
         case START_CYCLE_ACTION:
            if (runningCycle) break;
            Log.i(TAG, "onStartCommand: start_cycle_action");
            runningCycle = true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            long updateTime = prefs.getLong(UPDATE_TIME_KEY, 0);
            int seconds = new SettingsWrapper(PhotoLoadingService.this).getImageUpdatePeriod();
            long targetUpdateTime = updateTime + seconds * 1000;
            final long currentTime = System.currentTimeMillis();
            long startDelay = targetUpdateTime <= currentTime ? 0 : targetUpdateTime - currentTime;
            prefs.edit().putLong(UPDATE_TIME_KEY, currentTime).apply();
            Log.i(TAG, "onStartCommand: delay = " + startDelay);
            handler.postDelayed(new Runnable() {
               @Override public void run() {
                  fetchImage();
                  prefs.edit().putLong(UPDATE_TIME_KEY, currentTime).apply();
                  int period = new SettingsWrapper(PhotoLoadingService.this).getImageUpdatePeriod();
                  handler.postDelayed(this, period * 1000);
               }
            }, startDelay);
            break;
         case STOP_CYCLE_ACTION:
            runningCycle = false;
            handler.removeCallbacksAndMessages(null);
            stopSelf(startId);
            break;
      }

      Message msg = handler.obtainMessage();
      msg.arg1 = startId;
      msg.obj = intent;
      handler.sendMessage(msg);
      return START_STICKY;
   }

   private void fetchImage() {
      FileOutputStream stream = null;
      ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
      NetworkInfo info = manager.getActiveNetworkInfo();
      if (info != null && info.getType() != ConnectivityManager.TYPE_MOBILE && info.isConnected())
         try {
            Log.i(TAG, "job: start");
            PhotoFetcher fetcher = fetcherFactory.obtainFetcher();
            Log.i(TAG, "job: obtained fetcher");
            if (!fetcher.hasNext()) {
               fetcher.initFetcher();
               Log.i(TAG, "job: initialized fetcher");
            }
            Bitmap image = loadImage(fetcher.next());
            Log.i(TAG, "job: loaded image");
            stream = new FileOutputStream(new File(getFilesDir(), BACKGROUND_PNG));
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Log.i(TAG, "job: saved image");
            image.recycle();
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent intent = new Intent(IMAGE_READY_BROADCAST);
            lbm.sendBroadcast(intent);
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
            if (stream != null) try {
               stream.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      else {
         Log.i(TAG, "fetchImage: network is not ready or not appropriate");
      }
   }

   private Bitmap loadImage(URL imageUrl) throws IOException {
      Log.i(TAG, "loadImage: start " + imageUrl);
      URLConnection connection = imageUrl.openConnection();
      return BitmapFactory.decodeStream(new BufferedInputStream(connection.getInputStream()));
   }

   @Override public void onDestroy() {
      Log.i(TAG, "onDestroy: service destroyed");
      looper.quit();
   }

   @Override
   public IBinder onBind(Intent intent) {return null;}

   private class ServiceHandler extends Handler {
      ServiceHandler(Looper looper) {
         super(looper);
      }
   }
}
