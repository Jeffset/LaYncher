package by.jeffset.layncher.net;

import android.annotation.SuppressLint;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnsplashItPhotoFetcherTest {

   private static final int WIDTH = 200;
   private static final int HEIGHT = 300;

   private UnsplashItPhotoFetcher fetcher;

   @Before
   public void setUp() throws IOException {
      fetcher = new UnsplashItPhotoFetcher(WIDTH, HEIGHT);
      fetcher.initFetcher();
      fetcher.imageId = 0;
   }

   @Test
   public void testHasNext() {
      assertTrue(fetcher.hasNext());
   }

   @Test
   @SuppressLint("DefaultLocale")
   public void textURI() throws MalformedURLException {
      int id = 0;
      for (URL url : fetcher) {
         assertEquals(new URL(String.format("https://unsplash.it/%d/%d?image=%d", WIDTH, HEIGHT, id++)), url);
         if (id == 100)
            break;
      }
   }
}