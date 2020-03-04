package com.example.floodmonitor;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class UrlHandler extends AsyncTask<String, Integer, String> {

    ArrayList<HashMap<String, Double>> contactList;
    private HashMap<String, Double> contact = new HashMap<>();
    //private Double temperature,pressure,humidity;
    private int temperature,pressure,humidity;
    @Override
    protected String doInBackground(String... strings) {
        String json_response = null;
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            json_response = convertStreamToString(in);
            System.out.println(json_response);
            if (json_response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(json_response);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("main");
                    System.out.println("Json Object 1" + jsonObject1);
                    temperature = (int) jsonObject1.getDouble("temp");
                    System.out.println(temperature);
                    humidity = (int) jsonObject1.getDouble("humidity");
                    System.out.println(humidity);
                    pressure = (int) jsonObject1.getDouble("pressure");
                    System.out.println(pressure);
                  //  Double windSpeed = jsonObject1.getDouble("windSpeed");
                   // System.out.println(windSpeed);


                } catch (JSONException e) {
                    Log.e("error", "Json parsing error: " + e.getMessage());
                }
            } else {
                Log.e("error", "Couldn't get json from server.");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // ArrayList arrayList = new ArrayList(MainActivity.this,contactList,new String[]{"temperature","humidity","pressure"},new int[]{R.id.cid,R.id.cname,R.id.cemail});
        System.out.println(contact.size());
        HomePage.temp.setText(String.valueOf(this.temperature));
        HomePage.hum.setText(String.valueOf(this.humidity));
        HomePage.pres.setText(String.valueOf(this.pressure));
       /* ListAdapter adapter = new SimpleAdapter(MainActivity.this, contactList,
                R.layout.list_item, new String[]{"temperature", "humidity", "pressure"}, new int[]{R.id.cid, R.id.cname, R.id.cemail});
        lv.setAdapter(adapter);*/
    }

    public String convertStreamToString(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}