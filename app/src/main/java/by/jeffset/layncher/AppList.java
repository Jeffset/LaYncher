package by.jeffset.layncher;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by marco on 28.4.17.
 */

abstract class AppList extends Fragment implements Launchable.AppListener {
   protected AppLauncherAdapter launcherAdapter;// = new AppLauncherAdapter(this);
   protected RecyclerView recyclerView;


   @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      launcherAdapter = new AppLauncherAdapter(getActivity());
      //noinspection ConstantConditions
      recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
      recyclerView.setAdapter(launcherAdapter);
   }
}
