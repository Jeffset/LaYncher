package by.jeffset.layncher.net;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by marco on 18.5.17.
 *
 * @see <a href="https://unsplash.it">https://unsplash.it</a>
 */

final class UnsplashItPhotoFetcher extends PhotoFetcher {
   private final int width;
   private final int height;
   private int imageId = 10;

   UnsplashItPhotoFetcher(int width, int height) {
      int m = Math.max(width, height);
      this.width = m;
      this.height = m;
   }

   @Override void initFetcher() throws IOException {
      // do nothing
   }

   @Override public boolean hasNext() {
      return true;
   }

   @NonNull @SuppressLint("DefaultLocale")
   @Override public URL next() {
      try {
         return new URL(String.format("https://unsplash.it/%d/%d?image=%d", width, height, imageId++));
      } catch (MalformedURLException e) {
         throw new Error(e);
      }
   }
}
