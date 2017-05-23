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
public class SearchUriContentProviderAndroidTest extends ProviderTestCase2<SearchUriContentProvider> {

   private ContentResolver resolver;

   public SearchUriContentProviderAndroidTest() {
      super(SearchUriContentProvider.class, "by.jeffset.layncher");
   }

   @Test
   public void basicTest() {
      Cursor query = resolver.query(SearchContract.ALL_URI,
          null, null, null, null);
      assertNotNull(query);
      assertTrue(query.getCount() == 0);
      query.close();

      //==============================================================
      ContentValues contentValues = new ContentValues();
      contentValues.put(SearchContract.Search.URI, "https://vk.com/giffist");
      long value = System.currentTimeMillis();
      contentValues.put(SearchContract.Search.TIME, value);
      resolver.insert(SearchContract.ALL_URI, contentValues);
      //-----------
      contentValues = new ContentValues();
      contentValues.put(SearchContract.Search.URI, "tel:+375447221605");
      contentValues.put(SearchContract.Search.TIME, value - 1000);
      resolver.insert(SearchContract.ALL_URI, contentValues);
      //==============================================================

      query = resolver.query(SearchContract.ALL_URI,
          null, null, null, null);
      assertNotNull(query);
      assertTrue(query.getCount() == 2);
      assertTrue(query.moveToPosition(1));
      assertEquals(query.getString(query.getColumnIndex(SearchContract.Search.URI)), "tel:+375447221605");
      assertEquals(query.getLong(query.getColumnIndex(SearchContract.Search.TIME)), value - 1000);
      query.close();
      //================================================================

      query = resolver.query(SearchContract.LAST_ONE_URI,
          null, null, null, null);
      assertNotNull(query);
      assertTrue(query.getCount() == 1);
      assertTrue(query.moveToFirst());
      assertEquals(query.getString(query.getColumnIndex(SearchContract.Search.URI)), "https://vk.com/giffist");
      assertEquals(query.getLong(query.getColumnIndex(SearchContract.Search.TIME)), value);
      query.close();

      //================================================================
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testDeleteLastOne() {
      resolver.delete(SearchContract.LAST_ONE_URI, null, null);
   }

   @Before
   @Override
   public void setUp() throws Exception {
      setContext(InstrumentationRegistry.getTargetContext());
      super.setUp();
      resolver = getMockContentResolver();
   }

   @After
   @Override
   public void tearDown() throws Exception {
      super.tearDown();
   }

}