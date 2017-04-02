package by.jeffset.layncher.welcome;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import by.jeffset.layncher.R;


public class InfoFragment extends Fragment {
   private static final String INFO_TEXT_ARGUMENT = "by.jeffset.info_text";
   private static final String INFO_COLOR_ARGUMENT = "by.jeffset.info_color";
   private static final String INFO_LAYOUT_ARGUMENT = "by.jeffset.info_layout";

   private String infoText;
   private int infoColor;
   private int infoLayoutId;

   public InfoFragment() {}

   public static InfoFragment newInstance(String text, int color, @LayoutRes int layout) {
      InfoFragment fragment = new InfoFragment();
      Bundle args = new Bundle();
      args.putString(INFO_TEXT_ARGUMENT, text);
      args.putInt(INFO_COLOR_ARGUMENT, color);
      args.putInt(INFO_LAYOUT_ARGUMENT, layout);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle args = getArguments();
      if (args != null) {
         infoText = args.getString(INFO_TEXT_ARGUMENT);
         infoColor = args.getInt(INFO_COLOR_ARGUMENT);
         infoLayoutId = args.getInt(INFO_LAYOUT_ARGUMENT);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View root = inflater.inflate(infoLayoutId, container, false);
      CardView background = (CardView) root.findViewById(android.R.id.background);
      background.setCardBackgroundColor(infoColor);
      TextView textView = (TextView) root.findViewById(android.R.id.text1);
      textView.setText(infoText);
      return root;
   }

}
