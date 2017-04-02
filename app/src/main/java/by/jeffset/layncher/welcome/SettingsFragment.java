package by.jeffset.layncher.welcome;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import by.jeffset.layncher.MainActivity;
import by.jeffset.layncher.R;


public class SettingsFragment extends Fragment {


   public SettingsFragment() {}


   @NonNull public static SettingsFragment newInstance() {
      return new SettingsFragment();
   }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View root = inflater.inflate(R.layout.fragment_settings, container, false);
      RadioButton rbLight = (RadioButton) root.findViewById(R.id.rbLight);
      RadioButton rbDark = (RadioButton) root.findViewById(R.id.rbDark);
      View themeViewLight = root.findViewById(R.id.themeCardLight);
      View themeViewDark = root.findViewById(R.id.themeCardDark);
      RadioButton rbStandard = (RadioButton) root.findViewById(R.id.rbStandardLayout);
      RadioButton rbLarge = (RadioButton) root.findViewById(R.id.rbLargeLayout);
      TextView textViewLayoutTip = (TextView) root.findViewById(R.id.textViewLayoutTip);

      boolean isLight = WelcomeActivity.theme == R.style.AppTheme_Light;
      rbLight.setChecked(isLight);
      rbDark.setChecked(!isLight);

      boolean isStandard = WelcomeActivity.launcherMode == MainActivity.MODE_STANDARD;
      rbStandard.setChecked(isStandard);
      rbLarge.setChecked(!isStandard);

      View.OnClickListener lightListener = v -> {
         WelcomeActivity.theme = R.style.AppTheme_Light;
         boolean changed = rbDark.isChecked();
         rbDark.setChecked(false);
         rbLight.setChecked(true);
         if (changed)
            switchTheme();
      };
      View.OnClickListener darkListener = v -> {
         WelcomeActivity.theme = R.style.AppTheme_Dark;
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
            WelcomeActivity.launcherMode = MainActivity.MODE_STANDARD;
            textViewLayoutTip.setText(R.string.tip_layout_standard);
         } else {
            WelcomeActivity.launcherMode = MainActivity.MODE_LARGE;
            textViewLayoutTip.setText(R.string.tip_layout_large);
         }
      });
      return root;
   }

   private void switchTheme() {
      FragmentActivity activity = getActivity();
      activity.finish();
      Intent intent = new Intent(activity, activity.getClass());
      intent.putExtra(WelcomeActivity.EXTRA_STARTUP_PAGE_NUM, 3);
      activity.startActivity(intent);
   }

}
