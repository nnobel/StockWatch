package com.example.android.wearable.watchface;

/**
 * Created by nobel on 2015-01-17.
 */
public class LindexConfigDTO {
    private String indexUrl;
    private String hourHand;
    private String minuteHand;
    private String secondHand;

    public LindexConfigDTO(String indexUrl, String hourHand, String minuteHand, String secondHand) {
        this.indexUrl = indexUrl;
        this.hourHand = hourHand;
        this.minuteHand = minuteHand;
        this.secondHand = secondHand;
    }

    public String getIndexUrl() {

        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    public String getHourHand() {
        return hourHand;
    }

    public void setHourHand(String hourHand) {
        this.hourHand = hourHand;
    }

    public String getMinuteHand() {
        return minuteHand;
    }

    public void setMinuteHand(String minuteHand) {
        this.minuteHand = minuteHand;
    }

    public String getSecondHand() {
        return secondHand;
    }

    public void setSecondHand(String secondHand) {
        this.secondHand = secondHand;
    }
}
