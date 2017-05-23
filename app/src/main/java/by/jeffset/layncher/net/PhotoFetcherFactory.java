package by.jeffset.layncher.net;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.TreeMap;

final class PhotoFetcherFactory {

   private static final Map<String, PhotoFetcher> fetchers = new TreeMap<>();
   private final int width;
   private final int height;
   private final SharedPreferences prefs;
   private final String fetcherRes;

   PhotoFetcherFactory(int width, int height, @NonNull SharedPreferences prefs, String fetcherRes) {
      this.width = width;
      this.height = height;
      this.prefs = prefs;
      this.fetcherRes = fetcherRes;
   }

   @NonNull PhotoFetcher obtainFetcher() {
      String fetcherName = prefs.getString(fetcherRes, "unsplash.it");
      if (fetchers.containsKey(fetcherName))
         return fetchers.get(fetcherName);

      PhotoFetcher fetcher;

      switch (fetcherName) {
         case "unsplash.it":
            int max = Math.max(width, height);
            fetcher = new UnsplashItPhotoFetcher(max, max);
            break;
         case "yandex.fotki":
            fetcher = new YandexFotkiPhotoFetcher();
            break;
         default:
            throw new IllegalStateException();
      }

      fetchers.put(fetcherName, fetcher);
      return fetcher;
   }
}
