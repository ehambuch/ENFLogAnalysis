package de.erichambuch.enfloganalysis;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import de.erichambuch.enfloganalysis.chart.ChartDataConverter;
import de.erichambuch.enfloganalysis.chart.MyMarker;
import de.erichambuch.enfloganalysis.chart.MyValueFormatter;
import de.erichambuch.enfloganalysis.chart.MyXAxisFormatter;
import de.erichambuch.enfloganalysis.database.ENFDatabase;
import de.erichambuch.enfloganalysis.database.ENFEntry;

/**
 * Main activity of app.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Our private database.
     */
    private ENFDatabase enfDatabase;

    /**
     * Scaling factor for different screen sizes.
     */
    private float scalingFactor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enfDatabase = Room.databaseBuilder(getApplicationContext(),
                ENFDatabase.class, "enf-historic-database").allowMainThreadQueries().build();
        // TODO: check integrity and pass all calculation activity to AsyncTask

        // try to scale text to screen resolution
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        scalingFactor = metrics.scaledDensity / metrics.density;

        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                try {
                    handleNewFile(intent); // Handle text being sent
                } catch (Exception e) {
                    Log.e(AppInfo.APP_NAME, "Error reading logfile", e);
                    Toast.makeText(this, R.string.message_error_logfile, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            try {
                paintChart();
            } catch (Exception e) {
                Log.e(AppInfo.APP_NAME, "Error displaying chart", e);
                Toast.makeText(this, R.string.message_internal_error, Toast.LENGTH_LONG).show();
                throw e; // force app to end
            }
        }
    }

    protected void onDestroy() {
        if (enfDatabase != null)
            enfDatabase.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_cleardata:
                clearAllData();
                return true;
            case R.id.menu_openenfsettings:
                openSettings();
                return true;
            case R.id.menu_info:
                showInfo();
                return true;
            case R.id.menu_faq:
                showFaq();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The exported file <code>exposure.json</code> is passed as file (Uri).
     *
     * @param intent Intent
     * @throws IOException
     * @throws JSONException
     */
    private void handleNewFile(Intent intent) throws IOException, JSONException {
        Uri uri = intent.getClipData().getItemAt(0).getUri();
        try (InputStream inStream = getContentResolver().openInputStream(uri)) {
            StringBuffer buffer = new StringBuffer(10240);
            int c;
            while ((c = inStream.read()) != -1)
                buffer.append((char) c);

            ENFEntry enfEntry = enfDatabase.enfEntryDao().findByDate(LocalDate.now());
            if (enfEntry == null) {
                enfEntry = new ENFParser(enfDatabase).addNewEntry(buffer.toString());
                Toast.makeText(this, R.string.message_data_loaded, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.message_already_loaded, Toast.LENGTH_LONG).show();
            }
            paintChart();
        }
    }

    /**
     * Reset internal database.
     */
    private void clearAllData() {
        enfDatabase.clearAllTables();
        Toast.makeText(this, R.string.message_data_cleared, Toast.LENGTH_LONG).show();
    }


    /**
     * Draw the chart from stored data.
     */
    private void paintChart() {
        ChartDataConverter converter = new ChartDataConverter(getResources(), enfDatabase.enfEntryDao(), enfDatabase.exposureChecksDao());
        paintChart(converter.calculateRiskHistory(converter.convertToChartData()));
    }

    /**
     * Draw the chart from analyzed data.
     *
     * @param chartDataList the data to display
     */
    private void paintChart(List<ChartDataConverter.ChartData> chartDataList) {
        CombinedChart chart = findViewById(R.id.chart);

        chart.setNoDataText(getString(R.string.text_nodata));
        chart.setNoDataTextColor(R.color.colorPrimaryDark);
        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        chart.getLegend().setMaxSizePercent(0.90f);

        // X axis
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(ChartDataConverter.DAYS_BACK_IN_HISTORY); // 1 month
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1.0f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setTextSize(11.0f * this.scalingFactor);
        chart.getXAxis().setTextColor(R.color.colorPrimaryDark);

        // Y axis
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setGranularity(1.0f);
        chart.getAxisLeft().setGranularityEnabled(true);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setTextColor(R.color.colorPrimaryDark);
        chart.getAxisLeft().setTextSize(11.0f * this.scalingFactor);
        chart.getAxisRight().setEnabled(false);

        // build up chart
        int dayIndex = 0;
        float maximumMatches = 0;
        List<BarEntry> barEntryList = new ArrayList<>();
        List<Entry> lineEntryList = new ArrayList<>();
        List<String> labelList = new ArrayList<>(); // List of Labels for X-Axis
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withZone(ZoneId.systemDefault());
        for (ChartDataConverter.ChartData exposureData : chartDataList) {
            BarEntry barEntry = new BarEntry(dayIndex, exposureData.matchesCount);
            barEntry.setData(formatter.format(exposureData.date));
            barEntryList.add(barEntry);
            if (exposureData.matchesCount > maximumMatches)
                maximumMatches = exposureData.matchesCount;
            if (exposureData.riskContacts > 0) { // only add line of contact probability
                Entry entry = new Entry(dayIndex, exposureData.riskContacts);
                entry.setData(exposureData.explanation);
                lineEntryList.add(entry);
            }
            labelList.add(exposureData.label());
            dayIndex++;
        }
        chart.getAxisLeft().setAxisMaximum(maximumMatches); // scale Y axis
        BarDataSet dataSet = new BarDataSet(barEntryList, "Treffer");
        LineDataSet lineSet = new LineDataSet(lineEntryList, "Mögliche Risikokontakte");

        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(R.color.colorPrimaryDark);
        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(8.0f * this.scalingFactor);
        lineSet.setColor(Color.RED);
        lineSet.setValueTextColor(R.color.colorPrimaryDark);
        lineSet.setValueTextSize(8.0f * this.scalingFactor);
        lineSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // simulate normal distribution
        lineSet.setDrawCircles(true);
        lineSet.setCircleColor(Color.RED);
        lineSet.setDrawCircleHole(false);
        lineSet.setLineWidth(2.0f);
        lineSet.setDrawValues(false);
        BarData barData = new BarData(dataSet); // add entries to dataset
        barData.setValueFormatter(new MyValueFormatter());
        barData.setHighlightEnabled(true);
        LineData lineData = new LineData(lineSet);
        lineData.setHighlightEnabled(true);

        chart.getXAxis().setValueFormatter(new MyXAxisFormatter(labelList)); // date labels for x axis
        chart.getXAxis().setLabelRotationAngle(90.0f);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);
        chart.setData(combinedData);
        MyMarker marker = new MyMarker(this, R.layout.highlighter);
        chart.setMarker(marker);
        marker.setChartView(chart);
        chart.invalidate();
    }

    private void openSettings() {
        Intent intent = new Intent("com.google.android.gms.settings.EXPOSURE_NOTIFICATION_SETTINGS");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(AppInfo.APP_NAME, "No Intent available to handle action");
            Toast.makeText(this, R.string.message_error_no_enf, Toast.LENGTH_LONG).show();
        }
    }

    private void showInfo() {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(Html.fromHtml(getString(R.string.text_license), Html.FROM_HTML_MODE_COMPACT));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private void showFaq() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.url_faq)));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(AppInfo.APP_NAME, "No Intent available to handle action");
        }
    }
}