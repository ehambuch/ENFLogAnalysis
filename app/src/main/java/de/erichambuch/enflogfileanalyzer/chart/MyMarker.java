package de.erichambuch.enflogfileanalyzer.chart;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import de.erichambuch.enflogfileanalyzer.R;

/**
 * Marker that is displayed if an entry is selected (kind of tooltip).
 */
public class MyMarker extends MarkerView {

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public MyMarker(Context context, int layoutResource) {
        super(context, layoutResource);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        ((TextView) findViewById(R.id.highlightContent)).setText(e.getData() != null ? String.valueOf(e.getData()) : null);
        super.refreshContent(e, highlight);
    }
}
