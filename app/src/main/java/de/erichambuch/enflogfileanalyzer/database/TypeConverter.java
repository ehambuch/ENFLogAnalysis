package de.erichambuch.enflogfileanalyzer.database;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Type Converter to convert Java DateTime types to <em>seconds</em> to be stored into an INTEGER field.
 */
public class TypeConverter {

    @androidx.room.TypeConverter
    public static Integer fromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null)
            return null;
        return (int) localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @androidx.room.TypeConverter
    public static LocalDateTime fromDateTimeLong(Integer timestamp) {
        if (timestamp == null)
            return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    @androidx.room.TypeConverter
    public static Integer fromLocalDate(LocalDate localDate) {
        if (localDate == null)
            return null;
        return (int) localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    @androidx.room.TypeConverter
    public static LocalDate fromDateLong(Integer timestamp) {
        if (timestamp == null)
            return null;
        return LocalDate.from(Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()));
    }
}
