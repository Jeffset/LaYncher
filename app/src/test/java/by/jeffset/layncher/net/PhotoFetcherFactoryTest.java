package by.jeffset.layncher.net;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PhotoFetcherFactoryTest {

   private class TestSharedPrefs implements SharedPreferences {

      static final String FETCHER = "fetcher";
      String fetcherName;

      @Override public Map<String, ?> getAll() {
         return null;
      }

      @Nullable @Override public String getString(String key, @Nullable String defValue) {
         if (key.equals(FETCHER))
            return fetcherName;
         else
            return defValue;
      }

      @Nullable @Override public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
         return null;
      }

      @Override public int getInt(String key, int defValue) {
         return 0;
      }

      @Override public long getLong(String key, long defValue) {
         return 0;
      }

      @Override public float getFloat(String key, float defValue) {
         return 0;
      }

      @Override public boolean getBoolean(String key, boolean defValue) {
         return false;
      }

      @Override public boolean contains(String key) {
         return false;
      }

      @Override public Editor edit() {
         return null;
      }

      @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

      }

      @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

      }
   }

   @Test
   public void obtainFetcherMock() throws Exception {
      SharedPreferences preferences = mock(SharedPreferences.class);
      doReturn("unsplash.it", "unsplash.it", "yandex.fotki", "yandex.fotki")
          .when(preferences).getString(eq("fetcher"), any());

      PhotoFetcherFactory factory = new PhotoFetcherFactory(100, 200, preferences, "fetcher");
      PhotoFetcher fetcher = factory.obtainFetcher();
      assertEquals(UnsplashItPhotoFetcher.class, fetcher.getClass());
      assertEquals(fetcher, factory.obtainFetcher());
      fetcher = factory.obtainFetcher();
      assertEquals(YandexFotkiPhotoFetcher.class, fetcher.getClass());
      assertEquals(fetcher, factory.obtainFetcher());
   }

   @Test(expected = IllegalStateException.class)
   public void obtainFetcherFail() throws Exception {
      SharedPreferences preferences = mock(SharedPreferences.class);
      doReturn("some illegal value")
          .when(preferences).getString(eq("fetcher"), any());
      PhotoFetcherFactory factory = new PhotoFetcherFactory(100, 200, preferences, "fetcher");
      factory.obtainFetcher();
   }

   @Test
   public void obtainFetcherStub() throws Exception {
      TestSharedPrefs prefs = new TestSharedPrefs();
      PhotoFetcherFactory factory = new PhotoFetcherFactory(200, 300, prefs, TestSharedPrefs.FETCHER);
      prefs.fetcherName = "unsplash.it";
      PhotoFetcher fetcher = factory.obtainFetcher();
      assertEquals(UnsplashItPhotoFetcher.class, fetcher.getClass());
      assertEquals(fetcher, factory.obtainFetcher());

      prefs.fetcherName = "yandex.fotki";
      fetcher = factory.obtainFetcher();
      assertEquals(YandexFotkiPhotoFetcher.class, fetcher.getClass());
      assertEquals(fetcher, factory.obtainFetcher());
   }

}