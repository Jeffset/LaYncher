package by.jeffset.layncher;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IconDataFragment extends Fragment {
   static final String TAG = "by.jeffset.layncher.iconDataFragment";

   private Map<String, Drawable> icons = new HashMap<>();

   Drawable loadIcon(String name) {
      if (icons.containsKey(name))
         return icons.get(name);
      File iconFile = new File(getActivity().getFilesDir(), name);
      Drawable icon = new BitmapDrawable(BitmapFactory.decodeFile(iconFile.getAbsolutePath()));
      icons.put(name, icon);
      return icon;
   }

   public IconDataFragment() {
      setRetainInstance(true);
   }
}
