package by.jeffset.layncher.net;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public abstract class PhotoFetcher implements Iterable<URL>, Iterator<URL> {

   abstract void initFetcher() throws IOException;


   @NonNull @Override final public Iterator<URL> iterator() {
      return this;
   }
}
