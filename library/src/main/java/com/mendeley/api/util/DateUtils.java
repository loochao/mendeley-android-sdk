package com.mendeley.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    // ISO 8601 format, used by the Mendeley web API for timestamps.
    public final static SimpleDateFormat mendeleyApiDateFormat;

    static {
        mendeleyApiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        mendeleyApiDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Returns a {@link java.util.Date} given one String with a timestamp in the format used by the web API.
     *
     * @param date in the format used by Mendeley web API
     * @return parsed date
     * @throws java.text.ParseException
     */
    public static Date parseMendeleyApiTimestamp(String date) throws ParseException {
        synchronized (mendeleyApiDateFormat) {
            return mendeleyApiDateFormat.parse(date);
        }
    }

    public static String formatMendeleyApiTimestamp(Date date) {
        synchronized (mendeleyApiDateFormat) {
            return mendeleyApiDateFormat.format(date);
        }
    }

}
