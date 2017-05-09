package by.jeffset.layncher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import by.jeffset.layncher.settings.SettingsActivity;
import by.jeffset.layncher.settings.SettingsWrapper;

public class MainActivity extends AppCompatActivity {
   public static final String TAG = "LaYncher.AppDataHelper";
   private FavouriteAppsFragment favouriteAppsFragment = new FavouriteAppsFragment();
   private MainAppListFragment mainAppListFragment = MainAppListFragment.newInstance();

   public void addContact(View view) {
      Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
      intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
      startActivityForResult(intent, 740);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
         case 740: {
            Uri contactData = data.getData();
            Log.i(TAG, "onActivityResult: contactData: " + contactData);

            //MAKE YOUR CALL .. do whatever... example:
            ContentResolver contentResolver = getContentResolver();
            Cursor cur = contentResolver.query(contactData, null,
                null, null, null);
            cur.moveToFirst();
            int columnIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String theNumber = cur.getString(columnIndex);
            Log.i(TAG, "onActivityResult: number: " + theNumber);
            cur.close();
            break;
         }
         case 228: {
            finish();
            startActivity(getIntent());
         }
      }
   }

   public void settings(View view) {
      startActivityForResult(new Intent(this, SettingsActivity.class), 228);
   }


   class PagerAdapterWithFaves extends FragmentPagerAdapter {
      public PagerAdapterWithFaves(FragmentManager fm) {
         super(fm);
      }

      @Override public Fragment getItem(int position) {
         switch (position) {
            case 0:
               return mainAppListFragment;
            case 1:
               return favouriteAppsFragment;
         }
         return null;
      }

      @Override public int getCount() {
         return 2;
      }
   }

   class PagerAdapterStub extends FragmentPagerAdapter {
      public PagerAdapterStub(FragmentManager fm) {
         super(fm);
      }

      @Override public Fragment getItem(int position) {
         return mainAppListFragment;
      }

      @Override public int getCount() {
         return 1;
      }
   }

   class PageAnimator implements ViewPager.PageTransformer {
      @Override public void transformPage(View page, float position) {
         page.setTranslationY((Math.abs(position)) * -360.f);
         page.setAlpha(1.0f - Math.abs(position));
      }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      SettingsWrapper settingsWrapper = new SettingsWrapper(this);
      setTheme(settingsWrapper.getThemeId());
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
      pager.setAdapter(settingsWrapper.isShowFaves() ?
          new PagerAdapterWithFaves(getFragmentManager())
          :
          new PagerAdapterStub(getFragmentManager()));
      pager.setPageTransformer(false, new PageAnimator());
      pager.setCurrentItem(0);
   }
}