package com.example.android.wearable.watchface;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
//    private AsyncTask<Void, Void, String> mLoadIndicesTask = new LoadIndexesTask();
//    Map<Index, Double> mIndices;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "action is " + intent.getAction());
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarmReceiver.setAlarm(context);
//            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//
//            Intent fetchStockInfoIntent = new Intent(context, BootReceiver.class);
//            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, fetchStockInfoIntent, 0);

// Set the alarm to start at 8:30 a.m.
/*
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
*/

// setRepeating() lets you specify a precise custom interval--in this case,
// 15 minutes.
//            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(),
//                    1000*60, alarmIntent);
        } else {
            Log.d(TAG, "action is " + intent.getAction());
//            mLoadIndicesTask.execute();
        }
    }

    //TODO externalize
    private static final String url = "http://finansportalen.services.six.se/finansportalen-web/rest/indicators";
    private static final String USERAGENT = "Mozilla/5.0 ;Windows NT 6.1; WOW64; AppleWebKit/537.36 ;KHTML, like Gecko; Chrome/39.0.2171.95 Safari/537.36";

/*
    public enum Index {
        OMX("Stockholm"), NASDAQ("Nasdaq"), DOW("Dow Jones");
        private String indexName;
        Index(String name) {
            indexName = name;
        }
        public String getName() {
            return indexName;
        }
    }

    public static String GET(String urlString){
        InputStream inputStream = null;
        String result = "";

        try {
            Log.d(TAG, "Trying to connect to: " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 */
/* milliseconds *//*
);
            conn.setConnectTimeout(15000 */
/* milliseconds *//*
);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            inputStream = conn.getInputStream();


            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
*/

    /* Asynchronous task to load the indexes from the content provider and
     * report the index values back using onIndexesLoaded() */
/*
    private class LoadIndexesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "LoadIndexesTask.doInBackgroun");
            }

            return GET(url);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);

                String str = "";

                JSONArray jsonIndices = json.getJSONArray("indices");
                Map<Index, Double> indices = new HashMap<>();

                if (Log.isLoggable(TAG, Log.INFO)) {
                    Log.i(TAG, "number of indices: " + json.getJSONArray("indices").length());
                }

                for (int i = 0; i < json.getJSONArray("indices").length();i++ ) {
                    JSONObject jsonObject = jsonIndices.getJSONObject(i);
                    JSONObject changePercentageToday = jsonObject.getJSONObject("changePercentageToday");

                    double change = changePercentageToday.getDouble("data");

                    if (!changePercentageToday.getString("change").equals("positive")) {
                        change = change*-1;
                    }
                    indices.put(getIndex(jsonObject.getString("name")), change);
                }

                onIndicesLoaded(indices);

            } catch (JSONException e) {
                if (Log.isLoggable(TAG, Log.ERROR)) {
                    Log.e(TAG, "det sket sig", e);
                }
            }
        }
        private void onIndicesLoaded(Map<Index, Double> indices) {
            if (indices != null && !indices.isEmpty()) {
                mIndices = indices;
//                invalidate();
            }
        }

        private Index getIndex(String name) {
            if (name.equals(Index.OMX.getName())) {
                return Index.OMX;
            } else if (name.equals(Index.NASDAQ.getName())) {
                return Index.NASDAQ;
            } else {
                return Index.DOW;
            }
        }
    }
*/
}