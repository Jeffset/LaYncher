package by.jeffset.layncher.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowContentResolver;

import by.jeffset.data.SearchContract;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;


@RunWith(RobolectricTestRunner.class)
public class SearchUriContentProviderTest {

   private ContentResolver resolver;
   private ShadowContentResolver shadowContentResolver;

   @Test
   public void basicTest() {
      Cursor query = shadowContentResolver.query(SearchContract.ALL_URI,
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
      resolver.delete(SearchContract.ALL_URI, null, null);
      //=========================================
      query = resolver.query(SearchContract.LAST_ONE_URI,
          null, null, null, null);
      assertNotNull(query);
      assertTrue(query.getCount() == 0);
      query.close();
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testDeleteLastOne() {
      resolver.delete(SearchContract.LAST_ONE_URI, null, null);
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testDeleteLastDay() {
      resolver.delete(SearchContract.LAST_DAY_URI, null, null);
   }

   @Before
   public void setUp() throws Exception {
      resolver = RuntimeEnvironment.application.getContentResolver();
      shadowContentResolver = Shadows.shadowOf(resolver);
      ProviderInfo info = new ProviderInfo();
      info.authority = SearchContract.AUTHORITY;
      Robolectric.buildContentProvider(SearchUriContentProvider.class).create(info);
   }

}