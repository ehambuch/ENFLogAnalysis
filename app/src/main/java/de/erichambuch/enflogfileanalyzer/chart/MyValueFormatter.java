package de.erichambuch.enflogfileanalyzer.chart;

import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * Formatter for Y axis.
 */
public class MyValueFormatter extends ValueFormatter {
    public String getFormattedValue(float value) {
        return String.valueOf((int) value); // only ints
    }
}
