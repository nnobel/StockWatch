/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.watchface;

import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * A {@link com.google.android.gms.wearable.WearableListenerService} listening for {@link com.example.android.wearable.watchface.DigitalWatchFaceService} config messages
 * and updating the config {@link com.google.android.gms.wearable.DataItem} accordingly.
 */
public class AnalogWatchFaceStockListenerService extends WearableListenerService
         {
    private static final String TAG = "AnalogWatchFaceStockListenerService";
    public static final String PATH_WITH_FEATURE = "/stock_info";

             @Override
             public void onCreate() {
                 Log.d(TAG, "google services: " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this));
                 super.onCreate();
             }

/*
    @Override // WearableListenerService
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        if (!messageEvent.getPath().equals(PATH_WITH_FEATURE)) {
            return;
        }
        byte[] rawData = messageEvent.getData();
        // It's allowed that the message carries only some of the keys used in the config DataItem
        // and skips the ones that we don't want to change.
        DataMap configKeysToOverwrite = DataMap.fromByteArray(rawData);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Received watch face config message: " + configKeysToOverwrite);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult =
                    mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }
        }

        //TODO:DigitalWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }
*/

             @Override
             public void onPeerConnected(Node peer) {
                 Log.d(TAG, "onPeerConnected: " + peer);

                 super.onPeerConnected(peer);
             }

             @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                 Log.d(TAG, "onDataChanged: " + dataEvents);
                 try {
                     for (DataEvent dataEvent : dataEvents) {
                         if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                             continue;
                         }

                         DataItem dataItem = dataEvent.getDataItem();
                         if (!dataItem.getUri().getPath().equals(PATH_WITH_FEATURE)) {
                             continue;
                         }

                         DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                         DataMap dataMap = dataMapItem.getDataMap();

                         Log.d(TAG, "DataItem updated:" + dataMap);
                         if (!dataMap.isEmpty()) {
                             Log.d(TAG, "DataItem !empty");
                             AnalogWatchFaceService.Hand hand = AnalogWatchFaceService.Hand.Hour;
                             double change = 0d;
                             double d = dataMap.getDouble(AnalogWatchFaceService.Hand.Hour.name(), Double.POSITIVE_INFINITY);
                             if (d != Double.POSITIVE_INFINITY) {
                                 Log.d(TAG, "hand: " + AnalogWatchFaceService.Hand.Hour.name() + ": " + d);
                                 hand = AnalogWatchFaceService.Hand.Hour;
                                 change = d;
                             }
                             d = dataMap.getDouble(AnalogWatchFaceService.Hand.Minute.name(), Double.POSITIVE_INFINITY);
                             if (d != Double.POSITIVE_INFINITY) {
                                 Log.d(TAG, "hand: " + AnalogWatchFaceService.Hand.Minute.name() + ": " + d);
                                 hand = AnalogWatchFaceService.Hand.Minute;
                                 change = d;
                             }
                             d = dataMap.getDouble(AnalogWatchFaceService.Hand.Second.name(), Double.POSITIVE_INFINITY);
                             if (d != Double.POSITIVE_INFINITY) {
                                 Log.d(TAG, "hand: " + AnalogWatchFaceService.Hand.Second.name() + ": " + d);
                                 hand = AnalogWatchFaceService.Hand.Second;
                                 change = d;
                             }
                             AnalogWatchFaceService.setHands(hand, change);
                         }
                         //TODO: updateUiForConfigDataMap(config);
                     }
                 } finally {
                     dataEvents.close();
                 }

             }


             @Override
             public void onMessageReceived(MessageEvent messageEvent) {
                 Log.d(TAG, "onMessageReceived: " + messageEvent);
             }
         }
