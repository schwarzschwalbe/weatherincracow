package com.schwarzschwalbe.weatherincracow;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    // URLs Address
    String urlCurrent = "http://meteo.kdwd.webd.pl";
    String urlForecast = "http://www.meteo.pl/um/metco/mgram_pict.php?ntype=0u&row=466&col=232&lang=en&cname=Krak%F3w";
    ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Locate the Buttons in activity_main.xml
        Button refreshBC = (Button) findViewById(R.id.refreshButtonCurrent);
        Button refreshBF = (Button) findViewById(R.id.refreshButtonForecast);

        new UpdateCurrent().execute();
        new UpdateForecast().execute();

        // Capture button click
        refreshBC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            // Execute Update() AsyncTask
            new UpdateCurrent().execute();
            }
        });
        // Capture button click
        refreshBF.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Execute Update() AsyncTask
                new UpdateForecast().execute();
            }
        });
    }


    private class UpdateCurrent extends AsyncTask<Void, Void, Void> {
        String temp;
        Bitmap clouds;
        String cloudsDescription;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Checking current weather");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to the web site
                Document document = Jsoup.connect(urlCurrent).get();
                // Get the temperature
                Elements tempElement = document.select("span#gizmotemp");
                if(tempElement == null || tempElement.isEmpty()){
                    temp="error";
                }
                else temp = tempElement.text();

                // Using Elements to get the class data
                Element image = document.select("span#ajaxconditionicon2").select("img").first();
                String imgSrc = image.absUrl("src");
                cloudsDescription = image.attr("title");

                // Download image from URL
                InputStream input = new java.net.URL(imgSrc).openStream();
                // Decode Bitmap
                clouds = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                temp="error";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set temp into TextView
            TextView weatherInfoTV = (TextView) findViewById(R.id.weatherInfoTextView);
            if(!temp.equals("error"))
            {
                String text = temp + ", " + cloudsDescription;
                weatherInfoTV.setText(text);

                // Set downloaded image into ImageView
                ImageView skyI = (ImageView) findViewById(R.id.skyImage);
                skyI.setImageBitmap(clouds);
            }
            else
            {
                weatherInfoTV.setText("Server is down");
            }
            mProgressDialog.dismiss();
        }
    }




    private class UpdateForecast extends AsyncTask<Void, Void, Void> {
        Bitmap forecast=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Checking weather forecast");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(urlForecast);
                forecast = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ImageView forecastI = (ImageView) findViewById(R.id.forecastImage);
            forecastI.setImageBitmap(forecast);

            mProgressDialog.dismiss();
        }
    }
}
