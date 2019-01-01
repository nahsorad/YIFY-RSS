package np.com.roshanadhikary.yifyrss;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter myAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<String> titles;
    ArrayList<String> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titles = new ArrayList<>();
        links = new ArrayList<>();

        recyclerView = findViewById(R.id.list);

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);


        new ProcessInBackground().execute();
    }

    public InputStream getInputStream(URL url){
        try {
            return url.openConnection().getInputStream();
        }
        catch(IOException e){
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("RSS Feed Loading. Please wait ...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try{
                URL url = new URL("https://yts.am/rss");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(url),"UTF_8");

                Boolean insideItem = false;
                int eventType = xpp.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){

                    if(eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("title") && insideItem == true) {
                            int token = xpp.nextToken();
                            titles.add(xpp.getText());

                        } else if (xpp.getName().equalsIgnoreCase("link") && insideItem == true) {
                            links.add(xpp.nextText());
                        }
                    }
                    else if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        insideItem = false;
                    }
                    eventType = xpp.next();
                }
            }
            catch(MalformedURLException e){
                exception = e;
            }
            catch(XmlPullParserException e){
                exception = e;
            }
            catch(IOException e){
                exception = e;
            }
            return exception;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);

            myAdapter = new MovieAdapter(titles, links);
            recyclerView.setAdapter(myAdapter);

            progressDialog.dismiss();
        }
    }
}


