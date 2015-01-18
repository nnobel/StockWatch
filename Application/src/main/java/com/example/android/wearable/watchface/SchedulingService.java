package com.example.android.wearable.watchface;

import android.app.IntentService;
import android.content.Intent;


import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Map<Hand, Double> mHands;

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
    public enum Hand {
        Hour, Minute, Second;
    }
    public static List<String> getIndexNames() {
        List<String> indexes = new ArrayList<String>();
        for (Index index : Index.values()) {
            indexes.add(index.getName());
        }
        return indexes;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LindexConfigDTO lindexConfigDTO = LindexConfig.getInstance().getLindexConfig(this.getApplicationContext());

        String urlString = null;
        if (lindexConfigDTO != null) {
            urlString = lindexConfigDTO.getIndexUrl();

            String result = "";

            // Try to connect to the Google homepage and download content.
            try {
                result = loadFromNetwork(urlString);
                Log.d(TAG, String.format("Called %s and obtained the result:\n%s", urlString, result));

            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        } else {
            Log.w(TAG, "could not obtain a config object");
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
            Map<Hand, Double> indices = new HashMap<>();

            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, "number of indices: " + json.getJSONArray("indices").length());
            }

            for (int i = 0; i < json.getJSONArray("indices").length(); i++) {
                JSONObject jsonObject = jsonIndices.getJSONObject(i);
                JSONObject changePercentageToday = jsonObject.getJSONObject("changePercentageToday");
                Log.d(TAG, "changePercentageToday: " + changePercentageToday);

                double change = changePercentageToday.getDouble("data");
                Log.d(TAG, "change: " + change);

                indices.put(getHand(getIndex(jsonObject.getString("name"))), change);
            }

            onIndicesLoaded(indices);

        } catch (JSONException e) {
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, "det sket sig", e);
            }
        }
    }

    private Hand getHand(Index indexName) {
        LindexConfigDTO lindexConfigDTO = LindexConfig.getInstance().getLindexConfig(this.getApplicationContext());

        switch (indexName) {
            case DOW:
                return getHand(Index.DOW, lindexConfigDTO);
            case NASDAQ:
                return getHand(Index.NASDAQ, lindexConfigDTO);
            default:
                return getHand(Index.OMX, lindexConfigDTO);
        }
    }

    private Hand getHand(Index index, LindexConfigDTO lindexConfigDTO) {
        if (lindexConfigDTO.getHourHand().equalsIgnoreCase(index.getName())) {
            return Hand.Hour;
        } else if(lindexConfigDTO.getMinuteHand().equalsIgnoreCase(index.getName())) {
            return Hand.Minute;
        } else {
            return Hand.Second;
        }
    }

    private void onIndicesLoaded(Map<Hand, Double> indices) {
        if (indices != null && !indices.isEmpty()) {
            mHands = indices;
            for (Map.Entry<Hand, Double> entry : indices.entrySet()) {
                Log.d(TAG, "index: " + entry.getKey().name() + ". Change: " + entry.getValue());
//                DataMap config = new DataMap();
//                config.putDouble(entry.getKey().getName(), entry.getValue());
//                byte[] rawData = config.toByteArray();
                Log.d(TAG, "mGoogleApiClient null?" + (mGoogleApiClient == null));

//                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, getNodeId(), PATH_WITH_FEATURE, rawData).await();

                PutDataMapRequest dataMap = PutDataMapRequest.create(PATH_WITH_FEATURE);
                dataMap.getDataMap().putDouble(entry.getKey().name(), entry.getValue());
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
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
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
