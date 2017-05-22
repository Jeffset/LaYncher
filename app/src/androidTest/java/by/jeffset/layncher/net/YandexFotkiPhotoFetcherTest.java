package by.jeffset.layncher.net;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class YandexFotkiPhotoFetcherTest {

   private static final String text =
       "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:app=\"http://www.w3.org/2007/app\" xmlns:f=\"yandex:fotki\">\n" +
           "  <id>urn:yandex:fotki:pod:history</id>\n" +
           "  <title>История фото дня</title>\n" +
           "  <updated>2012-07-12T00:01:00Z</updated>\n" +
           "  <link href=\"http://api-fotki.yandex.ru/api/podhistory/\" rel=\"self\" />\n" +
           "  <link href=\"http://fotki.yandex.ru/hall-of-fame/\" rel=\"alternate\" />\n" +
           " \n" +
           " <entry>\n" +
           "    <id>urn:yandex:fotki:zizero43:photo:423785</id>\n" +
           "    <author>\n" +
           "      <name>zizero43</name>\n" +
           "      <f:uid>35254900</f:uid>\n" +
           "    </author>\n" +
           "    <title>Чай со смородиновым вареньем</title>\n" +
           "    <link href=\"http://api-fotki.yandex.ru/api/users/zizero43/photo/423785/\" rel=\"self\" />\n" +
           "    <link href=\"http://api-fotki.yandex.ru/api/users/zizero43/photo/423785/\" rel=\"edit\" />\n" +
           "    <link href=\"http://fotki.yandex.ru/users/zizero43/view/423785/\" rel=\"alternate\" />\n" +
           "    <link href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_orig\" rel=\"edit-media\" />\n" +
           "    <link href=\"http://api-fotki.yandex.ru/api/users/zizero43/album/131385/\" rel=\"album\" />\n" +
           "    <published>2012-03-29T15:01:34Z</published>\n" +
           "    <app:edited>2012-07-07T06:06:59Z</app:edited>\n" +
           "    <updated>2012-07-07T06:06:59Z</updated>\n" +
           "    <f:created>2012-02-25T15:35:16Z</f:created>\n" +
           "    <f:access value=\"public\" />\n" +
           "    <f:xxx value=\"false\" />\n" +
           "    <f:hide_original value=\"false\" />\n" +
           "    <f:disable_comments value=\"false\" />\n" +
           "    <f:img height=\"75\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XXS\" size=\"XXS\" width=\"75\" />\n" +
           "    <f:img height=\"705\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XL\" size=\"XL\" width=\"800\" />\n" +
           "    <f:img height=\"264\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_M\" size=\"M\" width=\"300\" />\n" +
           "    <f:img height=\"441\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_L\" size=\"L\" width=\"500\" />\n" +
           "    <f:img height=\"50\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XXXS\" size=\"XXXS\" width=\"50\" />\n" +
           "    <f:img height=\"132\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_S\" size=\"S\" width=\"150\" />>\n" +
           "    <f:img height=\"88\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XS\" size=\"XS\" width=\"100\" />\n" +
           "    <f:img bytesize=\"0\" height=\"787\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_orig\" size=\"orig\" width=\"893\" />\n" +
           "    <f:img height=\"787\" href=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XXL\" size=\"XXL\" width=\"893\" />\n" +
           "    <content src=\"http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_orig\" type=\"image/*\" />\n" +
           "    <f:pod-date>2012-04-01T00:00:00Z</f:pod-date>\n" +
           "  </entry>\n" +
           "  \n" +
           "\n" +
           "</feed>";
   private MockWebServer server;

   private void assertInfo(YandexFotkiPhotoFetcher fetcher) throws MalformedURLException {
      assertTrue(fetcher.hasNext());
      assertTrue(fetcher.entries.size() == 1);
      final YandexFotkiPhotoFetcher.Entry entry = fetcher.entries.get(0);
      assertTrue(entry.images.size() == 9);
      assertEquals(entry.images.get("XXXS"), new URL("http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XXXS"));
      assertEquals(entry.images.get("M"), new URL("http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_M"));
      assertEquals(entry.images.get("XXL"), new URL("http://img-fotki.yandex.ru/get/6204/35254900.3/0_67769_6a0d6298_XXL"));
   }

   @Before
   public void setUp() throws IOException {
      server = new MockWebServer();
      server.enqueue(new MockResponse().setBody(text));
      server.start();
   }

   @After
   public void tearDown() throws IOException {
      server.shutdown();
   }

   @Test
   public void testXmlParsing() throws IOException, XmlPullParserException {
      YandexFotkiPhotoFetcher fetcher = new YandexFotkiPhotoFetcher();
      fetcher.parseImageList(new ByteArrayInputStream(text.getBytes()));
      assertInfo(fetcher);
   }

   @Test
   public void testUsingMockWebServer() throws IOException {
      final HttpUrl url = server.url("/");
      YandexFotkiPhotoFetcher fetcher = new YandexFotkiPhotoFetcher();
      fetcher.url = url.url();
      fetcher.initFetcher();
      assertInfo(fetcher);
   }

   @Test
   public void demoServer() throws IOException, InterruptedException {
      MockWebServer server = new MockWebServer();
      server.enqueue(new MockResponse().setBody("spicy salsa"));
      server.start();

      URL url = server.url("/tacos").url();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      assertEquals(HTTP_OK, connection.getResponseCode());

      RecordedRequest request = server.takeRequest();
      assertEquals("GET /tacos HTTP/1.1", request.getRequestLine());
      server.shutdown();
   }
}