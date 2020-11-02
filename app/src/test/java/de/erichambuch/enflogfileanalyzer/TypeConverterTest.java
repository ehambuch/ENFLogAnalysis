package de.erichambuch.enflogfileanalyzer;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.erichambuch.enflogfileanalyzer.database.TypeConverter;

public class TypeConverterTest {

    @Test
    public void testConvertDate() {
        LocalDate now = LocalDate.now();
        Assert.assertEquals(now, TypeConverter.fromDateLong(TypeConverter.fromLocalDate(now)));
    }

    @Test
    public void testConvertDateTime() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Assert.assertEquals(now, TypeConverter.fromDateTimeLong(TypeConverter.fromLocalDateTime(now)));
    }
}
