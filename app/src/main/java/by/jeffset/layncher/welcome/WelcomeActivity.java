package by.jeffset.layncher.welcome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import by.jeffset.layncher.MainActivity;
import by.jeffset.layncher.R;
import by.jeffset.layncher.data.AppProcessorService;
import by.jeffset.layncher.settings.SettingsWrapper;

public class WelcomeActivity extends AppCompatActivity {

   static final String EXTRA_STARTUP_PAGE_NUM = "by.jeffset.layncher.startup_page";
   public static final String ANIMATABLE_TAG = "by.jeffset.animatable";
   private static final String TAG = "JEFFSET-LAYNCHER";
   public static final String PREFS_WELCOME_SHOWED = "by.jeffset.layncher.welcome_showed";
   private ViewPager viewPager;
   private int oldDragPosition = 0;
   private ValueAnimator pageFlipper;
   private PagerIndicator pagerPagerIndicator;

   private ProgressBar progressBar;
   private TextView textViewProgress;
   private BroadcastReceiver appsJobReceiver;

   private class AppsJobBroadcastReceiver extends BroadcastReceiver {
      @Override public void onReceive(Context context, Intent intent) {
         switch (intent.getAction()) {
            case AppProcessorService.FINISHED: {
               progressBar.setVisibility(View.GONE);
               textViewProgress.setText("everything is ready now");
               break;
            }
            case AppProcessorService.PROGRESS: {
               int progress = intent.getIntExtra(AppProcessorService.PROGRESS_VALUE_EXTRA, 0);
               progressBar.setIndeterminate(false);
               progressBar.setProgress(progress);
               textViewProgress.setText(String.format("%d %%", progress));
               break;
            }
         }

      }
   }

   public void onMainActivityStart(View view) {
      new SettingsWrapper(WelcomeActivity.this).setWelcomeWasShowed();
      finish();
      Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
      startActivity(mainIntent);
   }

   class WelcomePagerAdapter extends FragmentPagerAdapter {

      WelcomePagerAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override public Fragment getItem(int position) {
         //return fragments[position];
         switch (position) {
            case 0:
               Log.i(TAG, "getItem: 0");
               return InfoFragment.newInstance(getString(R.string.welcome_to_layncher),
                   ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null),
                   R.layout.fragment_greetings);
            case 1:
               Log.i(TAG, "getItem: 1");
               return InfoFragment.newInstance(getString(R.string.info_text_1),
                   ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight, null),
                   R.layout.fragment_info);
            case 2:
               Log.i(TAG, "getItem: 2");
               return InfoFragment.newInstance(getString(R.string.info_text_2),
                   ResourcesCompat.getColor(getResources(), R.color.colorAccent, null),
                   R.layout.fragment_info);
            case 3:
               Log.i(TAG, "getItem: 3");
               return SettingsFragment.newInstance();
         }
         return null;

      }

      @Override public int getCount() {
         return 4;
      }
   }

   class PageAnimator implements ViewPager.PageTransformer {
      @Override public void transformPage(View page, float position) {
         page.setTranslationY((Math.abs(position)) * 360.f);
         page.setAlpha(1.0f - Math.abs(position));
         pagerPagerIndicator.setActiveView(viewPager.getCurrentItem());
      }
   }

   protected void onCreate(Bundle savedInstanceState) {
      PreferenceManager.setDefaultValues(this, R.xml.settings_fave_apps, true);
      SettingsWrapper settingsWrapper = new SettingsWrapper(this);
      if (settingsWrapper.wasWelcomeShowed()) {
         startActivity(new Intent(this, MainActivity.class));
         super.onCreate(savedInstanceState);
         finish();
         return;
      }

      setTheme(settingsWrapper.getThemeId());
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_welcome);

      pagerPagerIndicator = (PagerIndicator) findViewById(R.id.pagerIndicator);
      viewPager = (ViewPager) findViewById(R.id.viewPager);
      viewPager.setOffscreenPageLimit(3);
      final WelcomePagerAdapter adapter = new WelcomePagerAdapter(getSupportFragmentManager());
      viewPager.setAdapter(adapter);
      viewPager.setPageTransformer(false, new PageAnimator());
      viewPager.setOnTouchListener((v, event) -> true);
      int startupPage = getIntent().getIntExtra(EXTRA_STARTUP_PAGE_NUM, 0);
      viewPager.setCurrentItem(startupPage,
          false);
      progressBar = (ProgressBar) findViewById(R.id.progressBar);
      progressBar.setIndeterminate(true);
      textViewProgress = (TextView) findViewById(R.id.textViewProgress);

      startService(new Intent(this, AppProcessorService.class));
   }

   @Override protected void onResume() {
      super.onResume();
      appsJobReceiver = new AppsJobBroadcastReceiver();
      LocalBroadcastManager.getInstance(this)
          .registerReceiver(appsJobReceiver, AppProcessorService.getBroadcastIntentFilter());
   }

   @Override protected void onPause() {
      super.onPause();
      LocalBroadcastManager.getInstance(this).unregisterReceiver(appsJobReceiver);
   }

   public void onBtnNextPage(View view) {
      if (pageFlipper == null) {
         initFlipper();
      }
      if (viewPager.beginFakeDrag()) {
         oldDragPosition = 0;
         pageFlipper.start();
      }
   }

   private void initFlipper() {
      pageFlipper = ValueAnimator.ofInt(0, viewPager.getWidth() - viewPager.getPaddingLeft());
      pageFlipper.addListener(new AnimatorListenerAdapter() {
         private void end() {
            viewPager.endFakeDrag();
            pagerPagerIndicator.setActiveView(viewPager.getCurrentItem());
         }

         @Override public void onAnimationEnd(Animator animation) {
            end();
         }

         @Override public void onAnimationCancel(Animator animation) {
            end();
         }
      });

      pageFlipper.setInterpolator(new AccelerateDecelerateInterpolator());
      pageFlipper.addUpdateListener(animation -> {
         try {
            int dragPosition = (Integer) animation.getAnimatedValue();
            int dragOffset = dragPosition - oldDragPosition;
            oldDragPosition = dragPosition;
            viewPager.fakeDragBy(-dragOffset);
         } catch (NullPointerException ignored) {/*just for avoiding multiple taps*/}
      });
      pageFlipper.setDuration(500);
   }
}
