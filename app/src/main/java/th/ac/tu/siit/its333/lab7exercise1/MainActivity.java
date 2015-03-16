package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {
    long timeBKK=0;
    long timeNON=0;
    long timePA=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {
        int id = v.getId();

        WeatherTask w = new WeatherTask();
        switch (id) {

            case R.id.btBangkok:

                if(System.currentTimeMillis()-timeBKK > 60000 ) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    timeBKK = System.currentTimeMillis();
                    timeNON=0;
                    timePA=0;
                }
                break;
            case R.id.btNon:
                if(System.currentTimeMillis()-timeNON > 60000 ) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    timeNON = System.currentTimeMillis();
                    timeBKK=0;
                    timePA=0;
                }
                break;
            case R.id.btPathum:
                if(System.currentTimeMillis()-timePA > 60000 ) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    timePA = System.currentTimeMillis();
                    timeBKK=0;
                    timeNON=0;
                }
                break;
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double mainTemp;
        double TempMin;
        double TempMax;
        int humid;
        String weather;

        double windSpeed;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON



                    JSONObject jWeather = new JSONObject(buffer.toString());

                    JSONArray jclouds = jWeather.getJSONArray("weather");
                    JSONObject weather1= jclouds.getJSONObject(0);
                    weather = weather1.getString("main");

                    JSONObject jTemp = jWeather.getJSONObject("main");
                    mainTemp = jTemp.getDouble("temp");
                    TempMin = jTemp.getDouble("temp_min");
                    TempMax = jTemp.getDouble("temp_max");
                    humid = jTemp.getInt("humidity");

                    JSONObject jWind = jWeather.getJSONObject("wind");
                    windSpeed = jWind.getDouble("speed");

                    errorMsg = "";

                    return true;

                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvTemp,tvWeather, tvHumid ,tvWind;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid =(TextView)findViewById(R.id.tvHumid);
            tvWind = (TextView)findViewById(R.id.tvWind);

            if (result) {
                tvTitle.setText(title);
                tvWeather.setText(weather);
                tvTemp.setText(String.format("%.1f ( max = %.1f , min = %.1f )",mainTemp,TempMin,TempMax));
                tvHumid.setText(String.format("%d%%",humid));
                tvWind.setText(String.format("%.1f", windSpeed));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}
