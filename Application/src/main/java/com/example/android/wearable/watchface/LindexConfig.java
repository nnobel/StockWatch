package com.example.android.wearable.watchface;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nobel on 2015-01-13.
 */
public class LindexConfig {
    public static final String HOURHAND = "HOURHAND";
    public static final String MINUTEHAND = "MINUTEHAND";
    public static final String SECONDHAND = "SECONDHAND";
    public static final String SECRETINDEXURL = "SECRETINDEXURL";
    public static final String PREFS_NAME = "LindexPrefsFile";

    private LindexConfigDTO lindexConfigDTO;

    private static LindexConfig ourInstance = new LindexConfig();

    public static LindexConfig getInstance() {
        return ourInstance;
    }

    public LindexConfigDTO getLindexConfig(Context applicationContext) {
        SharedPreferences settings = applicationContext.getSharedPreferences(PREFS_NAME, 0);
        LindexConfigDTO lindexConfigDTO = new LindexConfigDTO(settings.getString(SECRETINDEXURL, null),
                settings.getString(HOURHAND, SchedulingService.Index.OMX.getName()),
                settings.getString(MINUTEHAND, SchedulingService.Index.NASDAQ.getName()),
                settings.getString(SECONDHAND, SchedulingService.Index.DOW.getName()));

        return lindexConfigDTO;
    }

    public void setLindexConfig(Context applicationContext, LindexConfigDTO lindexConfigDTO) {
        SharedPreferences settings = applicationContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(HOURHAND, lindexConfigDTO.getHourHand());
        editor.putString(MINUTEHAND, lindexConfigDTO.getMinuteHand());
        editor.putString(SECONDHAND, lindexConfigDTO.getSecondHand());
        editor.putString(SECRETINDEXURL, lindexConfigDTO.getIndexUrl());
        editor.commit();
    }

}
