package by.jeffset.layncher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import by.jeffset.layncher.settings.SettingsWrapper;

public class MainActivity extends AppCompatActivity
    implements FavouriteAppsFragment.FragmentNotifier {
   public static final String TAG = "LaYncher.AppDataHelper";
   private FavouriteAppsFragment favouriteAppsFragment = new FavouriteAppsFragment();
   private MainAppListFragment mainAppListFragment = MainAppListFragment.newInstance();

   @Override public void notifyFragment() {
      favouriteAppsFragment.reloadFavourites();
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