package by.jeffset.layncher.net;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public abstract class PhotoFetcher implements Iterable<URL>, Iterator<URL> {
   public static final String SIZE_NORMAL = "normal";
   public static final String SIZE_LARGE = "large";
   public static final String SIZE_XLARGE = "xlarge";

   @StringDef(value = {SIZE_NORMAL, SIZE_LARGE, SIZE_XLARGE})
   @interface ImageSize {
   }

   protected String imageSize = SIZE_NORMAL;

   protected void setSize(@ImageSize String size) {
      imageSize = size;
   }

   abstract void initFetcher() throws IOException;


   @NonNull @Override final public Iterator<URL> iterator() {
      return this;
   }
}
