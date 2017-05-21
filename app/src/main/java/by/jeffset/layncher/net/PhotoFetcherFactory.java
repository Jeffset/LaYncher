package by.jeffset.layncher.net;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.Map;
import java.util.TreeMap;

import by.jeffset.layncher.R;
import by.jeffset.layncher.settings.SettingsWrapper;

final class PhotoFetcherFactory {

   private static final Map<String, PhotoFetcher> fetchers = new TreeMap<>();
   private final Context context;

   private final SettingsWrapper wrapper;
   private final DisplayMetrics metrics;

   PhotoFetcherFactory(@NonNull Context context) {
      this.context = context;
      wrapper = new SettingsWrapper(this.context);
      WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      metrics = new DisplayMetrics();
      display.getMetrics(metrics);
   }

   @NonNull PhotoFetcher obtainFetcher() {
      String fetcherName = wrapper.getPhotoFetcher();
      if (fetchers.containsKey(fetcherName))
         return fetchers.get(fetcherName);

      PhotoFetcher fetcher;

      if (fetcherName.equals(context.getString(R.string.unsplash_it))) {
         fetcher = new UnsplashItPhotoFetcher(metrics.widthPixels, metrics.heightPixels);
      } else if (fetcherName.equals(context.getString(R.string.yandex_fotki)))
         fetcher = new YandexFotkiPhotoFetcher();
      else
         throw new IllegalStateException();

      fetchers.put(fetcherName, fetcher);
      return fetcher;
   }
}
