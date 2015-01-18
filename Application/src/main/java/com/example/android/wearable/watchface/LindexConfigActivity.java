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

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class LindexConfigActivity extends Activity {
    public static final String TAG = "LindexConfigActivity";
    public static final String URL_PREFIX = "http://";

    @Override
    protected void onStop() {
        Log.d(TAG, "Instead of an ok button I could propably save state for the activity here");
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lindex_watch_face_config);

        LindexConfigDTO lindexConfigDTO = LindexConfig.getInstance().getLindexConfig(this);
        final Button button = (Button) findViewById(R.id.button_id);

        EditText secretIndexUrl = (EditText) findViewById(R.id.index_url);
        String indexUrl = lindexConfigDTO.getIndexUrl();
        if (indexUrl == null) {
            indexUrl = URL_PREFIX;
        }
        secretIndexUrl.setText(indexUrl, TextView.BufferType.EDITABLE);
        secretIndexUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                    button.setEnabled(s != null &&
                            s.length() > 0 &&
                            !s.toString().equalsIgnoreCase(URL_PREFIX) &&
                            s.toString().startsWith(URL_PREFIX));
            }
        });

        List<String> indexNames = SchedulingService.getIndexNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                indexNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner hourHand = (Spinner) findViewById(R.id.hour_hand);
        hourHand.setAdapter(adapter);
        hourHand.setSelection(indexNames.indexOf(lindexConfigDTO.getHourHand()));
        Log.d(TAG, "hour hand: " + hourHand.getSelectedItem());

        Spinner minuteHand = (Spinner) findViewById(R.id.minute_hand);
        minuteHand.setAdapter(adapter);
        minuteHand.setSelection(indexNames.indexOf(lindexConfigDTO.getMinuteHand()));
        Log.d(TAG, "minute hand: " + minuteHand.getSelectedItem());

        Spinner secondHand = (Spinner) findViewById(R.id.second_hand);
        secondHand.setAdapter(adapter);
        secondHand.setSelection(indexNames.indexOf(lindexConfigDTO.getSecondHand()));
        Log.d(TAG, "second hand: " + secondHand.getSelectedItem());

        Editable text = secretIndexUrl.getText();

        button.setEnabled(text != null &&
                text.length() > 0 &&
                !text.toString().equalsIgnoreCase(URL_PREFIX) &&
                text.toString().startsWith(URL_PREFIX));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "button clicked!");
                TextView indexUrl = (TextView) findViewById(R.id.index_url);
                Log.d(TAG, "index url: " + indexUrl.getText());
                Spinner hourHand = (Spinner) findViewById(R.id.hour_hand);
                Log.d(TAG, "hour hand: " + hourHand.getSelectedItem());
                Spinner minuteHand = (Spinner) findViewById(R.id.minute_hand);
                Log.d(TAG, "minute hand: " + minuteHand.getSelectedItem());
                Spinner secondHand = (Spinner) findViewById(R.id.second_hand);
                Log.d(TAG, "second hand: " + secondHand.getSelectedItem());
                //TODO
                //Populate a dto with the configs and localbroadcast the values
                //broadcast listener should be the scheduling service
                //and the scheduling service need to tell which hand is doing what percentage
                LindexConfigDTO lindexConfigDTO = new LindexConfigDTO(indexUrl.getText().toString(),
                        hourHand.getSelectedItem().toString(),
                        minuteHand.getSelectedItem().toString(),
                        secondHand.getSelectedItem().toString());
                setLindexConfig(lindexConfigDTO);

                finish();
            }
        });
    }

    private void setLindexConfig(LindexConfigDTO lindexConfigDTO) {
        LindexConfig.getInstance().setLindexConfig(this, lindexConfigDTO);
    }

}
