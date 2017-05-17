package by.jeffset.layncher;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import by.jeffset.layncher.data.AppProcessorService;
import by.jeffset.layncher.data.PhonesContract;
import by.jeffset.layncher.settings.SettingsActivity;
import by.jeffset.layncher.settings.SettingsWrapper;

public class MainActivity extends AppCompatActivity {
   public static final String TAG = "LaYncher.AppDataHelper";
   public static final int SETTINGS_REQ = 228;
   public static final int PHONE_REQ = 740;
   public static final String[] PHONE_DATA = {
       ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
       ContactsContract.CommonDataKinds.Phone.NUMBER,
       ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
   };

   @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                    @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      switch (requestCode) {
         case PHONE_REQ: {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
               launchNumberPicker();
            }
            break;
         }
      }
   }

   public void addContact(View view) {
      int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
      int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
      if (result1 != PackageManager.PERMISSION_GRANTED || result2 != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this,
             new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, PHONE_REQ);
         return;
      }

      launchNumberPicker();
   }

   private void launchNumberPicker() {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
      startActivityForResult(intent, PHONE_REQ);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
         case PHONE_REQ:
            if (resultCode == RESULT_OK) {
               AsyncTask.execute(() -> {
                  ContentResolver contentResolver = getContentResolver();
                  Cursor cur = contentResolver.query(data.getData(), PHONE_DATA,
                      null, null, null);
                  cur.moveToFirst();
                  Long id = cur.getLong(cur.getColumnIndex(PHONE_DATA[0]));
                  String number = cur.getString(cur.getColumnIndex(PHONE_DATA[1]));
                  String name = cur.getString(cur.getColumnIndex(PHONE_DATA[2]));
                  cur.close();

                  InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,
                      ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id));
                  ContentValues values = new ContentValues();


                  if (stream != null && stream instanceof ByteArrayInputStream) {
                     Bitmap bitmap = BitmapFactory.decodeStream(stream);
                     Bitmap decorated = AppProcessorService
                         .decorateIcon(new BitmapDrawable(bitmap), getResources(), false);

                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                     decorated.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                     values.put(PhonesContract.Contact.IMAGE, outputStream.toByteArray());
                  }

                  values.put(PhonesContract.Contact.NUMBER, number);
                  values.put(PhonesContract.Contact.CONTACT_ID, id);
                  values.put(PhonesContract.Contact.NAME, name);

                  Cursor testQ = contentResolver.query(PhonesContract.PHONES_URI,
                      new String[]{PhonesContract.Contact.NAME},
                      PhonesContract.Contact.NUMBER + " = ?",
                      new String[]{number}, null);
                  if (testQ.getCount() != 0) {
                     contentResolver.update(PhonesContract.PHONES_URI, values,
                         PhonesContract.Contact.NUMBER + " = ?",
                         new String[]{number});
                  } else
                     contentResolver.insert(PhonesContract.PHONES_URI, values);
                  testQ.close();

               });
               break;
            }
         case SETTINGS_REQ: {
            if (resultCode == SettingsActivity.NEED_RELAUNCH) {
               finish();
               startActivity(getIntent());
            }
         }
      }
   }

   public void settings(View view) {
      startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQ);
   }

   private class PagerAdapterWithFaves extends FragmentPagerAdapter {
      PagerAdapterWithFaves(FragmentManager fm) {
         super(fm);
      }

      @Override public Fragment getItem(int position) {
         switch (position) {
            case 0:
               return MainAppListFragment.newInstance();
            case 1:
               return new FavouriteAppsFragment();
         }
         return null;
      }

      @Override public int getCount() {
         return 2;
      }

   }

   private class PagerAdapterStub extends FragmentPagerAdapter {
      PagerAdapterStub(FragmentManager fm) {
         super(fm);
      }

      @Override public Fragment getItem(int position) {
         return MainAppListFragment.newInstance();
      }

      @Override public int getCount() {
         return 1;
      }
   }

   private class PageAnimator implements ViewPager.PageTransformer {
      @Override public void transformPage(View page, float position) {
         page.setTranslationY((Math.abs(position)) * -360.f);
         page.setAlpha(1.0f - Math.abs(position));
      }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      startService(new Intent(this, AppProcessorService.class));
      SettingsWrapper settingsWrapper = new SettingsWrapper(this);
      setTheme(settingsWrapper.getThemeId());
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
      if (settingsWrapper.isShowFaves()) {
         PagerAdapterWithFaves adapter = new PagerAdapterWithFaves(getFragmentManager());
         pager.setAdapter(adapter);
      } else {
         PagerAdapterStub adapter = new PagerAdapterStub(getFragmentManager());
         pager.setAdapter(adapter);
      }
      pager.setPageTransformer(false, new PageAnimator());
      pager.setCurrentItem(0);
   }
}