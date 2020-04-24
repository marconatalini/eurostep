package com.marconatalini.eurostep.tool;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFromJsonTimestampString {

    private Date date;

    public DateFromJsonTimestampString(String timestamp) {

        JSONObject jsonTimestamp;
        long ts = 0;

        try {
            jsonTimestamp = new JSONObject(timestamp);
            ts = jsonTimestamp.getLong("timestamp")*1000;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ts == 0) ts = System.currentTimeMillis();
        this.date = new Date(ts);
    }

    public Date getDate()
    {
        return this.date;
    }
}
