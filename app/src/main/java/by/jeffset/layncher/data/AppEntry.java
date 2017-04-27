package by.jeffset.layncher.data;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

public class AppEntry {
   public String packageName;
   public String activityName;
   public String label;
   public String sourceDir;
   public long modificationTime;
   public long usageTime;
   public boolean isFavourite;

   @Override public String toString() {
      return String.format("%s.%s", packageName, activityName);
   }

   @NonNull public final File getIconFile(@NonNull Context context) {
      return new File(context.getFilesDir(), toString());
   }
}
