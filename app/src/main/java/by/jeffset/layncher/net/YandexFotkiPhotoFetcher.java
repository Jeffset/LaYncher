package by.jeffset.layncher.net;

import android.support.annotation.NonNull;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YandexFotkiPhotoFetcher extends PhotoFetcher {
   static final String[] sizes = new String[]{
       "XXL", "XL", "L", "M"
   };

   URL url;

   YandexFotkiPhotoFetcher() {
      try {
         url = new URL("http://api-fotki.yandex.ru/api/podhistory/?limit=100");
      } catch (MalformedURLException ignored) {}
   }

   static class Entry {
      Map<String, URL> images = new HashMap<>();
   }

   List<Entry> entries = new ArrayList<>();

   private int index;

   @Override void initFetcher() throws IOException {
      index = 0;
      entries.clear();
      URLConnection connection = url.openConnection();
      try {
         parseImageList(connection.getInputStream());
      } catch (XmlPullParserException e) {
         e.printStackTrace();
      }
   }

   void parseImageList(@NonNull InputStream inputStream) throws IOException, XmlPullParserException {
      XmlPullParser parser = Xml.newPullParser();
      parser.setInput(new InputStreamReader(inputStream));
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.nextTag();

      Entry currentEntry = null;
      while (parser.next() != XmlPullParser.END_DOCUMENT) {
         if (parser.getEventType() == XmlPullParser.START_TAG) {
            switch (parser.getName()) {
               case "entry":
                  if (currentEntry != null)
                     entries.add(currentEntry);
                  currentEntry = new Entry();
                  break;
               case "f:img":
                  final String size = parser.getAttributeValue(null, "size");
                  final URL href = new URL(parser.getAttributeValue(null, "href"));
                  currentEntry.images.put(size, href);
            }
         }
      }
      if (currentEntry != null)
         entries.add(currentEntry);
   }

   @Override public boolean hasNext() {
      return index < entries.size();
   }

   @Override public URL next() {
      Entry entry = entries.get(index++);
      for (String size : sizes)
         if (entry.images.containsKey(size))
            return entry.images.get(size);
      throw new IllegalStateException("no appropriate size is available!");
   }
}
