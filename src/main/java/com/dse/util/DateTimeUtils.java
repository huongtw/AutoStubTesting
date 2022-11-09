package com.dse.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DateTimeUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM uuuu");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");

    public static String getDate(LocalDateTime dateTime) {
        return DATE_FORMATTER.format(dateTime).toUpperCase();
    }

    public static String getTime(LocalDateTime dateTime) {
        return TIME_FORMATTER.format(dateTime).toUpperCase();
    }

    public static LocalDateTime parse(String dateTime) {
        return LocalDateTime.parse(dateTime);
    }
}
