package com.example.top10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps= findViewById(R.id.xmlListview);


        Log.d(TAG, "onCreate: starting AsyncTask");
        DownloadData downloadData= new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=25/xml");
        Log.d(TAG, "onCreate: Done");


    }

    private class DownloadData extends AsyncTask<String,Void,String> {
        private static final String TAG = "DownloadData";

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with "+ strings[0]);
            String rssFeed=downloadXML(strings[0]);
            if(rssFeed==null){
                Log.e(TAG, "doInBackground: Error downloading" );
            }
            return rssFeed;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is "+s);
            ParseApplications parseApplications=new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter=new ArrayAdapter<>(MainActivity.this,R.layout.list_item,parseApplications.getApplications());
////            listApps.setAdapter(arrayAdapter);

            FeedAdapter feedAdapter=new FeedAdapter(MainActivity.this,R.layout.list_record,parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);

        }

        private String downloadXML(String urlPath){
            StringBuilder xmlResult=new StringBuilder();

            try {
                URL url=new URL(urlPath);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                int response=connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code is "+response);
                BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                int charsRead;
                char[] inputBuffer=new char[500];
                while (true){
                    charsRead=reader.read(inputBuffer);
                    if(charsRead<0){
                        break;}
                    if (charsRead>0){
                        xmlResult.append(String.copyValueOf(inputBuffer,0,charsRead));
                    }
                }
                reader.close();
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL "+ e.getMessage() );
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: "+ e.getMessage());
            }catch (SecurityException e){
                Log.e(TAG, "downloadXML: Security Exception: Needs Permission?"+ e.getMessage() );

            }

            return null;

        }
    }
}
