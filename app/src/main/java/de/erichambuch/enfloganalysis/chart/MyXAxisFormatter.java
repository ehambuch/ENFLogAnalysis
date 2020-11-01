package de.erichambuch.enfloganalysis.chart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

/**
 * Formats labels for the single days of the month (X axis).
 */
public class MyXAxisFormatter extends ValueFormatter {

    private final List<String> labels;

    public MyXAxisFormatter(List<String> labels) {
        this.labels = labels;
    }

    public String getFormattedValue(float value) {
        return String.valueOf((int) value); // only ints
    }

    public String getAxisLabel(float value, AxisBase base) {
        int i = (int) value; // values from 0...
        if (i >= 0 && i < labels.size())
            return labels.get(i);
        else
            return String.valueOf((int) value); // seems to be an error
    }
}
