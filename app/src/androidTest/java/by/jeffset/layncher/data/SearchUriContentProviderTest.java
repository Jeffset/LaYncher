package by.jeffset.layncher.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import by.jeffset.data.SearchContract;


@RunWith(AndroidJUnit4.class)
public class SearchUriContentProviderTest extends ProviderTestCase2<SearchUriContentProvider> {

   public SearchUriContentProviderTest() {
      super(SearchUriContentProvider.class, "by.jeffset.layncher");
   }

   @Test
   public void basicTest() {
      ContentResolver resolver = getMockContentResolver();
      Cursor query = resolver.query(SearchContract.ALL_URI,
          null, null, null, null);
      assertNotNull(query);
      assertTrue(query.getCount() == 0);
      query.close();

      ContentValues contentValues = new ContentValues();
      contentValues.put(SearchContract.Search.URI, "https://vk.com/giffist");
      final long value = System.currentTimeMillis();
      contentValues.put(SearchContract.Search.TIME, value);
      resolver.insert(SearchContract.ALL_URI, contentValues);

      query = resolver.query(SearchContract.ALL_URI,
          null, null, null, null);
      assertNotNull(query);
      assertTrue(query.getCount() == 1);
      assertTrue(query.moveToFirst());
      assertEquals(query.getString(query.getColumnIndex(SearchContract.Search.URI)), "https://vk.com/giffist");
      assertEquals(query.getLong(query.getColumnIndex(SearchContract.Search.TIME)), value);
      query.close();
      // delete update and ect
   }

   @Before
   @Override
   public void setUp() throws Exception {
      setContext(InstrumentationRegistry.getTargetContext());
      super.setUp();
   }

   @After
   @Override
   public void tearDown() throws Exception {
      super.tearDown();
   }

}