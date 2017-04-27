package by.jeffset.layncher;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.yandex.metrica.YandexMetrica;

public class App extends Application {

   public static final String API_KEY = "dd5f270f-eb9e-459a-a49c-f43cd3ef3be1";

   @Override public void onCreate() {
      super.onCreate();
      Stetho.initializeWithDefaults(this);
      // Инициализация AppMetrica SDK
      YandexMetrica.activate(getApplicationContext(), API_KEY);
      // Отслеживание активности пользователей
      YandexMetrica.enableActivityAutoTracking(this);
   }
}
