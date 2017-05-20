package by.jeffset.layncher.net;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class PhotoLoadingService extends Service {
   private static final String TAG = "LaY-PhotoService";

   public static final String IMAGE_READY_BROADCAST = "by.jeffset.layncher.net.broadcast.IMAGE_READY";

   static final String UPDATE_NOW_ACTION = "by.jeffset.layncher.net.action.UPDATE_NOW";
   static final String START_CYCLE_ACTION = "by.jeffset.layncher.net.action.START_CYCLE";
   static final String STOP_CYCLE_ACTION = "by.jeffset.layncher.net.action.STOP_CYCLE";
   public static final String BACKGROUND_PNG = "background.png";
   public static final String TEXT_COLOR_KEY = "by.jeffset.layncher.textColor";

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

   public static void setBackgroundImageAsync(@NonNull Activity activity) {
      AsyncTask.execute(() -> {
         File imageFile = new File(activity.getFilesDir(), BACKGROUND_PNG);
         if (imageFile.exists()) {
            try {
               Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
               activity.runOnUiThread(() -> {
                  BitmapDrawable drawable = new BitmapDrawable(bitmap);
                  activity.getWindow().setBackgroundDrawable(drawable);
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

                  stopSelf(startId);
               }
            });
            break;
         case START_CYCLE_ACTION:
            if (runningCycle) break;
            runningCycle = true;
            handler.postDelayed(new Runnable() {
               @Override public void run() {
                  try {
                     fetchImage();
                  } finally {
                     Log.i(TAG, "job: stop");
                  }
                  if (runningCycle)
                     handler.postDelayed(this, 10000);
                  else
                     stopSelf(startId);
               }
            }, 10000);
            break;
         case STOP_CYCLE_ACTION:
            runningCycle = false;
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
