package com.example.android.wearable.watchface;

import android.app.IntentService;
import android.content.Intent;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SchedulingService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {
    public SchedulingService() {
        super("SchedulingService");

    }
    public static final String PATH_WITH_FEATURE = "/stock_info";

    String mPeerId = "20141228";//getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
    GoogleApiClient mGoogleApiClient;

    public static final String TAG = "SchedulingService";
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    // The string the app searches for in the Google home page content. If the app finds
    // the string, it indicates the presence of a doodle.
    public static final String SEARCH_STRING = "doodle";
    // The Google home page URL from which the app fetches content.
    // You can find a list of other Google domains with possible doodles here:
    // http://en.wikipedia.org/wiki/List_of_Google_domains
    //public static final String URL = "http://www.google.com";
    private static final String URL = "http://finansportalen.services.six.se/finansportalen-web/rest/indicators";

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Map<Index, Double> mIndices;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart");

        super.onStart(intent, startId);
        if (!mGoogleApiClient.isConnected()) mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "ConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "ConnectionFailed");

    }

    @Override
    public void onResult(DataApi.DataItemResult dataItemResult) {
        Log.d(TAG, "success? " + dataItemResult.getStatus().isSuccess());

    }

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

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlString = URL;

        String result ="";

        // Try to connect to the Google homepage and download content.
        try {
            result = loadFromNetwork(urlString);
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle(getString(R.string.doodle_alert))
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(msg))
//                        .setContentText(msg);
//
//        mBuilder.setContentIntent(contentIntent);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

//
// The methods below this line fetch content from the specified URL and return the
// content as a string.
//
    private void parseResult(String result) {
        try {
            JSONObject json = new JSONObject(result);

            String str = "";

            JSONArray jsonIndices = json.getJSONArray("indices");
            Map<Index, Double> indices = new HashMap<>();

            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, "number of indices: " + json.getJSONArray("indices").length());
            }

            for (int i = 0; i < json.getJSONArray("indices").length(); i++) {
                JSONObject jsonObject = jsonIndices.getJSONObject(i);
                JSONObject changePercentageToday = jsonObject.getJSONObject("changePercentageToday");
                Log.d(TAG, "changePercentageToday: " + changePercentageToday);

                double change = changePercentageToday.getDouble("data");
                Log.d(TAG, "change: " + change);

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
            for (Map.Entry<Index, Double> entry : indices.entrySet()) {
                Log.d(TAG, "index: " + entry.getKey().getName() + ". Change: " + entry.getValue());
//                DataMap config = new DataMap();
//                config.putDouble(entry.getKey().getName(), entry.getValue());
//                byte[] rawData = config.toByteArray();
                Log.d(TAG, "mGoogleApiClient null?" + (mGoogleApiClient == null));

//                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, getNodeId(), PATH_WITH_FEATURE, rawData).await();

                PutDataMapRequest dataMap = PutDataMapRequest.create(PATH_WITH_FEATURE);
                dataMap.getDataMap().putDouble(entry.getKey().getName(), entry.getValue());
                PutDataRequest request = dataMap.asPutDataRequest();
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);

//                if (!result.getStatus().isSuccess()) {
//                    Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
//                }

            }
            Log.d(TAG, "done onIndicesLoaded!");
//                invalidate();
        }
    }

    private String getNodeId() {
            Set<String> results = new HashSet<String>();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                results.add(node.getId());
                Log.d(TAG, "adding node with id " + node.getId());
            }
            return results.isEmpty() ? mPeerId : results.iterator().next();
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

    /** Given a URL string, initiate a fetch operation. */
    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            str = readIt(stream);
            parseResult(str);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws IOException
     */
    private InputStream downloadUrl(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    /**
     * Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from www.google.com.
     * @return String version of InputStream.
     * @throws IOException
     */
    private String readIt(InputStream stream) throws IOException {

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        for(String line = reader.readLine(); line != null; line = reader.readLine())
            builder.append(line);
        reader.close();
        return builder.toString();
    }
}
