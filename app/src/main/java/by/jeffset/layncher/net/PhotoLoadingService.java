package by.jeffset.layncher.net;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import by.jeffset.layncher.R;
import by.jeffset.layncher.settings.SettingsWrapper;

public class PhotoLoadingService extends Service {
   private static final String TAG = "LaY-PhotoService";

   private static final String BACKGROUND_PNG = "background.png";
   private static final String UPDATE_TIME_KEY = "by.layncher.lastImageUpdateTime";

   private Looper looper;
   private ServiceHandler handler;
   private PhotoFetcherFactory fetcherFactory;
   private boolean runningCycle = false;


   public PhotoLoadingService() {
   }

   @Override public void onCreate() {
      super.onCreate();

      WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
      DisplayMetrics metrics = new DisplayMetrics();
      windowManager.getDefaultDisplay().getMetrics(metrics);
      fetcherFactory = new PhotoFetcherFactory(metrics.widthPixels, metrics.heightPixels,
          PreferenceManager.getDefaultSharedPreferences(this), getString(R.string.pref_photo_fetcher));

      HandlerThread thread = new HandlerThread("PhotoLoadingService");
      thread.start();

      looper = thread.getLooper();
      handler = new ServiceHandler(looper);
   }

   private void loadCurrentImage(ImageReadyListener listener) {
      AsyncTask.execute(() -> {
         synchronized (PhotoLoadingService.class) {
            File imageFile = new File(getFilesDir(), BACKGROUND_PNG);
            if (imageFile.exists()) {
               try {
                  Log.i(TAG, "loadCurrentImage: start");
                  Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                  Log.i(TAG, "loadCurrentImage: end");
                  listener.onImageReady(bitmap);
               } catch (FileNotFoundException ignored) {
                  listener.onImageReady(null);
               }
            }
         }
      });
   }

   private void stopCycle() {
      Log.i(TAG, "stopCycle: called");
      runningCycle = false;
      handler.removeCallbacksAndMessages(null);
   }

   private void startCycle() {
      if (runningCycle) return;
      Log.i(TAG, "startCycle: start_cycle_action");
      runningCycle = true;
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      long updateTime = prefs.getLong(UPDATE_TIME_KEY, 0);
      int seconds = new SettingsWrapper(PhotoLoadingService.this).getImageUpdatePeriod();
      long targetUpdateTime = updateTime + seconds * 1000;
      final long currentTime = System.currentTimeMillis();
      long startDelay = targetUpdateTime <= currentTime ? 0 : targetUpdateTime - currentTime;
      prefs.edit().putLong(UPDATE_TIME_KEY, currentTime).apply();
      Log.i(TAG, "startCycle: delay = " + startDelay);
      handler.postDelayed(new Runnable() {
         @Override public void run() {
            fetchImage();
            prefs.edit().putLong(UPDATE_TIME_KEY, currentTime).apply();
            int period = new SettingsWrapper(PhotoLoadingService.this).getImageUpdatePeriod();
            handler.postDelayed(this, period * 1000);
         }
      }, startDelay);
   }

   private void updateNow() {
      stopCycle();
      handler.post(() -> {
         try {
            if (!fetchImage())
               Toast.makeText(this, "Network is wrong or unavailable", Toast.LENGTH_SHORT).show();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putLong(UPDATE_TIME_KEY, System.currentTimeMillis()).apply();
         } finally {
            startCycle();
            Log.i(TAG, "job: stop");
         }
      });
   }

   private boolean fetchImage() {
      FileOutputStream stream = null;
      ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
      NetworkInfo info = manager.getActiveNetworkInfo();

      if (info != null && info.isConnected() &&
          (info.getType() != ConnectivityManager.TYPE_MOBILE || new SettingsWrapper(this).allowMobileData()))
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
            synchronized (PhotoLoadingService.class) {
               Log.i(TAG, "job: saving image...");
               stream = new FileOutputStream(new File(getFilesDir(), BACKGROUND_PNG));
               image.compress(Bitmap.CompressFormat.PNG, 100, stream);
               Log.i(TAG, "job: saved image");
            }

            for (ImageReadyListener listener : listeners)
               listener.onImageReady(image);

            return true;
         } catch (IOException e) {
            //e.printStackTrace();
            Log.w(TAG, "fetchImage: IOException occurred", e);
            return false;
         } finally {
            if (stream != null) try {
               stream.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      else {
         Log.i(TAG, "fetchImage: network is not ready or not appropriate");
         return false;
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
   public IBinder onBind(Intent intent) {
      Log.i(TAG, "onBind: binder is created");
      startCycle();
      return new PhotoServiceBinder();
   }

   @Override public boolean onUnbind(Intent intent) {
      Log.i(TAG, "onUnbind: called");
      stopCycle();
      return super.onUnbind(intent);
   }

   @FunctionalInterface
   public interface ImageReadyListener {
      void onImageReady(@Nullable Bitmap bitmap);
   }

   private List<ImageReadyListener> listeners = new ArrayList<>();

   public final class PhotoServiceBinder extends Binder {

      public void addListener(@NonNull ImageReadyListener listener) {
         listeners.add(listener);
      }

      public void removeListener(ImageReadyListener listener) {
         listeners.remove(listener);
      }

      public void updateNow() {
         PhotoLoadingService.this.updateNow();
      }

      public void loadCurrentImage(@NonNull ImageReadyListener listener) {
         PhotoLoadingService.this.loadCurrentImage(listener);
      }
   }

   private class ServiceHandler extends Handler {
      ServiceHandler(Looper looper) {
         super(looper);
      }
   }
}
