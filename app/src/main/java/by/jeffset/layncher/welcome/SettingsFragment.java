package by.jeffset.layncher.welcome;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import by.jeffset.layncher.R;
import by.jeffset.layncher.data.AppProcessorService;
import by.jeffset.layncher.settings.SettingsWrapper;


public class SettingsFragment extends Fragment {


   private BroadcastReceiver appsJobReceiver;
   private Button startButton;

   private class AppsJobBroadcastReceiver extends BroadcastReceiver {
      @SuppressLint("DefaultLocale")
      @Override public void onReceive(Context context, Intent intent) {
         switch (intent.getAction()) {
            case AppProcessorService.FINISHED:
               startButton.setEnabled(true);
         }
      }
   }

   public SettingsFragment() {}


   @NonNull public static SettingsFragment newInstance() {
      return new SettingsFragment();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View root = inflater.inflate(R.layout.fragment_settings, container, false);
      startButton = (Button) root.findViewById(R.id.button_next_page);
      RadioButton rbLight = (RadioButton) root.findViewById(R.id.rbLight);
      RadioButton rbDark = (RadioButton) root.findViewById(R.id.rbDark);
      View themeViewLight = root.findViewById(R.id.themeCardLight);
      View themeViewDark = root.findViewById(R.id.themeCardDark);
      RadioButton rbStandard = (RadioButton) root.findViewById(R.id.rbStandardLayout);
      RadioButton rbLarge = (RadioButton) root.findViewById(R.id.rbLargeLayout);
      TextView textViewLayoutTip = (TextView) root.findViewById(R.id.textViewLayoutTip);

      SettingsWrapper settingsWrapper = new SettingsWrapper(getContext());

      int theme = settingsWrapper.getThemeId();
      String launcherMode = settingsWrapper.getLayoutMode();

      boolean isLight = theme == R.style.AppTheme_Light;
      rbLight.setChecked(isLight);
      rbDark.setChecked(!isLight);

      boolean isStandard = launcherMode.equals(settingsWrapper.STANDARD_MODE);
      rbStandard.setChecked(isStandard);
      rbLarge.setChecked(!isStandard);

      View.OnClickListener lightListener = v -> {
         settingsWrapper.setThemeId(R.style.AppTheme_Light);
         boolean changed = rbDark.isChecked();
         rbDark.setChecked(false);
         rbLight.setChecked(true);

         if (changed) {switchTheme();}
      };
      View.OnClickListener darkListener = v -> {
         settingsWrapper.setThemeId(R.style.AppTheme_Dark);
         boolean changed = rbLight.isChecked();
         rbDark.setChecked(true);
         rbLight.setChecked(false);
         if (changed)
            switchTheme();
      };

      rbLight.setOnClickListener(lightListener);
      rbDark.setOnClickListener(darkListener);

      themeViewLight.setOnClickListener(lightListener);
      themeViewDark.setOnClickListener(darkListener);

      rbStandard.setOnCheckedChangeListener((buttonView, isChecked) -> {
         if (isChecked) {
            settingsWrapper.setLayoutMode(settingsWrapper.STANDARD_MODE);
            textViewLayoutTip.setText(R.string.tip_layout_standard);
         } else {
            settingsWrapper.setLayoutMode(settingsWrapper.LARGE_MODE);
            textViewLayoutTip.setText(R.string.tip_layout_large);
         }
      });
      return root;
   }

   private void switchTheme() {
      FragmentActivity activity = getActivity();
      activity.overridePendingTransition(0, 0);
      activity.finish();
      Intent intent = new Intent(activity, activity.getClass());
      intent.putExtra(WelcomeActivity.EXTRA_STARTUP_PAGE_NUM, 3);
      activity.startActivity(intent);
      activity.overridePendingTransition(0, 0);
   }

   @Override public void onResume() {
      super.onResume();
      getActivity().startService(new Intent(getActivity(), AppProcessorService.class));
      appsJobReceiver = new AppsJobBroadcastReceiver();
      LocalBroadcastManager.getInstance(getActivity())
          .registerReceiver(appsJobReceiver, AppProcessorService.getBroadcastIntentFilter());
   }

   @Override public void onPause() {
      super.onPause();
      LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(appsJobReceiver);
   }
}
